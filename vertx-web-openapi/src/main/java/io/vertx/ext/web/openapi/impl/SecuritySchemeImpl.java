package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.SecurityScheme;

import java.util.function.Function;

public class SecuritySchemeImpl implements SecurityScheme {

  private final RouterBuilder routerBuilder;
  private final String securitySchemeId;

  SecuritySchemeImpl(RouterBuilder routerBuilder, String securitySchemeId) {
    this.routerBuilder = routerBuilder;
    this.securitySchemeId = securitySchemeId;
  }

  @Override
  public String securitySchemeId() {
    return securitySchemeId;
  }

  @Override
  public RouterBuilder bindBlocking(Function<JsonObject, AuthenticationHandler> factory) {
    JsonObject securitySchemes = routerBuilder.getOpenAPI().getCached(JsonPointer.from("/components/securitySchemes"));

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
    JsonObject securitySchemes = routerBuilder.getOpenAPI().getCached(JsonPointer.from("/components/securitySchemes"));

    if (securitySchemes != null) {
      if (securitySchemes.containsKey(securitySchemeId)) {
        return factory.apply(securitySchemes.getJsonObject(securitySchemeId))
          .onSuccess(handler -> routerBuilder.securityHandler(securitySchemeId, handler))
          .mapEmpty();
      }
    }
    return
      Future.failedFuture("OpenAPI does not contain securityScheme: " + securitySchemeId);
  }
}
