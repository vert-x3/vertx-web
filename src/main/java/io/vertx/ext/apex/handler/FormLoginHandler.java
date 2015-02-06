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

package io.vertx.ext.apex.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.handler.impl.FormLoginHandlerImpl;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.auth.AuthService;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface FormLoginHandler extends Handler<RoutingContext> {

  static final String DEFAULT_USERNAME_PARAM = "username";
  static final String DEFAULT_PASSWORD_PARAM = "password";
  static final String DEFAULT_RETURN_URL_PARAM = "return_url";

  static FormLoginHandler create(AuthService authService) {
    return new FormLoginHandlerImpl(authService, DEFAULT_USERNAME_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM);
  }

  static FormLoginHandler create(AuthService authService, String usernameParam, String passwordParam,
                                 String returnURLParam) {
    return new FormLoginHandlerImpl(authService, usernameParam, passwordParam, returnURLParam);
  }

  @Override
  void handle(RoutingContext context);

}
