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
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.handler.impl.FormLoginHandlerImpl;

/**
 * Handler that handles login from a form on a custom login page.
 * <p>
 * Used in conjunction with the {@link RedirectAuthHandler}.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface FormLoginHandler extends AuthenticationHandler {

  /**
   * Create a handler with default {@link FormLoginHandlerOptions}.
   *
   * @param authProvider  the auth service to use
   * @return the handler
   */
  static FormLoginHandler create(AuthenticationProvider authProvider) {
    return new FormLoginHandlerImpl(authProvider, new FormLoginHandlerOptions());
  }

  /**
   * Like {@link #create(AuthenticationProvider)}, with the given {@link FormLoginHandlerOptions}.
   */
  static FormLoginHandler create(AuthenticationProvider authProvider, FormLoginHandlerOptions options) {
    return new FormLoginHandlerImpl(authProvider, options);
  }
}


