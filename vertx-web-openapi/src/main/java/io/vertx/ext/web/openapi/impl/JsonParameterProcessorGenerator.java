package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessorImpl;
import io.vertx.ext.web.validation.impl.parameter.SingleValueParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

public class JsonParameterProcessorGenerator implements ParameterProcessorGenerator {

  private final static JsonPointer CONTENT_JSON_POINTER = JsonPointer.create().append("content").append("application/json");
  private final static JsonPointer SCHEMA_POINTER = CONTENT_JSON_POINTER.copy().append("schema");

  @Override
  public boolean canGenerate(JsonObject parameter, JsonObject fakeSchema, ParameterLocation parsedLocation, String parsedStyle) {
    return CONTENT_JSON_POINTER.queryJson(parameter) != null;
  }

  @Override
  public ParameterProcessor generate(JsonObject parameter, JsonObject fakeSchema, JsonPointer parameterPointer, ParameterLocation parsedLocation, String parsedStyle, GeneratorContext context) {
    JsonObject originalSchema = (JsonObject) SCHEMA_POINTER.queryJsonOrDefault(parameter, new JsonObject());
    SchemaHolder schemas = context.getSchemaHolder(
      originalSchema, context.fakeSchema(fakeSchema),
      parameterPointer.copy().append("content").append("application/json").append("schema")
    );
    return new ParameterProcessorImpl(
      parameter.getString("name"),
      parsedLocation,
      !parameter.getBoolean("required", false),
      new SingleValueParameterParser(parameter.getString("name"), ValueParser.JSON_PARSER),
      schemas.getValidator()
    );
  }

}
