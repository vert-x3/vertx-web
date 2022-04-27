package io.vertx.ext.web.validation.builder.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.builder.ArrayParserFactory;
import io.vertx.ext.web.validation.builder.ObjectParserFactory;
import io.vertx.ext.web.validation.builder.StyledParameterProcessorFactory;
import io.vertx.ext.web.validation.builder.TupleParserFactory;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.parameter.*;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.ext.web.validation.impl.validator.ValueValidator;
import io.vertx.json.schema.common.dsl.ArraySchemaBuilder;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.json.schema.common.dsl.TupleSchemaBuilder;
import io.vertx.json.schema.validator.Schema;
import io.vertx.json.schema.validator.SchemaRepository;
import io.vertx.json.schema.validator.impl.SchemaValidatorInternal;

import java.util.function.BiFunction;

public class ValidationDSLUtils {

  public static BiFunction<ParameterLocation, SchemaRepository, ParameterProcessor> createArrayParamFactory(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, repository) -> {
      final JsonObject schema = schemaBuilder.toJson();
      ValueParser<String> parser = arrayParserFactory.newArrayParser(
        ValueParserInferenceUtils.infeerItemsParserForArraySchema(schema)
      );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new SingleValueParameterParser(location.lowerCaseIfNeeded(parameterName), parser),
        new ValueValidator((SchemaValidatorInternal) repository.validator(Schema.of(schema))));
    };
  }

  public static BiFunction<ParameterLocation, SchemaRepository, ParameterProcessor> createTupleParamFactory(String parameterName, TupleParserFactory tupleParserFactory, TupleSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, repository) -> {
      JsonObject json = schemaBuilder.toJson();
      ValueParser<String> parser = tupleParserFactory.newTupleParser(
        ValueParserInferenceUtils.infeerTupleParsersForArraySchema(json),
        ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(json)
      );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new SingleValueParameterParser(location.lowerCaseIfNeeded(parameterName), parser),
        new ValueValidator((SchemaValidatorInternal) repository.validator(Schema.of(json))));
    };
  }

  public static BiFunction<ParameterLocation, SchemaRepository, ParameterProcessor> createObjectParamFactory(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, repository) -> {
      final JsonObject json = schemaBuilder.toJson();
      ValueParser<String> parser =
        objectParserFactory.newObjectParser(
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(json),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(json),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(json)
        );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new SingleValueParameterParser(location.lowerCaseIfNeeded(parameterName), parser),
        new ValueValidator((SchemaValidatorInternal) repository.validator(Schema.of(json))));
    };
  }

  public static StyledParameterProcessorFactory createExplodedArrayParamFactory(String parameterName, ArraySchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, repository) -> {
      final JsonObject json = schemaBuilder.toJson();
      ParameterParser parser = new ExplodedArrayValueParameterParser(
        location.lowerCaseIfNeeded(parameterName),
        ValueParserInferenceUtils.infeerItemsParserForArraySchema(json)
      );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        parser,
        new ValueValidator((SchemaValidatorInternal) repository.validator(Schema.of(json))));
    };
  }

  public static StyledParameterProcessorFactory createExplodedTupleParamFactory(String parameterName, TupleSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, repository) -> {
      final JsonObject json = schemaBuilder.toJson();
      ParameterParser parser = new ExplodedTupleValueParameterParser(
        location.lowerCaseIfNeeded(parameterName),
        ValueParserInferenceUtils.infeerTupleParsersForArraySchema(json),
        ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(json)
      );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        parser,
        new ValueValidator((SchemaValidatorInternal) repository.validator(Schema.of(schemaBuilder.toJson()))));
    };
  }

  public static StyledParameterProcessorFactory createExplodedObjectParamFactory(String parameterName, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, repository) -> {
      final JsonObject json = schemaBuilder.toJson();
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new ExplodedObjectValueParameterParser(
          parameterName,
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(json, location::lowerCaseIfNeeded),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(json),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(json)
        ),
        new ValueValidator((SchemaValidatorInternal) repository.validator(Schema.of(json))));
    };
  }

  public static StyledParameterProcessorFactory createDeepObjectParamFactory(String parameterName, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, repository) -> {
      final JsonObject json = schemaBuilder.toJson();
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new DeepObjectValueParameterParser(
          parameterName, ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(json),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(json),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(json)
        ),
        new ValueValidator((SchemaValidatorInternal) repository.validator(Schema.of(json))));
    };
  }
}
