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
import io.vertx.ext.auth.AuthProvider;

/**
 * Handler that handles login from a form on a custom login page.
 * <p>
 * Used in conjunction with the {@link io.vertx.ext.apex.handler.RedirectAuthHandler}.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface FormLoginHandler extends Handler<RoutingContext> {

  /**
   * The default value of the form attribute which will contain the username
   */
  static final String DEFAULT_USERNAME_PARAM = "username";

  /**
   * The default value of the form attribute which will contain the password
   */
  static final String DEFAULT_PASSWORD_PARAM = "password";

  /**
   * The default value of the form attribute which will contain the return url
   */
  static final String DEFAULT_RETURN_URL_PARAM = "return_url";

  /**
   * Create a handler
   *
   * @param authProvider  the auth service to use
   * @return the handler
   */
  static FormLoginHandler create(AuthProvider authProvider) {
    return new FormLoginHandlerImpl(authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_PASSWORD_PARAM, DEFAULT_RETURN_URL_PARAM);
  }

  /**
   * Create a handler
   *
   * @param authProvider  the auth service to use
   * @param usernameParam   the value of the form attribute which will contain the username
   * @param passwordParam   the value of the form attribute which will contain the password
   * @param returnURLParam   the value of the form attribute which will contain the return url
   *
   * @return the handler
   */
  static FormLoginHandler create(AuthProvider authProvider, String usernameParam, String passwordParam,
                                 String returnURLParam) {
    return new FormLoginHandlerImpl(authProvider, usernameParam, passwordParam, returnURLParam);
  }

}
