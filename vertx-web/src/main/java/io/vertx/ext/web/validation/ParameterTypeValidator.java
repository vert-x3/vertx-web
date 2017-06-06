package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.validation.impl.*;

import java.util.List;

/**
 * Interface for declaration of method for validate a specific parameter type.<br/>
 * If you want to implement your own type validator, you need only to implement {@link ParameterTypeValidator#isValid(String)}:
 * <ul>
 *     <li>If parameter is valid, call {@link RequestParameter#create(Object)} to put value (maybe modified) inside RequestParameters. The validation flow will care about set parameter name</li>
 *     <li>If parameter is invalid, throw a new ValidationException with message ({@link ValidationException#ValidationException(String)}) and/or with {@link io.vertx.ext.web.validation.ValidationException.ErrorType} ({@link ValidationException#ValidationException(String, ValidationException.ErrorType)}). As for value, the validation flow will care about setting all other ValidationException fields</li>
 * </ul>
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface ParameterTypeValidator {
  /**
   * Function that checks if parameter is valid. It returns a RequestParameter object that will be linked inside {@link io.vertx.ext.web.RequestParameters}. For more info, check {@link RequestParameter}.
   * @param value value of parameter to test
   * @return request parameter value
   */
  RequestParameter isValid(String value) throws ValidationException;

  /**
   * Function that checks if array of values of a specific parameter. It returns a RequestParameter object that will be linked inside {@link io.vertx.ext.web.RequestParameters}. For more info, check {@link RequestParameter}.
   * @param value list of values of parameter to test
   * @return request parameter value
   */
  default RequestParameter isValidCollection(List<String> value) throws ValidationException {
    return this.isValid(value.get(0));
  }

  /**
   * Returns default value of parameter
   *
   * @return
   */
  default Object getDefault() {
    return null;
  }

  /* --- Factory methods for built-in type validators --- */

  //TODO javadoc
  static ParameterTypeValidator createIntegerTypeValidator(Integer defaultValue) {
    return createIntegerTypeValidator(null, null, null, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createIntegerTypeValidator(Double maximum, Double minimum, Double multipleOf, Integer defaultValue) {
    return createIntegerTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createIntegerTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Integer>(NumericTypeValidator.parseInteger, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Integer.parseInt((String) defaultValue));
    else
      return new NumericTypeValidator<Integer>(NumericTypeValidator.parseInteger, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Integer) defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createLongTypeValidator(Long defaultValue) {
    return createLongTypeValidator(null, null, null, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createLongTypeValidator(Double maximum, Double minimum, Double multipleOf, Long defaultValue) {
    return createLongTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createLongTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Long>(NumericTypeValidator.parseLong, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Long.parseLong((String) defaultValue));
    else
      return new NumericTypeValidator<Long>(NumericTypeValidator.parseLong, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Long) defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createFloatTypeValidator(Float defaultValue) {
    return createFloatTypeValidator(null, null, null, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createFloatTypeValidator(Double maximum, Double minimum, Double multipleOf, Float defaultValue) {
    return createFloatTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createFloatTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Float>(NumericTypeValidator.parseFloat, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Float.parseFloat((String) defaultValue));
    else
      return new NumericTypeValidator<Float>(NumericTypeValidator.parseFloat, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Float) defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createDoubleTypeValidator(Double defaultValue) {
    return createDoubleTypeValidator(null, null, null, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createDoubleTypeValidator(Double maximum, Double minimum, Double multipleOf, Double defaultValue) {
    return createDoubleTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createDoubleTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    if (defaultValue instanceof String)
      return new NumericTypeValidator<Double>(NumericTypeValidator.parseDouble, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, Double.parseDouble((String) defaultValue));
    else
      return new NumericTypeValidator<Double>(NumericTypeValidator.parseDouble, exclusiveMaximum, maximum, exclusiveMinimum, minimum, multipleOf, (Double) defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createStringTypeValidator(String pattern) {
    return new StringTypeValidator(pattern);
  }

  //TODO javadoc
  static ParameterTypeValidator createStringTypeValidator(String pattern, Integer minLength, Integer maxLength, Object defaultValue) {
    return new StringTypeValidator(pattern, minLength, maxLength, (String) defaultValue);
  }

  //TODO javadoc
  static ParameterTypeValidator createEnumTypeValidator(List<String> allowedValues, ParameterTypeValidator innerValidator) {
    return new EnumTypeValidator(allowedValues, innerValidator);
  }

  //TODO javadoc
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

  //TODO javadoc
  static ParameterTypeValidator createStringEnumTypeValidator(List<String> allowedValues) {
    return new EnumTypeValidator(allowedValues, ParameterType.GENERIC_STRING.getValidationMethod());
  }

  //TODO javadoc
  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator) {
    return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator);
  }

  //TODO javadoc
  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator, String collectionFormat, Integer maxItems, Integer minItems) {
    return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator, collectionFormat, maxItems, minItems);
  }

}
