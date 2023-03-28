/* ******************************************************************************
 * Copyright (c) 2019 Stephane Bastian
 *
 * This program and the accompanying materials are made available under the 2
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 3
 *
 * Contributors: 1
 *   Stephane Bastian - initial API and implementation
 * ******************************************************************************/
package io.vertx.ext.web.handler.impl;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.audit.Marker;
import io.vertx.ext.auth.audit.SecurityAudit;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.impl.RoutingContextInternal;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Implementation of the {@link io.vertx.ext.web.handler.AuthorizationHandler}
 *
 * @author <a href="mail://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 */
public class AuthorizationHandlerImpl implements AuthorizationHandler {

  private final static Logger LOG = LoggerFactory.getLogger(AuthorizationHandler.class);

  private final static int FORBIDDEN_CODE = 403;
  private final static HttpException FORBIDDEN_EXCEPTION = new HttpException(FORBIDDEN_CODE);

  private final Authorization authorization;
  private final Collection<AuthorizationProvider> authorizationProviders;
  private BiConsumer<RoutingContext, AuthorizationContext> variableHandler;

  public AuthorizationHandlerImpl(Authorization authorization) {
    this.authorization = Objects.requireNonNull(authorization);
    this.authorizationProviders = new ArrayList<>();
  }

  public AuthorizationHandlerImpl() {
    this.authorization = null;
    this.authorizationProviders = new ArrayList<>();
  }

  private Authorization computeAuthorizationIfNeeded(RoutingContext ctx) {
    // static (RBAC)
    if (authorization != null) {
      return authorization;
    }

    // dynamic (ABAC)
    String domain = null;
    String operation = null;
    String resource = null;

    // create the authorization from the context
    final Route route = ctx.currentRoute();
    if (route != null) {
      // while it may seem that we are doing a lot of work here, by computing the permission per request
      // we can't ignore the fact that someone may have reused the handler in several routes, and in that
      // case the metadata is always different given the location.
      domain = route.getMetadata("X-ABAC-Domain");
      operation = route.getMetadata("X-ABAC-Operation");
      resource = route.getMetadata("X-ABAC-Resource");
    }
    // default to web
    if (domain == null) {
      domain = "web";
    }

    if (operation != null && resource != null) {
      // computed from the metadata
      return PermissionBasedAuthorization.create(domain + ":" + operation).setResource(resource);
    }

    // computed from the request
    return PermissionBasedAuthorization.create(domain + ":" + ctx.request().method().name()).setResource(ctx.normalizedPath());
  }

  @Override
  public void handle(RoutingContext ctx) {
    final User user = ctx.user().get();

    if (user == null) {
      ctx.fail(FORBIDDEN_CODE, FORBIDDEN_EXCEPTION);
    } else {
      try {
        // this handler can perform asynchronous operations
        if (!ctx.request().isEnded()) {
          ctx.request().pause();
        }
        // create the authorization context
        final AuthorizationContext authorizationContext;
        if (variableHandler == null) {
          // no variable handler, use the request params as source of variables
          authorizationContext = AuthorizationContext.create(user, ctx.request().params());
        } else {
          authorizationContext = AuthorizationContext.create(user);
          variableHandler.accept(ctx, authorizationContext);
        }

        // check or fetch authorizations
        checkOrFetchAuthorizations(ctx, computeAuthorizationIfNeeded(ctx), authorizationContext, authorizationProviders.iterator());
      } catch (RuntimeException e) {
        // resume as the error handler may allow this request to become valid again
        if (!ctx.request().isEnded()) {
          ctx.request().resume();
        }
        ctx.fail(e);
      }
    }
  }

  @Override
  public AuthorizationHandler variableConsumer(BiConsumer<RoutingContext, AuthorizationContext> handler) {
    this.variableHandler = handler;
    return this;
  }

  /**
   * this method checks that the specified authorization match the current content.
   * It doesn't fetch all providers at once in order to do early-out, but rather tries to be smart and fetch authorizations one provider at a time
   *
   * @param ctx                  the current routing context
   * @param authorizationContext the current authorization context
   * @param providers            the providers iterator
   */
  private void checkOrFetchAuthorizations(RoutingContext ctx, Authorization authorization, AuthorizationContext authorizationContext, Iterator<AuthorizationProvider> providers) {
    final User user = ctx.user().get();
    final SecurityAudit audit = ((RoutingContextInternal) ctx).securityAudit();
    audit.authorization(authorization);
    audit.user(user);

    if (authorization.match(authorizationContext)) {
      audit.audit(Marker.AUTHORIZATION, true);
      if (!ctx.request().isEnded()) {
        ctx.request().resume();
      }
      ctx.next();
      return;
    }

    if (user == null || !providers.hasNext()) {
      if (!ctx.request().isEnded()) {
        ctx.request().resume();
      }
      audit.audit(Marker.AUTHORIZATION, false);
      ctx.fail(FORBIDDEN_CODE, FORBIDDEN_EXCEPTION);
      return;
    }

    // there was no match, in this case we do the following:
    // 1) contact the next provider we haven't contacted yet
    // 2) if there is a match, get out right away otherwise repeat 1)
    do {
      AuthorizationProvider provider = providers.next();
      // we haven't fetched authorization from this provider yet
      if (!user.authorizations().contains(provider.getId())) {
        provider.getAuthorizations(user)
          .onFailure(err -> {
            LOG.warn("An error occurred getting authorization - providerId: " + provider.getId(), err);
            // note that we don't 'record' the fact that we tried to fetch the authorization provider.
            // therefore, it will be re-fetched later-on
          })
          .eventually(v -> {
            checkOrFetchAuthorizations(ctx, authorization, authorizationContext, providers);
            return Future.succeededFuture();
          });
        // get out right now as the callback will decide what to do next
        return;
      }
    } while (providers.hasNext());
    // reached the end of the iterator
    if (!ctx.request().isEnded()) {
      ctx.request().resume();
    }
    audit.audit(Marker.AUTHORIZATION, false);
    ctx.fail(FORBIDDEN_CODE, FORBIDDEN_EXCEPTION);
  }

  @Override
  public AuthorizationHandler addAuthorizationProvider(AuthorizationProvider authorizationProvider) {
    Objects.requireNonNull(authorizationProvider);
    this.authorizationProviders.add(authorizationProvider);
    return this;
  }
}
