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

package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.apex.addons.impl.BasicAuthHandlerImpl;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.auth.AuthService;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface BasicAuthHandler extends AuthHandler {

  static final String DEFAULT_REALM = "apex";

  static AuthHandler basicAuthHandler(AuthService authService) {
    return new BasicAuthHandlerImpl(authService, DEFAULT_REALM);
  }

  static AuthHandler basicAuthHandler(AuthService authService, String realm) {
    return new BasicAuthHandlerImpl(authService, realm);
  }

  @Override
  void handle(RoutingContext context);
}
