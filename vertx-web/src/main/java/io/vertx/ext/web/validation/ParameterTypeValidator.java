package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.validation.impl.ArrayTypeValidator;
import io.vertx.ext.web.validation.impl.NumericTypeValidator;
import io.vertx.ext.web.validation.impl.StringEnumTypeValidator;
import io.vertx.ext.web.validation.impl.StringTypeValidator;

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
  boolean isValid(String value);

  /**
   * Function that check if parameter is valid
   *
   * @param value value of parameter to test, Note: if this function is called, the parameter is actually encoded
   * @return true if parameter is valid
   */
  default boolean isValidEncoded(String value) {
    return this.isValid(Utils.urlDecode(value, false));
  }

  static ParameterTypeValidator createIntegerTypeValidator() {
    return new NumericTypeValidator<Integer>(NumericTypeValidator.parseInteger);
  }

  static ParameterTypeValidator createIntegerTypeValidator(Double maximum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Integer>(NumericTypeValidator.parseInteger, maximum, minimum, multipleOf);
  }

  static ParameterTypeValidator createIntegerTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Integer>(NumericTypeValidator.parseInteger, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf);
  }

  static ParameterTypeValidator createLongTypeValidator() {
    return new NumericTypeValidator<Long>(NumericTypeValidator.parseLong);
  }

  static ParameterTypeValidator createLongTypeValidator(Double maximum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Long>(NumericTypeValidator.parseLong, maximum, minimum, multipleOf);
  }

  static ParameterTypeValidator createLongTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Long>(NumericTypeValidator.parseLong, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf);
  }

  static ParameterTypeValidator createFloatTypeValidator() {
    return new NumericTypeValidator<Float>(NumericTypeValidator.parseFloat);
  }

  static ParameterTypeValidator createFloatTypeValidator(Double maximum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Float>(NumericTypeValidator.parseFloat, maximum, minimum, multipleOf);
  }

  static ParameterTypeValidator createFloatTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Float>(NumericTypeValidator.parseFloat, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf);
  }

  static ParameterTypeValidator createDoubleTypeValidator() {
    return new NumericTypeValidator<Double>(NumericTypeValidator.parseDouble);
  }

  static ParameterTypeValidator createDoubleTypeValidator(Double maximum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Double>(NumericTypeValidator.parseDouble, maximum, minimum, multipleOf);
  }

  static ParameterTypeValidator createDoubleTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf) {
    return new NumericTypeValidator<Double>(NumericTypeValidator.parseDouble, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf);
  }

  static ParameterTypeValidator createStringTypeValidator(String pattern) {
    return new StringTypeValidator(pattern);
  }

  static ParameterTypeValidator createStringTypeValidator(String pattern, Integer minLength, Integer maxLength) {
    return new StringTypeValidator(pattern, minLength, maxLength);
  }

  static ParameterTypeValidator createEnumTypeValidator(List<String> allowedValues) {
    return new StringEnumTypeValidator(allowedValues);
  }

  /**
   * @param arrayMembersValidator
   * @return An ArrayTypeValidator with explode collection format
   */
  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator) {
    return new ArrayTypeValidator(arrayMembersValidator);
  }

  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator, String collectionFormat, Integer maxItems, Integer minItems) {
    return new ArrayTypeValidator(arrayMembersValidator, collectionFormat, maxItems, minItems);
  }

}
