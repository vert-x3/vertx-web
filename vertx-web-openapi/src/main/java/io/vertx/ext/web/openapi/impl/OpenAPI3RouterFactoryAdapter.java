package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.RouterFactory;
import io.vertx.ext.web.openapi.RouterFactoryOptions;

import java.util.function.Function;

public class OpenAPI3RouterFactoryAdapter implements OpenAPI3RouterFactory {

  private RouterFactory routerFactory;

  public OpenAPI3RouterFactoryAdapter(RouterFactory routerFactory) {
    this.routerFactory = routerFactory;
  }

  @Override
  public OpenAPI3RouterFactory addSecurityHandler(String securitySchemaName, Handler<RoutingContext> handler) {
    routerFactory.securityHandler(securitySchemaName, handler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory setOptions(RouterFactoryOptions options) {
    routerFactory.setOptions(options);
    return this;
  }

  @Override
  public RouterFactoryOptions getOptions() {
    return routerFactory.getOptions();
  }

  @Override
  public Router getRouter() {
    return routerFactory.createRouter();
  }

  @Override
  public OpenAPI3RouterFactory setBodyHandler(BodyHandler bodyHandler) {
    routerFactory.bodyHandler(bodyHandler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addGlobalHandler(Handler<RoutingContext> globalHandler) {
    routerFactory.rootHandler(globalHandler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory setExtraOperationContextPayloadMapper(Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper) {
    routerFactory.serviceExtraPayloadMapper(extraOperationContextPayloadMapper);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addSecuritySchemaScopeValidator(String securitySchemaName, String scopeName, Handler<RoutingContext> handler) {
    routerFactory.securityHandler(securitySchemaName, scopeName, handler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addHandlerByOperationId(String operationId, Handler<RoutingContext> handler) {
    routerFactory.operation(operationId).handler(handler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory addFailureHandlerByOperationId(String operationId, Handler<RoutingContext> failureHandler) {
    routerFactory.operation(operationId).failureHandler(failureHandler);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory mountOperationToEventBus(String operationId, String address) {
    routerFactory.operation(operationId).routeToEventBus(address);
    return this;
  }

  @Override
  public OpenAPI3RouterFactory mountServiceFromTag(String tag, String address) {
    routerFactory
      .operations()
      .forEach(op -> {
        JsonArray tags = (JsonArray) JsonPointer.from("/tags").queryJsonOrDefault(op.getOperationModel(), new JsonArray());
        if (tags.contains(tag)) {
          op.routeToEventBus(address);
        }
      });
    return this;
  }

  @Override
  public OpenAPI3RouterFactory mountServicesFromExtensions() {
    routerFactory.mountServicesFromExtensions();
    return this;
  }

  @Override
  public OpenAPI3RouterFactory mountServiceInterface(Class interfaceClass, String address) {
    routerFactory.mountServiceInterface(interfaceClass, address);
    return this;
  }
}
