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
public interface JWTAuthHandler extends AuthHandler {

  /**
   * Create a JWT auth handler
   *
   * @param authProvider  the auth provider to use
   * @return the auth handler
   */
  static JWTAuthHandler create(JWTAuth authProvider) {
    return new JWTAuthHandlerImpl(authProvider, null);
  }

  /**
   * Create a JWT auth handler
   *
   * @param authProvider  the auth provider to use.
   * @return the auth handler
   */
  static JWTAuthHandler create(JWTAuth authProvider, String skip) {
    return new JWTAuthHandlerImpl(authProvider, skip);
  }

  /**
   * Set the audience list
   * @param audience  the audience list
   * @return a reference to this for fluency
   */
  @Fluent
  JWTAuthHandler setAudience(List<String> audience);

  /**
   * Set the issuer
   * @param issuer  the issuer
   * @return a reference to this for fluency
   */
  @Fluent
  JWTAuthHandler setIssuer(String issuer);

  /**
   * Set whether expiration is ignored
   * @param ignoreExpiration  whether expiration is ignored
   * @return a reference to this for fluency
   */
  @Fluent
  JWTAuthHandler setIgnoreExpiration(boolean ignoreExpiration);
}
