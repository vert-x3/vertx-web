package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ValidationException;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class EnumTypeValidator implements ParameterTypeValidator {

  private List<String> allowedValues;
  private ParameterTypeValidator innerValidator;

  public EnumTypeValidator(List<String> allowedValues, ParameterTypeValidator innerValidator) {
    this.allowedValues = allowedValues;
    this.innerValidator = innerValidator;
  }

  @Override
  public RequestParameter isValid(String value) {
    if (!allowedValues.contains(value))
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException("Value " + value + " in not inside enum list " + allowedValues.toString());
    if (this.innerValidator != null)
      return this.innerValidator.isValid(value);
    else
      return RequestParameter.create(value);
  }

  @Override
  public Object getDefault() {
    return this.innerValidator.getDefault();
  }
}
