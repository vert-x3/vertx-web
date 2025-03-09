package io.vertx.ext.web.validation.impl.parameter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.SchemaRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.vertx.ext.web.validation.ParameterProcessorException.createMissingParameterWhenRequired;
import static io.vertx.ext.web.validation.ParameterProcessorException.createParsingError;
import static io.vertx.ext.web.validation.ParameterProcessorException.createValidationError;

public class ParameterProcessorImpl implements ParameterProcessor, Comparable<ParameterProcessorImpl> {

  private String parameterName;
  private ParameterLocation location;
  private boolean isOptional;
  private ParameterParser parser;
  private SchemaRepository repo;
  private JsonObject schema;
  private String validationErrorMessage;
  private String parsingErrorMessage;

  public ParameterProcessorImpl(String parameterName, ParameterLocation location, boolean isOptional,
                                ParameterParser parser, SchemaRepository repo, JsonObject schema) {
    this.parameterName = parameterName;
    this.location = location;
    this.isOptional = isOptional;
    this.parser = parser;
    this.repo = repo;
    this.schema = schema;
  }

  @Override
  public Future<RequestParameter> process(Map<String, List<String>> params) {
    Object json;
    try {
      json = parser.parseParameter(params);
    } catch (MalformedValueException e) {
      throw createParsingError(parameterName, location, e, parsingErrorMessage);
    }
    if (json != null)
      return Future.<RequestParameter>future(p -> {
        OutputUnit result = repo.validator(JsonSchema.of(schema)).validate(json);
        if (result.getValid()) {
          p.complete(RequestParameter.create(json));
        } else {
          p.fail(result.toException(""));
        }
      }).recover(t -> Future.failedFuture(createValidationError(parameterName, location, t, validationErrorMessage)));
    else if (!isOptional)
      throw createMissingParameterWhenRequired(parameterName, location);
    else {
      RequestParameter defaultValue =
        Optional.ofNullable(schema.getValue("default")).map(RequestParameter::create).orElse(null);
      return Future.succeededFuture(defaultValue);
    }
  }

  @Override
  public String getName() {
    return parameterName;
  }

  @Override
  public ParameterLocation getLocation() {
    return location;
  }

  @Override
  public ParameterProcessor validationErrorMessage(String message) {
    this.validationErrorMessage = message;
    return this;
  }

  @Override
  public ParameterProcessor parsingErrorMessage(String message) {
    this.parsingErrorMessage = message;
    return this;
  }

  @Override
  public int compareTo(ParameterProcessorImpl o) {
    return parser.compareTo(o.parser);
  }
}
