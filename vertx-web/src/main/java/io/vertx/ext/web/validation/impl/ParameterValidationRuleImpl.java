package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ParameterValidationRule;
import io.vertx.ext.web.validation.ValidationException;

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

  // Default Values
  private String defaultValue; //TODO what to do with default value?
  private List<String> defaultArrayValue;

  // Array params
  private boolean expectedArray;
  private Integer maxItems;
  private Integer minItems;

  // Only for internal construction of object
  private ParameterValidationRuleImpl(String name, ParameterTypeValidator validator, boolean isOptional, boolean expectedArray, String defaultValue, List<String> defaultArrayValue, boolean allowEmptyValue, Integer maxItems, Integer minItems, ParameterLocation location) {
    this.name = name;
    this.validator = validator;
    this.isOptional = isOptional;
    this.expectedArray = expectedArray;
    this.defaultValue = defaultValue;
    this.defaultArrayValue = defaultArrayValue;
    this.allowEmptyValue = allowEmptyValue;
    this.maxItems = maxItems;
    this.minItems = minItems;
    this.location = location;
  }

  // For single parameter rule construction
  public ParameterValidationRuleImpl(String name, ParameterTypeValidator validator, boolean isOptional, String defaultValue, boolean allowEmptyValue, ParameterLocation location) {
    this(name, validator, isOptional, false, defaultValue, null, allowEmptyValue, null, null, location);
  }

  // For array parameter rule construction
  public ParameterValidationRuleImpl(String name, ParameterTypeValidator validator, boolean isOptional, List<String> defaultArrayValue, boolean allowEmptyValue, Integer maxItems, Integer minItems, ParameterLocation location) {
    this(name, validator, isOptional, true, null, defaultArrayValue, allowEmptyValue, maxItems, minItems, location);
  }

  @Override
  public String getName() {
    return this.name;
  }

  private void callValidator(String value) throws ValidationException {
    if (!validator.isValid(value))
      throw ValidationException.generateNotMatchValidationException(this.name, value, this, this.location);
  }

  @Override
  public void validateSingleParam(String value) throws ValidationException {
    if (value != null && value.length() != 0) {
      // Value not null with size > 0
      if (this.expectedArray) {
        throw ValidationException.generateUnexpectedSingleStringValidationException(this.getName(), this);
      } else {
        callValidator(value);
      }
    } else {
      // Value or null or length == 0
      if (!this.allowEmptyValue)
        throw ValidationException.generateEmptyValueValidationException(this.name, this, this.location);
      //TODO applydefaultvalue?!
    }
  }

  private boolean checkMinItems(int size) {
    if (minItems != null)
      return size >= minItems;
    else return true;
  }

  private boolean checkMaxItems(int size) {
    if (maxItems != null)
      return size <= maxItems;
    else return true;
  }

  @Override
  public void validateArrayParam(List<String> value) throws ValidationException {
    if (value != null && value.size() != 0) {
      // array not null with size > 0
      if (!this.expectedArray && value.size() > 1)
        throw ValidationException.generateUnexpectedArrayValidationException(this.name, this);
      else {
        if (checkMaxItems(value.size()) && checkMinItems(value.size())) {
          for (String s : value) {
            callValidator(s);
          }
        } else {
          throw ValidationException.generateUnexpectedArraySizeValidationException(this.name, this.maxItems, this.minItems, value.size(), this, this.location);
        }
      }
    } else {
      // array or null or size == 0
      if (!this.allowEmptyValue)
        throw ValidationException.generateEmptyValueValidationException(this.name, this, this.location);
      //TODO applydefaultvalue?!
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
      ", defaultValue='" + defaultValue + '\'' +
      ", defaultArrayValue=" + defaultArrayValue +
      ", expectedArray=" + expectedArray +
      ", maxItems=" + maxItems +
      ", minItems=" + minItems +
      '}';
  }
}
