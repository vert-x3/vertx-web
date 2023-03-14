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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.*;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An authentication handler factory. This class will hold factories for creating {@link AuthenticationHandler}
 * objects.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface SecurityScheme {

  RouterBuilder apiKeyHandler(APIKeyHandler handler);

  RouterBuilder httpHandler(BasicAuthHandler handler);

  RouterBuilder httpHandler(DigestAuthHandler handler);

  RouterBuilder httpHandler(JWTAuthHandler handler);

  RouterBuilder oauth2Handler(String callback, Function<JsonObject, OAuth2AuthHandler> factory);

  default RouterBuilder oauth2Handler(Function<JsonObject, OAuth2AuthHandler> factory) {
    return oauth2Handler(null, factory);
  }

  Future<RouterBuilder> openIdConnectHandler(String callback, Function<String, Future<OAuth2AuthHandler>> factory);

  default Future<RouterBuilder> openIdConnectHandler(Function<String, Future<OAuth2AuthHandler>> factory) {
    return openIdConnectHandler(null, factory);
  }
}
