package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.body.JsonBodyProcessorImpl;

public class JsonBodyProcessorGenerator implements BodyProcessorGenerator {

  @Override
  public boolean canGenerate(String mediaTypeName, JsonObject mediaTypeObject) {
    return Utils.isJsonContentType(mediaTypeName);
  }

  @Override
  public BodyProcessor generate(String mediaTypeName, JsonObject mediaTypeObject, JsonPointer mediaTypePointer, GeneratorContext context) {
    SchemaHolder schemas = context.getSchemaHolder(
      mediaTypeObject.getJsonObject("schema", new JsonObject()),
      mediaTypePointer.copy().append("schema")
    );
    return new JsonBodyProcessorImpl(schemas.getValidator());
  }
}
