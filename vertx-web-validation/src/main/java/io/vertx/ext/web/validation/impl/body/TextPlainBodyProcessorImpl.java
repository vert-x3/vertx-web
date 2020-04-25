package io.vertx.ext.web.validation.impl.body;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.impl.validator.ValueValidator;

public class TextPlainBodyProcessorImpl implements BodyProcessor {

  ValueValidator valueValidator;

  public TextPlainBodyProcessorImpl(ValueValidator valueValidator) {
    this.valueValidator = valueValidator;
  }

  @Override
  public boolean canProcess(String contentType) {
    return contentType.contains("text/plain");
  }

  @Override
  public Future<RequestParameter> process(RoutingContext requestContext) {
    return valueValidator.validate(requestContext.getBodyAsString());
  }
}
