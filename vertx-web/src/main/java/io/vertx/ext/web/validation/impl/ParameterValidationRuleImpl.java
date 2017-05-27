package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ParameterValidationRuleImpl implements ParameterValidationRule {

  private String name;
  ParameterTypeValidator validator;
  private ParameterLocation location;

  private boolean isOptional;
  private boolean allowEmptyValue;

  public ParameterValidationRuleImpl(String name, ParameterTypeValidator validator, boolean isOptional, boolean allowEmptyValue, ParameterLocation location) {
    if (name == null)
      throw new NullPointerException("name cannot be null");
    this.name = name;
    if (validator == null)
      throw new NullPointerException("validator cannot be null");
    this.validator = validator;
    this.isOptional = isOptional;
    this.allowEmptyValue = allowEmptyValue;
    this.location = location;
  }

  @Override
  public String getName() {
    return this.name;
  }

  private void callValidator(String value) throws ValidationException {
    try {
      validator.isValid(value);
    } catch (ValidationException e) {
      e.setParameterName(this.name);
      e.setValidationRule(this);
      e.setValue(value);
      throw e;
    }
  }

  private void callValidator(List<String> value) throws ValidationException {
    try {
      validator.isValidCollection(value);
    } catch (ValidationException e) {
      e.setParameterName(this.name);
      e.setValidationRule(this);
      e.setValue(value.toString());
      throw e;
    }
  }

  @Override
  public void validateSingleParam(String value) throws ValidationException {
    if (value != null && value.length() != 0) {
      callValidator(value);
    } else {
      // Value or null or length == 0
      if (!this.allowEmptyValue)
        throw ValidationException.generateEmptyValueValidationException(this.name, this, this.location);
    }
  }

  @Override
  public void validateArrayParam(List<String> value) throws ValidationException {
    if (value != null && value.size() != 0) {
      callValidator(value);
    } else {
      // array or null or size == 0
      if (!this.allowEmptyValue)
        throw ValidationException.generateEmptyValueValidationException(this.name, this, this.location);
    }
  }

  @Override
  public boolean isOptional() {
    return isOptional;
  }

  @Override
  public ParameterTypeValidator getParameterTypeValidator() {
    return validator;
  }

  @Override
  public String toString() {
    return "ParameterValidationRuleImpl{" +
      "name='" + name + '\'' +
      ", validator=" + validator +
      ", location=" + location +
      ", isOptional=" + isOptional +
      ", allowEmptyValue=" + allowEmptyValue +
      '}';
  }
}
