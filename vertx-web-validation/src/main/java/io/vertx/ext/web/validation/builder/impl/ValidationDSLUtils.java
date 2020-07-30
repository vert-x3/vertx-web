package io.vertx.ext.web.validation.builder.impl;

import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.common.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.common.dsl.TupleSchemaBuilder;
import io.vertx.ext.web.validation.builder.ArrayParserFactory;
import io.vertx.ext.web.validation.builder.ObjectParserFactory;
import io.vertx.ext.web.validation.builder.StyledParameterProcessorFactory;
import io.vertx.ext.web.validation.builder.TupleParserFactory;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.parameter.*;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.ext.web.validation.impl.validator.SchemaValidator;

import java.util.function.BiFunction;

public class ValidationDSLUtils {

  public static BiFunction<ParameterLocation, SchemaParser, ParameterProcessor> createArrayParamFactory(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ValueParser parser = arrayParserFactory.newArrayParser(
        ValueParserInferenceUtils.infeerItemsParserForArraySchema(s.getJson())
      );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new SingleValueParameterParser(parameterName, parser),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static BiFunction<ParameterLocation, SchemaParser, ParameterProcessor> createTupleParamFactory(String parameterName, TupleParserFactory tupleParserFactory, TupleSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ValueParser parser = tupleParserFactory.newTupleParser(
          ValueParserInferenceUtils.infeerTupleParsersForArraySchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(s.getJson())
      );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new SingleValueParameterParser(parameterName, parser),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static BiFunction<ParameterLocation, SchemaParser, ParameterProcessor> createObjectParamFactory(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ValueParser parser =
        objectParserFactory.newObjectParser(
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s.getJson())
        );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new SingleValueParameterParser(parameterName, parser),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static StyledParameterProcessorFactory createExplodedArrayParamFactory(String parameterName, ArraySchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ParameterParser parser = new ExplodedArrayValueParameterParser(
          ValueParserInferenceUtils.infeerItemsParserForArraySchema(s.getJson()),
          parameterName
        );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        parser,
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static StyledParameterProcessorFactory createExplodedTupleParamFactory(String parameterName, TupleSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ParameterParser parser = new ExplodedTupleValueParameterParser(
          ValueParserInferenceUtils.infeerTupleParsersForArraySchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(s.getJson()),
          parameterName
      );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        parser,
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static StyledParameterProcessorFactory createExplodedObjectParamFactory(String parameterName, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new ExplodedObjectValueParameterParser(
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s.getJson()),
          parameterName
        ),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static StyledParameterProcessorFactory createDeepObjectParamFactory(String parameterName, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new DeepObjectValueParameterParser(
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s.getJson()),
          parameterName
        ),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }
}
