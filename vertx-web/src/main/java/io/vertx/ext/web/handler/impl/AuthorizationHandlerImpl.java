/********************************************************************************
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
 ********************************************************************************/
package io.vertx.ext.web.handler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.ext.auth.Authorization;
import io.vertx.ext.auth.AuthorizationContext;
import io.vertx.ext.auth.AuthorizationProvider;
import io.vertx.ext.auth.impl.AuthorizationContextImpl;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthorizationHandler;

/**
 * Implementation of the {@link io.vertx.ext.web.AuthorizationHandler}
 *
 * @author <a href="mail://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 */
public class AuthorizationHandlerImpl implements AuthorizationHandler {
  private final static Logger logger = Logger.getLogger(AuthorizationHandler.class.getName());
  private final static int FORBIDDEN_CODE = 403;
  private final static HttpStatusException FORBIDDEN_EXCEPTION = new HttpStatusException(FORBIDDEN_CODE);

  private Authorization authorization;
  private Collection<AuthorizationProvider> authorizationProviders;
  
  public AuthorizationHandlerImpl(Authorization authorization) {
    this.authorization = Objects.requireNonNull(authorization);
    this.authorizationProviders = new ArrayList<>();
  }

  @Override
  public void handle(RoutingContext routingContext) {
    if (routingContext.user() == null) {
      routingContext.fail(FORBIDDEN_CODE, FORBIDDEN_EXCEPTION);
    } else {
      // create the authorization context
      AuthorizationContext authorizationContext = getAuhorizationContext(routingContext);
      // check or fetch authorizations
      checkOrFetchAuthorizations(routingContext, authorizationContext, authorizationProviders.iterator());
    }
  }
  
  /**
   * this method checks that the specified authorization match the current content.
   * It doesn't fetch all providers at once in order to do early-out, but rather tries to be smart and fetch authorizations one provider at a time
   * 
   * @param routingContext
   * @param authorizationContext
   * @param providers
   */
  private void checkOrFetchAuthorizations(RoutingContext routingContext, AuthorizationContext authorizationContext, Iterator<AuthorizationProvider> providers) {
    if (authorization.match(authorizationContext)) {
      routingContext.next();
      return;
    }
    if (!providers.hasNext()) {
      routingContext.fail(FORBIDDEN_CODE, FORBIDDEN_EXCEPTION);
      return;
    }
    
    // there was no match, in this case we do the following:
    // 1) contact the next provider we haven't contacted yet
    // 2) if there is a match, get out right away otherwise repeat 1) 
    while (providers.hasNext()) {
      AuthorizationProvider provider = providers.next();
      // we haven't fetch authorization from this provider yet
      if (! routingContext.user().authorizations().getProviderIds().contains(provider.getId())) {
        provider.getAuthorizations(routingContext.user(), authorizationResult -> {
          if (authorizationResult.failed()) {
            logger.log(Level.WARNING, "An error occured getting authorization - providerId: " + provider.getId());
            // note that we don't 'record' the fact that we tried to fetch the authorization provider. therefore it will be re-fetched later-on
          }
          else {
            routingContext.user().authorizations().add(provider.getId(), authorizationResult.result());
          }
          checkOrFetchAuthorizations(routingContext, authorizationContext, providers);
        });
        // get out right now as the callback will decide what to do next
        return;
      }
    }
  }
  
  private final static AuthorizationContext getAuhorizationContext(RoutingContext event) {
    AuthorizationContext result = new AuthorizationContextImpl(event.user());
    // add request parameters,  as it may be useful to allow/deny access based on the value of a request param
    result.variables().addAll(event.request().params());
    // add the remove address
    result.variables().add(AuthorizationHandler.VARIABLE_REMOTE_IP, event.request().connection().remoteAddress().toString());
    return result;
  }
  
  @Override
  public AuthorizationHandler addAuthorizationProvider(AuthorizationProvider authorizationProvider) {
    Objects.requireNonNull(authorizationProvider);
    
    this.authorizationProviders.add(authorizationProvider);
    return this;
  }

}
