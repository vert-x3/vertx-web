package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.VertxGen;

import java.util.function.Function;

/**
 * The way an OpenAPI operation is transformed into a route name.
 */
@VertxGen
public enum RouteNamingStrategy implements Function<Operation, String> {
  OPERATION_ID(Operation::getOperationId),
  OPERATION_OPENAPI_PATH(Operation::getOpenAPIPath);

  private final Function<Operation, String> impl;

  RouteNamingStrategy(Function<Operation, String> impl) {
    this.impl = impl;
  }

  @Override
  public String apply(Operation operation) {
    return impl.apply(operation);
  }
}
