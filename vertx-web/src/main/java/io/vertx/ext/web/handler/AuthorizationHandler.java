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
import io.vertx.core.Handler;
import io.vertx.ext.auth.Authorization;
import io.vertx.ext.auth.AuthorizationProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthorizationHandlerImpl;

/**
 * Base interface for authorization handlers that provide authorization support.
 * <p>
 * AuthorizationHandlerImpl usually requires a {@link AuthenticationHandler} to be on the routing chain before it
 * or a custom handler that has previously set a {@link io.vertx.ext.auth.User} in the {@link io.vertx.ext.web.RoutingContext}
 *
 * @author <a href="mail://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 */
public interface AuthorizationHandler extends Handler<RoutingContext> {
  /**
   * this is the name of the variable added to the AuthorizationContext that represent the remote ip address
   */
  public final static String VARIABLE_REMOTE_IP = "remote-ip";

  /**
   * create the the handler that will check the specified authorization 
   * Note that to check several authorizations, you can specify a sub-interface such as {@link io.vertx.ext.auth.AndAuthorization} or {@link io.vertx.ext.auth.OrAuthorization}
   * 
   * @param authorization
   * @return
   */
  static AuthorizationHandler create(Authorization authorization) {
    return new AuthorizationHandlerImpl(authorization);
  }

  @Fluent
  AuthorizationHandler addAuthorizationProvider(AuthorizationProvider authorizationProvider);
  
}
