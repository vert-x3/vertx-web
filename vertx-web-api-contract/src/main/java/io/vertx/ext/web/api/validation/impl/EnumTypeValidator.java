package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class EnumTypeValidator extends SingleValueParameterTypeValidator<Object> {

  private List<String> allowedValues;

  public EnumTypeValidator(List<String> allowedValues, ParameterTypeValidator innerValidator) {
    super((innerValidator != null) ? innerValidator.getDefault() : null);
    this.allowedValues = allowedValues;
    if (innerValidator != null) {
      for (String value : this.allowedValues) {
        try {
          innerValidator.isValid(value);
        } catch (ValidationException e) {
          throw new IllegalArgumentException("Value " + value + "of enum is invalid" + e.getMessage());
        }
      }
    }
  }

  @Override
  public RequestParameter isValidSingleParam(String value) {
    if (!allowedValues.contains(value))
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException("Value " + value + " "
        + "in not inside enum list " + allowedValues.toString());
    else return RequestParameter.create(value);
  }
}
