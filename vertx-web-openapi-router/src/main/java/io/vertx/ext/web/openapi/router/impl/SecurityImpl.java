/*
 * Copyright 2023 Red Hat, Inc.
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
package io.vertx.ext.web.openapi.router.impl;

import io.vertx.core.Future;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.ext.web.openapi.router.Security;
import io.vertx.openapi.contract.SecurityScheme;

import java.util.Objects;
import java.util.function.Function;

public class SecurityImpl implements Security {

  private final RouterBuilderInternal routerBuilder;
  private final SecurityScheme securityScheme;

  private final String securitySchemeId;

  SecurityImpl(RouterBuilderInternal routerBuilder, SecurityScheme securityScheme, String securitySchemeId) {
    Objects.requireNonNull(routerBuilder, "'routerBuilder' cannot be null");
    Objects.requireNonNull(securityScheme, "'securityScheme' cannot be null");
    Objects.requireNonNull(securitySchemeId, "'securitySchemeId' cannot be null");

    this.routerBuilder = routerBuilder;
    this.securityScheme = securityScheme;
    this.securitySchemeId = securitySchemeId;
  }

  @Override
  public RouterBuilder apiKeyHandler(APIKeyHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");

    if (!"apiKey".equals(securityScheme.getType())) {
      throw new IllegalStateException("Invalid 'type' value for APIKey security scheme: " + securityScheme.getType());
    }

    Objects.requireNonNull(securityScheme.getName(), "'name' cannot be null");
    Objects.requireNonNull(securityScheme.getIn(), "'in' cannot be null");

    switch (securityScheme.getIn()) {
      case "header":
        routerBuilder.security(securitySchemeId, handler.header(securityScheme.getName()), null);
        break;
      case "query":
        routerBuilder.security(securitySchemeId, handler.parameter(securityScheme.getName()), null);
        break;
      case "cookie":
        routerBuilder.security(securitySchemeId, handler.cookie(securityScheme.getName()), null);
        break;
      default:
        throw new IllegalStateException("Invalid 'in' value for APIKey security scheme: " + securityScheme.getIn());
    }
    return routerBuilder;
  }

  @Override
  public RouterBuilder httpHandler(BasicAuthHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");

    if (!"http".equals(securityScheme.getType())) {
      throw new IllegalStateException("Invalid 'type' value for HTTP security scheme: " + securityScheme.getType());
    }

    if (!"basic".equals(securityScheme.getScheme())) {
      throw new IllegalStateException("Invalid 'schema' value for HTTP security scheme: " + securityScheme.getScheme());
    }

    routerBuilder.security(securitySchemeId, handler, null);
    return routerBuilder;
  }

  @Override
  public RouterBuilder httpHandler(DigestAuthHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");
    if (!"http".equals(securityScheme.getType())) {
      throw new IllegalStateException("Invalid 'type' value for HTTP security scheme: " + securityScheme.getType());
    }

    if (!"digest".equals(securityScheme.getScheme())) {
      throw new IllegalStateException("Invalid 'schema' value for HTTP security scheme: " + securityScheme.getScheme());
    }

    routerBuilder.security(securitySchemeId, handler, null);
    return routerBuilder;
  }

  @Override
  public RouterBuilder httpHandler(JWTAuthHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");
    if (!"http".equals(securityScheme.getType())) {
      throw new IllegalStateException("Invalid 'type' value for HTTP security scheme: " + securityScheme.getType());
    }

    if (!"bearer".equals(securityScheme.getScheme())) {
      throw new IllegalStateException("Invalid 'schema' value for HTTP security scheme: " + securityScheme.getScheme());
    }

    routerBuilder.security(securitySchemeId, handler, null);
    return routerBuilder;
  }

  @Override
  public RouterBuilder oauth2Handler(String callback, Function<SecurityScheme, OAuth2AuthHandler> factory) {
    Objects.requireNonNull(factory, "'factory' cannot be null");
    if (!"oauth2".equals(securityScheme.getType())) {
      throw new IllegalStateException("Invalid 'type' value for Oauth2 security scheme: " + securityScheme.getType());
    }

    if (securityScheme.getFlows() != null && securityScheme.getFlows().getAuthorizationCode() != null) {
      // callback is required for authorizationCode flow
      Objects.requireNonNull(callback, "'callback' cannot be null when using authorizationCode flow");
    }

    routerBuilder.security(securitySchemeId, factory.apply(securityScheme), callback);
    return routerBuilder;
  }


  @Override
  public Future<RouterBuilder> openIdConnectHandler(String callback, Function<String, Future<OAuth2AuthHandler>> factory) {
    if (!"openIdConnect".equals(securityScheme.getType())) {
      throw new IllegalStateException("Invalid 'type' value for OpenIdConnect security scheme: " + securityScheme.getType());
    }

    return factory.apply(securityScheme.getOpenIdConnectUrl())
      .onSuccess(handler -> routerBuilder.security(securitySchemeId, handler, callback))
      .map(routerBuilder);
  }
}
