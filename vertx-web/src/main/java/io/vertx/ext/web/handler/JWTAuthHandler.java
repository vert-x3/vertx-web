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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.handler.impl.JWTAuthHandlerImpl;

import java.util.List;

/**
 * An auth handler that provides JWT Authentication support.
 *
 * @author Paulo Lopes
 */
@VertxGen
public interface JWTAuthHandler extends AuthenticationHandler {

  /**
   * Create a JWT auth handler
   *
   * @param authProvider  the auth provider to use
   * @return the auth handler
   */
  static JWTAuthHandler create(JWTAuth authProvider) {
    return create(authProvider, null);
  }

  /**
   * Create a JWT auth handler
   *
   * @param authProvider  the auth provider to use
   * @return the auth handler
   */
  static JWTAuthHandler create(JWTAuth authProvider, String realm) {
    return new JWTAuthHandlerImpl(authProvider, realm);
  }

  /**
   * Return a new instance with the internal state copied from the caller but the scopes delimiter set
   * to be unique to the instance.
   *
   * @param delimiter scope delimiter.
   * @return new instance of this interface.
   */
  @Fluent
  JWTAuthHandler scopeDelimiter(String delimiter);

  /**
   * Return a new instance with the internal state copied from the caller but the scopes to be requested during a token
   * request are unique to the instance.
   *
   * @param scope scope.
   * @return new instance of this interface.
   */
  @Fluent
  JWTAuthHandler withScope(String scope);

  /**
   * Return a new instance with the internal state copied from the caller but the scopes to be requested during a token
   * request are unique to the instance.
   *
   * @param scopes scopes.
   * @return new instance of this interface.
   */
  @Fluent
  JWTAuthHandler withScopes(List<String> scopes);
}
