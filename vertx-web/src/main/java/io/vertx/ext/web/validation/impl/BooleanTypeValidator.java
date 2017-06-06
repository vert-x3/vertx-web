package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ValidationException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class BooleanTypeValidator implements ParameterTypeValidator {

  Boolean defaultValue;

  public BooleanTypeValidator(Boolean defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public RequestParameter isValid(String value) {
    if (value == null || value.length() == 0)
      return RequestParameter.create(getDefault());
    else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("1"))
      return RequestParameter.create(Boolean.valueOf(true));
    else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("0"))
      return RequestParameter.create(Boolean.valueOf(false));
    else
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(null);
  }

  @Override
  public Object getDefault() {
    return defaultValue;
  }
}
