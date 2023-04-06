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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthorizationHandlerImpl;

import java.util.function.BiConsumer;

/**
 * Base interface for authorization handlers that provide authorization support.
 * <p>
 * AuthorizationHandlerImpl usually requires a {@link AuthenticationHandler} to be on the routing chain before it
 * or a custom handler that has previously set a {@link io.vertx.ext.auth.User} in the {@link io.vertx.ext.web.RoutingContext}
 *
 * @author <a href="mail://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 */
@VertxGen
public interface AuthorizationHandler extends Handler<RoutingContext> {

  /**
   * create the handler that will check the specified authorization
   * Note that to check several authorizations, you can specify a sub-interface such as {@link io.vertx.ext.auth.authorization.AndAuthorization} or {@link io.vertx.ext.auth.authorization.OrAuthorization}
   *
   * @param authorization the authorization to attest.
   * @return fluent self.
   */
  static AuthorizationHandler create(Authorization authorization) {
    return new AuthorizationHandlerImpl(authorization);
  }

  /**
   * create the handler that will check the attribute based authorization. In this mode, the required authorization is
   * computed from the request itself or the metadata of the route. The important keys are:
   *
   * <ul>
   *   <li>{@code X-ABAC-Domain} - The domain of the permission, a domain is a the first segment of {@code domain:operation}</li>
   *   <li>{@code X-ABAC-Operation} - The operation of the permission, the operation is a the second segment of {@code domain:operation}</li>
   *   <li>{@code X-ABAC-Resource} - This is usually is a opaque string to mark the resource to access</li>
   * </ul>
   *
   * When any of these metadata values are missing they are replaced at runtime with their default values:
   *
   * <ul>
   *   <li>{@code X-ABAC-Domain} - Always {@code web}</li>
   *   <li>{@code X-ABAC-Operation} - The request HTTP {@link io.vertx.core.http.HttpMethod} from {@link HttpServerRequest#method()}</li>
   *   <li>{@code X-ABAC-Resource} - The normalized request path from {@link RoutingContext#normalizedPath()}</li>
   * </ul>
   *
   * @return fluent self.
   */
  static AuthorizationHandler create() {
    return new AuthorizationHandlerImpl();
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
  @GenIgnore
  AuthorizationHandler variableConsumer(BiConsumer<RoutingContext, AuthorizationContext> handler);
}
