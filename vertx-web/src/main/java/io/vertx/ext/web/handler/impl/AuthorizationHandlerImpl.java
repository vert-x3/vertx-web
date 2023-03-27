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
import io.vertx.ext.auth.authorization.*;
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

  private final boolean abac;
  private final Authorization authorization;
  private final Collection<AuthorizationProvider> authorizationProviders;
  private BiConsumer<RoutingContext, AuthorizationContext> variableHandler;

  public AuthorizationHandlerImpl(Authorization authorization) {
    this.abac = false;
    this.authorization = Objects.requireNonNull(authorization);
    this.authorizationProviders = new ArrayList<>();
  }

  public AuthorizationHandlerImpl() {
    this.abac = true;
    this.authorization = null;
    this.authorizationProviders = new ArrayList<>();
  }

  @Override
  public void handle(RoutingContext ctx) {
    if (!ctx.user().authenticated()) {
      ctx.fail(FORBIDDEN_CODE, FORBIDDEN_EXCEPTION);
    } else {
      final User user = ctx.user().get();

      try {
        // this handler can perform asynchronous operations
        if (!ctx.request().isEnded()) {
          ctx.request().pause();
        }
        // create the authorization context
        final AuthorizationContext authorizationContext = AuthorizationContext.create(user);
        if (variableHandler != null) {
          variableHandler.accept(ctx, authorizationContext);
        }

        final Authorization authorization;

        if (abac) {
          // create the authorization from the context
          Map<String, Object> metadata = ctx.currentRoute().metadata();
          if (metadata == null) {
            metadata = Collections.emptyMap();
          }
          String domain = (String) metadata.getOrDefault("X-ABAC-Domain", "web");
          String operation = (String) metadata.getOrDefault("X-ABAC-Operation", ctx.request().method().name());
          String resource = (String) metadata.getOrDefault("X-ABAC-Resource", ctx.normalizedPath());
          authorization = WildcardPermissionBasedAuthorization.create(domain + ":" + operation).setResource(resource);
        } else {
          authorization = this.authorization;
        }

        // check or fetch authorizations
        checkOrFetchAuthorizations(ctx, authorization, authorizationContext, authorizationProviders.iterator());
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
            // note that we don't 'record' the fact that we tried to fetch the authorization provider. therefore, it will be re-fetched later-on
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
