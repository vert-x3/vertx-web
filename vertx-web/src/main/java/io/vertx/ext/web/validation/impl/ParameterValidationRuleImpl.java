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

  // Array params, this params are used ONLY IF array collectionformat is multi, otherwise it will be handled by the validator
  private boolean multiArray; // If parameter is a multi array, this object have to manually loop inside it, otherwise, call validator function
  private Integer maxItems;
  private Integer minItems;

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

    // Multi array construction routine
    if (validator instanceof ArrayTypeValidator) {
      ArrayTypeValidator arrayValidator = (ArrayTypeValidator) validator;
      if (arrayValidator.getCollectionFormat().equals(ArrayTypeValidator.CollectionsSplitters.multi)) {
        this.multiArray = true;
        this.validator = arrayValidator.getInnerValidator();
        this.minItems = arrayValidator.getMinItems();
        this.maxItems = arrayValidator.getMaxItems();
      } else {
        this.multiArray = false;
      }
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  private void callValidator(String value) throws ValidationException {
    if (!validator.isValid(value))
      throw ValidationException.generateNotMatchValidationException(this.name, value, this, this.location);
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
      if (this.multiArray) {
        if (checkMaxItems(value.size()) && checkMinItems(value.size())) {
          for (String s : value) {
            callValidator(s);
          }
        } else {
          throw ValidationException.generateUnexpectedArraySizeValidationException(this.name, this.maxItems, this.minItems, value.size(), this, this.location);
        }
      } else {
        validateSingleParam(value.get(0));
      }
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
  public boolean isMultiArray() {
    return multiArray;
  }

  @Override
  public String toString() {
    return "ParameterValidationRuleImpl{" +
      "name='" + name + '\'' +
      ", validator=" + validator +
      ", location=" + location +
      ", isOptional=" + isOptional +
      ", allowEmptyValue=" + allowEmptyValue +
      ", multiArray=" + multiArray +
      ", maxItems=" + maxItems +
      ", minItems=" + minItems +
      '}';
  }
}
