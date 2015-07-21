/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.UserSessionHandlerImpl;

/**
 *
 * This handler should be used if you want to store the User object in the Session so it's available between
 * different requests, without you having re-authenticate each time.
 *
 * It requires that the session handler is already present on previous matching routes.
 *
 * It requires an Auth provider so, if the user is deserialized from a clustered session it knows which Auth provider
 * to associate the session with.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface UserSessionHandler extends Handler<RoutingContext> {

  /**
   * Create a new handler
   *
   * @param authProvider  The auth provider to use
   * @return  the handler
   */
  static UserSessionHandler create(AuthProvider authProvider) {
    return new UserSessionHandlerImpl(authProvider);
  }

}
