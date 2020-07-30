package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ValidationException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class BooleanTypeValidator extends SingleValueParameterTypeValidator<Boolean> {

  public BooleanTypeValidator(Boolean defaultValue) {
    super(defaultValue);
  }

  @Override
  public RequestParameter isValidSingleParam(String value) {
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("1"))
      return RequestParameter.create(true);
    else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("0"))
      return RequestParameter.create(false);
    else
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(null);
  }
}
