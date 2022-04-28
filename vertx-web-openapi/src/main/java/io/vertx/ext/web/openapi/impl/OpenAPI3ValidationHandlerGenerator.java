package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.openapi.RouterBuilderException;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.ValidationHandlerImpl;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.json.schema.SchemaParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAPI3ValidationHandlerGenerator {

  private static final JsonPointer BODY_REQUIRED_POINTER = JsonPointer.from("/requestBody/required");
  private static final JsonPointer REQUEST_BODY_CONTENT_POINTER = JsonPointer.from("/requestBody/content");

  private final OpenAPIHolderImpl holder;
  private final SchemaParser schemaParser;
  private final JsonPointerIteratorWithLoader iteratorWithLoader;
  private final List<ParameterProcessorGenerator> parameterProcessorGenerators;
  private final List<BodyProcessorGenerator> bodyProcessorGenerators;

  public OpenAPI3ValidationHandlerGenerator(OpenAPIHolderImpl holder, SchemaParser schemaParser) {
    this.holder = holder;
    this.schemaParser = schemaParser;
    this.iteratorWithLoader = new JsonPointerIteratorWithLoader(holder);
    this.parameterProcessorGenerators = new ArrayList<>();
    this.bodyProcessorGenerators = new ArrayList<>();
  }

  public OpenAPI3ValidationHandlerGenerator addParameterProcessorGenerator(ParameterProcessorGenerator gen) {
    parameterProcessorGenerators.add(gen);
    return this;
  }

  public OpenAPI3ValidationHandlerGenerator addBodyProcessorGenerator(BodyProcessorGenerator gen) {
    bodyProcessorGenerators.add(gen);
    return this;
  }

  public ValidationHandler create(OperationImpl operation) {
    //TODO error handling of this function?
    Map<ParameterLocation, List<ParameterProcessor>> parameterProcessors = new HashMap<>();
    List<BodyProcessor> bodyProcessors = new ArrayList<>();

    GeneratorContext context = new GeneratorContext(this.schemaParser, holder, operation);

    // Parse parameter processors
    for (Map.Entry<JsonPointer, JsonObject> pe : operation.getParameters().entrySet()) {
      ParameterLocation parsedLocation = ParameterLocation.valueOf(pe.getValue().getString("in").toUpperCase());
      String parsedStyle = OpenAPI3Utils.resolveStyle(pe.getValue());

      if (pe.getValue().getBoolean("allowReserved", false))
        throw RouterBuilderException
          .createUnsupportedSpecFeature("You are using allowReserved keyword in parameter " + pe.getKey() + " which " +
            "is not supported");

      JsonObject fakeSchema = context.fakeSchema(pe.getValue().getJsonObject("schema", new JsonObject()));

      ParameterProcessorGenerator generator = parameterProcessorGenerators.stream()
        .filter(g -> g.canGenerate(pe.getValue(), fakeSchema, parsedLocation, parsedStyle))
        .findFirst().orElseThrow(() -> RouterBuilderException.cannotFindParameterProcessorGenerator(pe.getKey(), pe.getValue()));

      try {
        ParameterProcessor generated = generator
          .generate(pe.getValue(), fakeSchema, pe.getKey(), parsedLocation, parsedStyle, context);

        if (!parameterProcessors.containsKey(generated.getLocation()))
          parameterProcessors.put(generated.getLocation(), new ArrayList<>());
        parameterProcessors.get(generated.getLocation()).add(generated);
      } catch (Exception e) {
        throw RouterBuilderException
          .createErrorWhileGeneratingValidationHandler(
            String.format("Cannot generate parameter validator for parameter %s in %s", pe.getKey().toURI(),
              parsedLocation), e
          );
      }
    }

    // Parse body required predicate
    if (parseIsBodyRequired(operation)) context.addPredicate(RequestPredicate.BODY_REQUIRED);

    // Parse body processors
    for (Map.Entry<String, Object> mediaType : (JsonObject) REQUEST_BODY_CONTENT_POINTER.queryOrDefault(operation.getOperationModel(), iteratorWithLoader, new JsonObject())) {
      JsonObject mediaTypeModel = (JsonObject) mediaType.getValue();
      JsonPointer mediaTypePointer = operation.getPointer().copy().append("requestBody").append("content").append(mediaType.getKey());
      BodyProcessor generated = bodyProcessorGenerators.stream()
        .filter(g -> g.canGenerate(mediaType.getKey(), mediaTypeModel))
        .findFirst()
        .orElse(NoopBodyProcessorGenerator.INSTANCE)
        .generate(mediaType.getKey(), mediaTypeModel, mediaTypePointer, context);
      bodyProcessors.add(generated);
    }

    return new ValidationHandlerImpl(parameterProcessors, bodyProcessors, context.getPredicates());
  }

  private boolean parseIsBodyRequired(OperationImpl operation) {
    return (boolean) BODY_REQUIRED_POINTER.queryOrDefault(operation.getOperationModel(), iteratorWithLoader, false);
  }


}
