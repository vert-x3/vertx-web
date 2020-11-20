package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.parameter.DeepObjectValueParameterParser;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessorImpl;

public class DeepObjectParameterProcessorGenerator implements ParameterProcessorGenerator {

  @Override
  public boolean canGenerate(JsonObject parameter, JsonObject fakeParameterSchema, ParameterLocation parsedLocation, String parsedStyle) {
    return parsedStyle.equals("deepObject");
  }

  @Override
  public ParameterProcessor generate(JsonObject parameter, JsonObject fakeParameterSchema, JsonPointer parameterPointer, ParameterLocation parsedLocation, String parsedStyle, GeneratorContext context) {
    SchemaHolder schemas = context.getSchemaHolder(parameter.getJsonObject("schema", new JsonObject()), fakeParameterSchema, parameterPointer.copy().append("schema"));

    return new ParameterProcessorImpl(
      parameter.getString("name"),
      parsedLocation,
      !parameter.getBoolean("required", false),
      new DeepObjectValueParameterParser(
        parameter.getString("name"),
        ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(schemas.getFakeSchema()),
        ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(schemas.getFakeSchema()),
        ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(schemas.getFakeSchema())
      ),
      schemas.getValidator()
    );
  }
}
