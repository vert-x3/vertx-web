package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.parameter.ExplodedObjectValueParameterParser;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessorImpl;

public class ExplodedObjectParameterProcessorGenerator implements ParameterProcessorGenerator {

  @Override
  public boolean canGenerate(JsonObject parameter, JsonObject fakeSchema, ParameterLocation parsedLocation, String parsedStyle) {
    return OpenApi3Utils.isSchemaObjectOrCombinators(fakeSchema) &&
      OpenApi3Utils.resolveExplode(parameter) &&
      ("form".equals(parsedStyle) || "matrix".equals(parsedStyle) || "label".equals(parsedStyle));
  }

  @Override
  public ParameterProcessor generate(JsonObject parameter, JsonObject fakeSchema, JsonPointer parameterPointer, ParameterLocation parsedLocation, String parsedStyle, GeneratorContext context) {
    SchemaHolder schemas = context.getSchemaHolder(
      parameter.getJsonObject("schema", new JsonObject()),
      fakeSchema,
      parameterPointer.copy().append("schema")
    );

    return new ParameterProcessorImpl(
      parameter.getString("name"),
      parsedLocation,
      !parameter.getBoolean("required", false),
      new ExplodedObjectValueParameterParser(
        ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(schemas.getFakeSchema()),
        ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(schemas.getFakeSchema()),
        ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(schemas.getFakeSchema()),
        parameter.getString("name")
      ),
      schemas.getValidator()
    );
  }

}
