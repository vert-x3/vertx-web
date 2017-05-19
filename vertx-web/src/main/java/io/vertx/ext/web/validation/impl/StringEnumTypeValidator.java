package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterTypeValidator;

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
  public boolean isValid(String value) {
    return allowedValues.contains(value);
  }
}
