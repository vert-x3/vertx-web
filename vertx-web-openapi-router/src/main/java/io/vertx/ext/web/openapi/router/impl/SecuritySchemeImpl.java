package io.vertx.ext.web.openapi.router.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.ext.web.openapi.router.SecurityScheme;
import io.vertx.openapi.contract.OpenAPIContract;

import java.util.function.Function;

public class SecuritySchemeImpl implements SecurityScheme {

  private final RouterBuilder routerBuilder;
  private final OpenAPIContract contract;

  private final String securitySchemeId;

  SecuritySchemeImpl(RouterBuilder routerBuilder, OpenAPIContract contract, String securitySchemeId) {
    this.routerBuilder = routerBuilder;
    this.contract = contract;
    this.securitySchemeId = securitySchemeId;
  }

  @Override
  public RouterBuilder bindBlocking(Function<JsonObject, AuthenticationHandler> factory) {
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        routerBuilder.securityHandler(securitySchemeId, factory.apply(securitySchemes.getJsonObject(securitySchemeId)));
        return routerBuilder;
      }
    }
    throw new IllegalStateException("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }

  @Override
  public Future<RouterBuilder> bind(Function<JsonObject, Future<AuthenticationHandler>> factory) {
    JsonObject securitySchemes = contract.getRawContract().getJsonObject("components").getJsonObject("securitySchemes");

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        return factory.apply(securitySchemes.getJsonObject(securitySchemeId))
          .onSuccess(handler -> routerBuilder.securityHandler(securitySchemeId, handler))
          .map(routerBuilder);
      }
    }
    return
      Future.failedFuture("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }
}
