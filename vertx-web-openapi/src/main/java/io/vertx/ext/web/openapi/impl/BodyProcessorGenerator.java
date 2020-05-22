package io.vertx.ext.web.openapi.impl;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;

public interface BodyProcessorGenerator {

  boolean canGenerate(String mediaTypeName, JsonObject mediaTypeObject);

  @GenIgnore
  BodyProcessor generate(
    String mediaTypeName,
    JsonObject mediaTypeObject,
    JsonPointer mediaTypePointer,
    GeneratorContext context
  );
}
