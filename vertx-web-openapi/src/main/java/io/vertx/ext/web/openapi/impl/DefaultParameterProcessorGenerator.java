package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessorImpl;
import io.vertx.ext.web.validation.impl.parameter.SingleValueParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultParameterProcessorGenerator implements ParameterProcessorGenerator {

  @Override
  public boolean canGenerate(JsonObject parameter, JsonObject fakeSchema, ParameterLocation parsedLocation, String parsedStyle) {
    return !parameter.containsKey("content");
  }

  @Override
  public ParameterProcessor generate(JsonObject parameter, JsonObject fakeSchema, JsonPointer parameterPointer, ParameterLocation parsedLocation, String parsedStyle, GeneratorContext context) {
    SchemaHolder schemas = context.getSchemaHolder(
      parameter.getJsonObject("schema", new JsonObject()),
      fakeSchema,
      parameterPointer.copy().append("schema")
    );

    if (parsedLocation == ParameterLocation.QUERY && parameter.getBoolean("allowEmptyValue", false) && "boolean".equals(fakeSchema.getString("type"))) {
      // Flag parameter!
      return new ParameterProcessorImpl(
        parameter.getString("name"),
        parsedLocation,
        !parameter.getBoolean("required", false),
        new FlagParameterParser(
          parameter.getString("name")
        ),
        schemas.getValidator()
      );
    } else if (OpenAPI3Utils.isFakeSchemaAnyOfOrOneOf(fakeSchema)) {
      // anyOf or oneOf
      List<ValueParser<String>> valueParsers = fakeSchema.getJsonArray("x-anyOf", fakeSchema.getJsonArray("x-oneOf"))
        .stream()
        .map(j -> (JsonObject)j)
        .map(j -> generateValueParser(j, parsedStyle))
        .collect(Collectors.toList());

      return new ParameterProcessorImpl(
        parameter.getString("name"),
        parsedLocation,
        !parameter.getBoolean("required", false),
        new AnyOfOneOfSingleParameterParser(
          parameter.getString("name"),
          valueParsers
        ),
        schemas.getValidator()
      );
    } else {
      // Other parameters
      ValueParser<String> valueParser = generateValueParser(fakeSchema, parsedStyle);

      return new ParameterProcessorImpl(
        parameter.getString("name"),
        parsedLocation,
        !parameter.getBoolean("required", false),
        new SingleValueParameterParser(
          parameter.getString("name"),
          valueParser
        ),
        schemas.getValidator()
      );
    }
  }

  private ValueParser<String> generateValueParserForObjectParameter(JsonObject fakeSchema, String parsedStyle) {
    return ContainerSerializationStyles.resolve(parsedStyle).getObjectFactory().newObjectParser(
      ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(fakeSchema),
      ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(fakeSchema),
      ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(fakeSchema)
    );
  }

  private ValueParser<String> generateForArrayParameter(JsonObject fakeSchema, String parsedStyle) {
    return ContainerSerializationStyles.resolve(parsedStyle).getArrayFactory().newArrayParser(
      ValueParserInferenceUtils.infeerItemsParserForArraySchema(fakeSchema)
    );
  }

  private ValueParser<String> generateForPrimitiveParameter(JsonObject fakeSchema) {
    return ValueParserInferenceUtils.infeerPrimitiveParser(fakeSchema);
  }

  private ValueParser<String> generateValueParser(JsonObject fakeSchema, String parsedStyle) {
    if (OpenAPI3Utils.isSchemaObjectOrCombinators(fakeSchema)) {
      return generateValueParserForObjectParameter(fakeSchema, parsedStyle);
    } else if (OpenAPI3Utils.isSchemaArray(fakeSchema)) {
      return generateForArrayParameter(fakeSchema, parsedStyle);
    } else {
      return generateForPrimitiveParameter(fakeSchema);
    }
  }

}
