package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ValidationException;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class StringEnumTypeValidator implements ParameterTypeValidator {

  private List<String> allowedValues;

  public StringEnumTypeValidator(List<String> allowedValues) {
    this.allowedValues = allowedValues;
  }

  @Override
  public void isValid(String value) {
    if (!allowedValues.contains(value))
      throw ValidationException.generateNotMatchValidationException("Value " + value + " in not inside enum list " + allowedValues.toString());
  }
}
