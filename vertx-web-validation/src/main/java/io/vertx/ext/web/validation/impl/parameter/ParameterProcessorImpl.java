package io.vertx.ext.web.validation.impl.parameter;

import io.vertx.core.Future;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.validator.ValueValidator;

import java.util.List;
import java.util.Map;

import static io.vertx.ext.web.validation.ParameterProcessorException.*;

public class ParameterProcessorImpl implements ParameterProcessor, Comparable<ParameterProcessorImpl> {

  private final String parameterName;
  private final ParameterLocation location;
  private final boolean isOptional;
  private final ParameterParser parser;
  private final ValueValidator validator;

  public ParameterProcessorImpl(String parameterName, ParameterLocation location, boolean isOptional, ParameterParser parser, ValueValidator validator) {
    this.parameterName = parameterName;
    this.location = location;
    this.isOptional = isOptional;
    this.parser = parser;
    this.validator = validator;
  }

  @Override
  public Future<RequestParameter> process(Map<String, List<String>> params) {
    Object json;
    try {
      json = parser.parseParameter(params);
    } catch (MalformedValueException e) {
      throw createParsingError(parameterName, location, e);
    }
    if (json != null)
      return validator.validate(json).recover(t -> Future.failedFuture(createValidationError(parameterName, location, t)));
    else if (!isOptional)
      throw createMissingParameterWhenRequired(parameterName, location);
    else {
      return validator.getDefault().map(defaultValue -> null != defaultValue ? RequestParameter.create(defaultValue) : null);
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
  public int compareTo(ParameterProcessorImpl o) {
    return parser.compareTo(o.parser);
  }
}
