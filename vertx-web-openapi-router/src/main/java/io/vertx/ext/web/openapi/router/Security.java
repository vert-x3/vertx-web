/*
 * Copyright 2021 Red Hat, Inc.
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
package io.vertx.ext.web.openapi.router;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.*;
import io.vertx.openapi.contract.SecurityScheme;

import java.util.function.Function;

/**
 * An authentication handler factory. This class will hold factories for creating {@link AuthenticationHandler}
 * objects.
 * <p>
 * Handlers will be used when creating the router. This class will configure the handlers to some extent:
 *
 * <ul>
 *   <li>{@link APIKeyHandler} - api key handlers will be configured from the document to avoid setup mistakes</li>
 *   <li>{@link BasicAuthHandler} - nothing is required to be configured, they will be used as is</li>
 *   <li>{@link DigestAuthHandler} - nothing is required to be configured, they will be used as is</li>
 *   <li>{@link JWTAuthHandler} - nothing is required to be configured, they will be used as is. Note that for scopes
 *   you may need to configure the provider to locate the claims in the right place</li>
 *   <li>{@link OAuth2AuthHandler} can be used in two forms: {@code oauth2} or {@code openIdConnect}. When using
 *   {@code oauth2} the function will receive the openapi configuration and it is the function implementor
 *   responsibility to configure the handler correctly. For {@code openIdConnect} mode the asynchronous function can be
 *   used to use the discovery mechanism.</li>
 * </ul>
 *
 * And extra note on {@link OAuth2AuthHandler} callback. The callback is optional and if not provided the default
 * behavior is to validate tokens as bearer tokens. When specified the callback <b>must</b> be an absolute path relative
 * to the root of the router.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface Security {

  /**
   * Configure the {@link APIKeyHandler} to be used when creating the router.
   * @param handler the handler to be used
   * @return caller builder
   */
  RouterBuilder apiKeyHandler(APIKeyHandler handler);

  /**
   * Configure the {@link BasicAuthHandler} to be used when creating the router.
   * @param handler the handler to be used
   * @return caller builder
   */
  RouterBuilder httpHandler(BasicAuthHandler handler);

  /**
   * Configure the {@link DigestAuthHandler} to be used when creating the router.
   * @param handler the handler to be used
   * @return caller builder
   */
  RouterBuilder httpHandler(DigestAuthHandler handler);

  /**
   * Configure the {@link JWTAuthHandler} to be used when creating the router.
   * @param handler the handler to be used
   * @return caller builder
   */
  RouterBuilder httpHandler(JWTAuthHandler handler);

  /**
   * Configure the {@link OAuth2AuthHandler} to be used when creating the router.
   * @param callback the callback path to be used to validate tokens
   * @param factory the handler factory that will receive the configuration and return the handler
   * @return caller builder
   */
  RouterBuilder oauth2Handler(String callback, Function<SecurityScheme, OAuth2AuthHandler> factory);

  /**
   * Configure the {@link OAuth2AuthHandler} to be used when creating the router. In this mode the callback is not used
   * and will not handle {@code authorization_code} flows.
   * @param factory the handler factory that will receive the configuration and return the handler
   * @return caller builder
   */
  default RouterBuilder oauth2Handler(Function<SecurityScheme, OAuth2AuthHandler> factory) {
    return oauth2Handler(null, factory);
  }

  /**
   * Configure the {@link OAuth2AuthHandler} to be used when creating the router.
   * @param callback the callback path to be used to validate tokens
   * @param factory the handler factory that will receive the {@code openIdConnect} discovery url and return the handler
   * @return caller builder
   */
  Future<RouterBuilder> openIdConnectHandler(String callback, Function<String, Future<OAuth2AuthHandler>> factory);

  /**
   * Configure the {@link OAuth2AuthHandler} to be used when creating the router. In this mode the callback is not used
   * and will not handle {@code authorization_code} flows.
   * @param factory the handler factory that will receive the {@code openIdConnect} discovery url and return the handler
   * @return caller builder
   */
  default Future<RouterBuilder> openIdConnectHandler(Function<String, Future<OAuth2AuthHandler>> factory) {
    return openIdConnectHandler(null, factory);
  }
}
