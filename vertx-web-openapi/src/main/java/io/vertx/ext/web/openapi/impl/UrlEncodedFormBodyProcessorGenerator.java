package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.body.FormBodyProcessorImpl;

public class UrlEncodedFormBodyProcessorGenerator implements BodyProcessorGenerator {

  @Override
  public boolean canGenerate(String mediaTypeName, JsonObject mediaTypeObject) {
    return mediaTypeName.equals("application/x-www-form-urlencoded");
  }

  @Override
  public BodyProcessor generate(String mediaTypeName, JsonObject mediaTypeObject, JsonPointer mediaTypePointer, GeneratorContext context) {
    SchemaHolder schemas = context.getSchemaHolder(
      mediaTypeObject.getJsonObject("schema", new JsonObject()),
      mediaTypePointer.copy().append("schema")
    );
    return new FormBodyProcessorImpl(
      ValueParserInferenceUtils.infeerPropertiesFormValueParserForObjectSchema(schemas.getFakeSchema()),
      ValueParserInferenceUtils.infeerPatternPropertiesFormValueParserForObjectSchema(schemas.getFakeSchema()),
      ValueParserInferenceUtils.infeerAdditionalPropertiesFormValueParserForObjectSchema(schemas.getFakeSchema()),
      mediaTypeName,
      schemas.getValidator()
    );
  }
}
