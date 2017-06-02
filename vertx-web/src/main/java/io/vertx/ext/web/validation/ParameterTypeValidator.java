package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.validation.impl.*;
import jnr.ffi.annotations.In;

import java.util.List;

/**
 * Interface for declaration of method for validate a specific parameter type
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface ParameterTypeValidator {
  /**
   * Function that check if parameter is valid
   * @param value value of parameter to test
   * @return true if parameter is valid
   */
  RequestParameter isValid(String value) throws ValidationException;

  /**
   * Function that check if array of values of a specific parameter
   *
   * @param value value of parameter to test
   * @return true if parameter is valid
   */
  default RequestParameter isValidCollection(List<String> value) throws ValidationException {
    return this.isValid(value.get(0));
  }

  default Object getDefault() {
    return null;
  }

  static ParameterTypeValidator createIntegerTypeValidator(Integer defaultValue) {
    return createIntegerTypeValidator(null, null, null, defaultValue);
  }

  static ParameterTypeValidator createIntegerTypeValidator(Double maximum, Double minimum, Double multipleOf, Integer defaultValue) {
    return createIntegerTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  static ParameterTypeValidator createIntegerTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Integer>(NumericTypeValidator.parseInteger, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Integer.parseInt((String) defaultValue));
    else
      return new NumericTypeValidator<Integer>(NumericTypeValidator.parseInteger, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Integer) defaultValue);
  }

  static ParameterTypeValidator createLongTypeValidator(Long defaultValue) {
    return createLongTypeValidator(null, null, null, defaultValue);
  }

  static ParameterTypeValidator createLongTypeValidator(Double maximum, Double minimum, Double multipleOf, Long defaultValue) {
    return createLongTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  static ParameterTypeValidator createLongTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Long>(NumericTypeValidator.parseLong, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Long.parseLong((String) defaultValue));
    else
      return new NumericTypeValidator<Long>(NumericTypeValidator.parseLong, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Long) defaultValue);
  }

  static ParameterTypeValidator createFloatTypeValidator(Float defaultValue) {
    return createFloatTypeValidator(null, null, null, defaultValue);
  }

  static ParameterTypeValidator createFloatTypeValidator(Double maximum, Double minimum, Double multipleOf, Float defaultValue) {
    return createFloatTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  static ParameterTypeValidator createFloatTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Float>(NumericTypeValidator.parseFloat, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Float.parseFloat((String) defaultValue));
    else
      return new NumericTypeValidator<Float>(NumericTypeValidator.parseFloat, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Float) defaultValue);
  }

  static ParameterTypeValidator createDoubleTypeValidator(Double defaultValue) {
    return createDoubleTypeValidator(null, null, null, defaultValue);
  }

  static ParameterTypeValidator createDoubleTypeValidator(Double maximum, Double minimum, Double multipleOf, Double defaultValue) {
    return createDoubleTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  static ParameterTypeValidator createDoubleTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Double>(NumericTypeValidator.parseDouble, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Double.parseDouble((String) defaultValue));
    else
      return new NumericTypeValidator<Double>(NumericTypeValidator.parseDouble, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Double) defaultValue);
  }

  static ParameterTypeValidator createStringTypeValidator(String pattern) {
    return new StringTypeValidator(pattern);
  }

  static ParameterTypeValidator createStringTypeValidator(String pattern, Integer minLength, Integer maxLength, Object defaultValue) {
    return new StringTypeValidator(pattern, minLength, maxLength, (String) defaultValue);
  }

  static ParameterTypeValidator createEnumTypeValidator(List<String> allowedValues, ParameterTypeValidator innerValidator) {
    return new EnumTypeValidator(allowedValues, innerValidator);
  }

  static ParameterTypeValidator createBooleanTypeValidator(Object defaultValue) {
    if (defaultValue != null) {
      if (defaultValue instanceof String)
        return new BooleanTypeValidator(Boolean.valueOf((String) defaultValue));
      else
        return new BooleanTypeValidator(Boolean.valueOf((Boolean) defaultValue));
    } else {
      return new BooleanTypeValidator(null);
    }
  }

  static ParameterTypeValidator createStringEnumTypeValidator(List<String> allowedValues) {
    return new EnumTypeValidator(allowedValues, ParameterType.GENERIC_STRING.getValidationMethod());
  }

  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator) {
    return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator);
  }

  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator, String collectionFormat, Integer maxItems, Integer minItems) {
    return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator, collectionFormat, maxItems, minItems);
  }

}
