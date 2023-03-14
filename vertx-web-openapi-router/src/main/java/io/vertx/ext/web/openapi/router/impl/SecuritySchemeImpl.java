package io.vertx.ext.web.openapi.router.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.ext.web.openapi.router.SecurityScheme;
import io.vertx.openapi.contract.OpenAPIContract;

import java.util.Objects;
import java.util.function.Function;

public class SecuritySchemeImpl implements SecurityScheme {

  private final RouterBuilder routerBuilder;
  private final OpenAPIContract contract;

  private final String securitySchemeId;

  private boolean allowCallback;

  SecuritySchemeImpl(RouterBuilder routerBuilder, OpenAPIContract contract, String securitySchemeId) {
    this.routerBuilder = routerBuilder;
    this.contract = contract;
    this.securitySchemeId = securitySchemeId;
  }

  @Override
  public RouterBuilder apiKeyHandler(APIKeyHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        final JsonObject config = securitySchemes.getJsonObject(securitySchemeId);

        if (!"apiKey".equals(config.getString("type"))) {
          throw new IllegalStateException("Invalid 'type' value for APIKey security scheme: " + config.getString("type"));
        }

        Objects.requireNonNull(config.getString("name"), "'name' cannot be null");

        switch (config.getString("in", "<null>")) {
          case "header":
            routerBuilder.security(securitySchemeId, handler.header(config.getString("name")), null);
            break;
          case "query":
            routerBuilder.security(securitySchemeId, handler.parameter(config.getString("name")), null);
            break;
          case "cookie":
            routerBuilder.security(securitySchemeId, handler.cookie(config.getString("name")), null);
            break;
          default:
            throw new IllegalStateException("Invalid 'in' value for APIKey security scheme: " + config.getString("in"));
        }
        return routerBuilder;
      }
    }
    throw new IllegalStateException("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }

  @Override
  public RouterBuilder httpHandler(BasicAuthHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        final JsonObject config = securitySchemes.getJsonObject(securitySchemeId);

        if (!"http".equals(config.getString("type"))) {
          throw new IllegalStateException("Invalid 'type' value for HTTP security scheme: " + config.getString("type"));
        }

        if (!"basic".equals(config.getString("scheme"))) {
          throw new IllegalStateException("Invalid 'schema' value for HTTP security scheme: " + config.getString("scheme"));
        }

        routerBuilder.security(securitySchemeId, handler, null);
        return routerBuilder;
      }
    }
    throw new IllegalStateException("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }

  @Override
  public RouterBuilder httpHandler(DigestAuthHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        final JsonObject config = securitySchemes.getJsonObject(securitySchemeId);

        if (!"http".equals(config.getString("type"))) {
          throw new IllegalStateException("Invalid 'type' value for HTTP security scheme: " + config.getString("type"));
        }

        if (!"digest".equals(config.getString("scheme"))) {
          throw new IllegalStateException("Invalid 'schema' value for HTTP security scheme: " + config.getString("scheme"));
        }

        routerBuilder.security(securitySchemeId, handler, null);
        return routerBuilder;
      }
    }
    throw new IllegalStateException("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }

  @Override
  public RouterBuilder httpHandler(JWTAuthHandler handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        final JsonObject config = securitySchemes.getJsonObject(securitySchemeId);

        if (!"http".equals(config.getString("type"))) {
          throw new IllegalStateException("Invalid 'type' value for HTTP security scheme: " + config.getString("type"));
        }

        if (!"bearer".equals(config.getString("scheme"))) {
          throw new IllegalStateException("Invalid 'schema' value for HTTP security scheme: " + config.getString("scheme"));
        }

        routerBuilder.security(securitySchemeId, handler, null);
        return routerBuilder;
      }
    }
    throw new IllegalStateException("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }

  @Override
  public RouterBuilder oauth2Handler(String callback, Function<JsonObject, OAuth2AuthHandler> factory) {
    Objects.requireNonNull(factory, "'factory' cannot be null");
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        final JsonObject config = securitySchemes.getJsonObject(securitySchemeId);

        if (!"oauth2".equals(config.getString("type"))) {
          throw new IllegalStateException("Invalid 'type' value for Oauth2 security scheme: " + config.getString("type"));
        }

        routerBuilder.security(securitySchemeId, factory.apply(config), callback);
        return routerBuilder;
      }
    }
    throw new IllegalStateException("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }


  @Override
  public Future<RouterBuilder> openIdConnectHandler(String callback, Function<String, Future<OAuth2AuthHandler>> factory) {
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        final JsonObject config = securitySchemes.getJsonObject(securitySchemeId);

        if (!"openIdConnect".equals(config.getString("type"))) {
          throw new IllegalStateException("Invalid 'type' value for OpenIdConnect security scheme: " + config.getString("type"));
        }

        return factory.apply(config.getString("openIdConnectUrl"))
          .onSuccess(handler -> routerBuilder.security(securitySchemeId, handler, callback))
          .map(routerBuilder);
      }
    }
    return
      Future.failedFuture("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }
}
