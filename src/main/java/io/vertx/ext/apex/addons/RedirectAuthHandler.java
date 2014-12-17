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
import io.vertx.ext.apex.addons.impl.RedirectAuthHandlerImpl;
import io.vertx.ext.auth.AuthService;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface RedirectAuthHandler extends AuthHandler {

  static final String DEFAULT_LOGIN_REDIRECT_URL = "/loginpage";
  static final String DEFAULT_RETURN_URL_PARAM = "return_url";

  static AuthHandler redirectAuthHandler(AuthService authService) {
    return new RedirectAuthHandlerImpl(authService, DEFAULT_LOGIN_REDIRECT_URL, DEFAULT_RETURN_URL_PARAM);
  }

  static AuthHandler redirectAuthHandler(AuthService authService, String loginRedirectURL) {
    return new RedirectAuthHandlerImpl(authService, loginRedirectURL, DEFAULT_RETURN_URL_PARAM);
  }

  static AuthHandler redirectAuthHandler(AuthService authService, String loginRedirectURL, String returnURLParam) {
    return new RedirectAuthHandlerImpl(authService, loginRedirectURL, returnURLParam);
  }
}
