package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class SingleValueParameterTypeValidator<T> implements ParameterTypeValidator {
  public T defaultValue;

  public SingleValueParameterTypeValidator(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  public abstract RequestParameter isValidSingleParam(String value);

  @Override
  public RequestParameter isValid(String value) {
    if (value != null) {
      return isValidSingleParam(value);
    } else
    if (this.hasDefault())
      return RequestParameter.create(getDefault());
    else
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(null);
  }

  @Override
  public RequestParameter isValidCollection(List<String> value) throws ValidationException {
    if (value.size() > 1)
      throw ValidationException.ValidationExceptionFactory.generateUnexpectedArrayValidationException(null, null);
    return this.isValid(value.get(0));
  }

  @Override
  public T getDefault() {
    return defaultValue;
  }
}
