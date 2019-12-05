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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthorizationHandlerImpl;

import java.util.function.BiConsumer;

import static io.vertx.codegen.annotations.GenIgnore.PERMITTED_TYPE;

/**
 * Base interface for authorization handlers that provide authorization support.
 * <p>
 * AuthorizationHandlerImpl usually requires a {@link AuthHandler} to be on the routing chain before it
 * or a custom handler that has previously set a {@link io.vertx.ext.auth.User} in the {@link io.vertx.ext.web.RoutingContext}
 *
 * @author <a href="mail://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 */
@VertxGen
public interface AuthorizationHandler extends Handler<RoutingContext> {

  /**
   * create the the handler that will check the specified authorization
   * Note that to check several authorizations, you can specify a sub-interface such as {@link io.vertx.ext.auth.authorization.AndAuthorization} or {@link io.vertx.ext.auth.authorization.OrAuthorization}
   *
   * @param authorization the authorization to attest.
   * @return fluent self.
   */
  static AuthorizationHandler create(Authorization authorization) {
    return new AuthorizationHandlerImpl(authorization);
  }

  /**
   * Adds a provider that shall be used to retrieve the required authorizations for the user to attest.
   * Multiple calls are allowed to retrieve authorizations from many sources.
   *
   * @param authorizationProvider a provider.
   * @return fluent self.
   */
  @Fluent
  AuthorizationHandler addAuthorizationProvider(AuthorizationProvider authorizationProvider);

  /**
   * Provide a simple handler to extract needed variables.
   * As it may be useful to allow/deny access based on the value of a request param one can do:
   * {@code (routingCtx, authCtx) -> authCtx.variables().addAll(routingCtx.request().params()) }
   *
   * Or for example the remote address:
   * {@code (routingCtx, authCtx) -> authCtx.result.variables().add(VARIABLE_REMOTE_IP, routingCtx.request().connection().remoteAddress()) }
   *
   * @param handler a bi consumer.
   * @return fluent self.
   */
  @Fluent
  @GenIgnore(PERMITTED_TYPE)
  AuthorizationHandler variableConsumer(BiConsumer<RoutingContext, AuthorizationContext> handler);
}
