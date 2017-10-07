package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.*;

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

  public ParameterValidationRuleImpl(String name, ParameterTypeValidator validator, boolean isOptional, boolean
    allowEmptyValue, ParameterLocation location) {
    if (name == null) throw new NullPointerException("name cannot be null");
    this.name = name;
    if (validator == null) throw new NullPointerException("validator cannot be null");
    this.validator = validator;
    this.isOptional = isOptional;
    this.allowEmptyValue = allowEmptyValue;
    this.location = location;
  }

  @Override
  public String getName() {
    return this.name;
  }

  protected RequestParameter callValidator(String value) throws ValidationException {
    try {
      RequestParameter result = validator.isValid(value);
      if (result.getName() == null)
        result.setName(getName());
      return result;
    } catch (ValidationException e) {
      e.setParameterName(this.name);
      e.setValidationRule(this);
      e.setValue(value);
      throw e;
    }
  }

  protected RequestParameter callValidator(List<String> value) throws ValidationException {
    try {
      RequestParameter result = validator.isValidCollection(value);
      if (result.getName() == null)
        result.setName(getName());
      return result;
    } catch (ValidationException e) {
      e.setParameterName(this.name);
      e.setValidationRule(this);
      e.setValue(value.toString());
      throw e;
    }
  }

  @Override
  public RequestParameter validateSingleParam(String value) throws ValidationException {
    if (value != null && value.length() != 0) {
      return callValidator(value);
    } else {
      // Value or null or length == 0
      if (!this.allowEmptyValue)
        throw ValidationException.ValidationExceptionFactory.generateEmptyValueValidationException(this.name, this,
          this.location);
      else return RequestParameter.create(getName(), parameterTypeValidator().getDefault());
    }
  }

  @Override
  public RequestParameter validateArrayParam(List<String> value) throws ValidationException {
    if (value != null && value.size() != 0) {
      return callValidator(value);
    } else {
      // array or null or size == 0
      if (!this.allowEmptyValue)
        throw ValidationException.ValidationExceptionFactory.generateEmptyValueValidationException(this.name, this,
          this.location);
      else return RequestParameter.create(getName(), parameterTypeValidator().getDefault());
    }
  }

  @Override
  public boolean isOptional() {
    return isOptional;
  }

  @Override
  public ParameterTypeValidator parameterTypeValidator() {
    return validator;
  }

  @Override
  public boolean allowEmptyValue() {
    return allowEmptyValue;
  }

  @Override
  public String toString() {
    return "ParameterValidationRuleImpl{" + "name='" + name + '\'' + ", validator=" + validator + ", location=" +
      location + ", isOptional=" + isOptional + ", allowEmptyValue=" + allowEmptyValue + '}';
  }

  public static class ParameterValidationRuleFactory {

    static ParameterValidationRule createValidationRule(String name, ParameterType type, boolean isOptional, boolean
      allowEmptyValue, ParameterLocation location) {
      return new ParameterValidationRuleImpl(name, type.validationMethod(), isOptional, allowEmptyValue, location);
    }

    public static ParameterValidationRule createValidationRuleWithCustomTypeValidator(String name,
                                                                                      ParameterTypeValidator
                                                                                        validator, boolean
                                                                                        isOptional, boolean
                                                                                        allowEmptyValue,
                                                                                      ParameterLocation location) {
      return new ParameterValidationRuleImpl(name, validator, isOptional, allowEmptyValue, location);
    }
  }
}
