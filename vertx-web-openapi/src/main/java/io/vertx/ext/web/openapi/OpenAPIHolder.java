package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

@VertxGen
public interface OpenAPIHolder {

  /**
   * Get cached object using {@code pointer}
   *
   * @param pointer
   * @return
   */
  JsonObject getCached(JsonPointer pointer);

  /**
   * if {@code obj} contains {@code $ref}, it tries to solve it and return the resolved object, otherwise it returns {@code obj}
   *
   * @param obj
   * @return
   */
  JsonObject solveIfNeeded(JsonObject obj);

  /**
   * @return the root of the OpenAPI document
   */
  JsonObject getOpenAPI();

}
