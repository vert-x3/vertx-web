package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.validation.impl.*;

import java.util.List;

/**
 * Interface for declaration of method for validate a specific parameter type.<br/>
 * If you want to implement your own type validator, you need only to implement
 * {@link ParameterTypeValidator#isValid(String)}:
 * <ul>
 * <li>If parameter is valid, call {@link RequestParameter#create(Object)} to put value (maybe modified) inside
 * RequestParameters. The validation flow will care about set parameter name</li>
 * <li>If parameter is invalid, throw a new ValidationException with message
 * ({@link ValidationException#ValidationException(String)}) and/or with
 * {@link io.vertx.ext.web.api.validation.ValidationException.ErrorType}
 * ({@link ValidationException#ValidationException(String, ValidationException.ErrorType)}). As for value, the
 * validation flow will care about setting all other ValidationException fields</li>
 * </ul>
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen @FunctionalInterface
public interface ParameterTypeValidator {
  /**
   * Function that checks if parameter is valid. It returns a RequestParameter object that will be linked inside
   * {@link RequestParameters}. For more info, check {@link RequestParameter}.
   *
   * @param value value of parameter to test
   * @return request parameter value
   */
  RequestParameter isValid(String value) throws ValidationException;

  /**
   * Function that checks if array of values of a specific parameter. It returns a RequestParameter object that will
   * be linked inside {@link RequestParameters}. For more info, check {@link RequestParameter}.
   *
   * @param value list of values of parameter to test
   * @return request parameter value
   */
  default RequestParameter isValidCollection(List<String> value) throws ValidationException {
    if (value.size() > 1 && !(this instanceof ArrayTypeValidator || this instanceof ObjectTypeValidator))
      throw ValidationException.ValidationExceptionFactory.generateUnexpectedArrayValidationException(null, null);
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

  /**
   * Returns true if this type validator has default value
   *
   * @return
   */
  default boolean hasDefault() { return getDefault() != null; }

  /* --- Factory methods for built-in type validators --- */

  /**
   * Create a new type validator for integer values
   *
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createIntegerTypeValidator(Integer defaultValue) {
    return createIntegerTypeValidator(null, null, null, defaultValue);
  }

  /**
   * Create a new type validator for integer values
   *
   * @param maximum      Maximum value. It can be null
   * @param minimum      Minimum value. It can be null
   * @param multipleOf   Multiple of value. It can be null
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createIntegerTypeValidator(Double maximum, Double minimum, Double multipleOf, Integer
    defaultValue) {
    return createIntegerTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new type validator for integer values
   *
   * @param exclusiveMaximum If true, value will be mark as valid if value < maximum. If false, value will be mark as
   *                        valid if value <= maximum. The default value is false. It can be null
   * @param maximum          Maximum value. It can be null
   * @param exclusiveMinimum If true, value will be mark as valid if value > minimum. If false, value will be mark as
   *                        valid if value >= minimum. The default value is false. It can be null
   * @param minimum          Minimum value. It can be null
   * @param multipleOf       Multiple of value. It can be null
   * @param defaultValue     Default value that will be set if parameter is empty or not found. To apply default
   *                         value you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be
   *                         null
   * @return
   */
  static ParameterTypeValidator createIntegerTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean
    exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    return new NumericTypeValidator(Integer.class, exclusiveMaximum, maximum,
        exclusiveMinimum, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new type validator for long integer values
   *
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createLongTypeValidator(Long defaultValue) {
    return createLongTypeValidator(null, null, null, defaultValue);
  }

  /**
   * Create a new type validator for long integer values
   *
   * @param maximum      Maximum value. It can be null
   * @param minimum      Minimum value. It can be null
   * @param multipleOf   Multiple of value. It can be null
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createLongTypeValidator(Double maximum, Double minimum, Double multipleOf, Long
    defaultValue) {
    return createLongTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new type validator for long integer values
   *
   * @param exclusiveMaximum If true, value will be mark as valid if value < maximum. If false, value will be mark as
   *                        valid if value <= maximum. The default value is false. It can be null
   * @param maximum          Maximum value. It can be null
   * @param exclusiveMinimum If true, value will be mark as valid if value > minimum. If false, value will be mark as
   *                        valid if value >= minimum. The default value is false. It can be null
   * @param minimum          Minimum value. It can be null
   * @param multipleOf       Multiple of value. It can be null
   * @param defaultValue     Default value that will be set if parameter is empty or not found. To apply default
   *                         value you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be
   *                         null
   * @return
   */
  static ParameterTypeValidator createLongTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean
    exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    return new NumericTypeValidator(Long.class, exclusiveMaximum, maximum,
        exclusiveMinimum, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new type validator for float values
   *
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createFloatTypeValidator(Float defaultValue) {
    return createFloatTypeValidator(null, null, null, defaultValue);
  }

  /**
   * Create a new type validator for float values
   *
   * @param maximum      Maximum value. It can be null
   * @param minimum      Minimum value. It can be null
   * @param multipleOf   Multiple of value. It can be null
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createFloatTypeValidator(Double maximum, Double minimum, Double multipleOf, Float
    defaultValue) {
    return createFloatTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new type validator for float values
   *
   * @param exclusiveMaximum If true, value will be mark as valid if value < maximum. If false, value will be mark as
   *                        valid if value <= maximum. The default value is false. It can be null
   * @param maximum          Maximum value. It can be null
   * @param exclusiveMinimum If true, value will be mark as valid if value > minimum. If false, value will be mark as
   *                        valid if value >= minimum. The default value is false. It can be null
   * @param minimum          Minimum value. It can be null
   * @param multipleOf       Multiple of value. It can be null
   * @param defaultValue     Default value that will be set if parameter is empty or not found. To apply default
   *                         value you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be
   *                         null
   * @return
   */
  static ParameterTypeValidator createFloatTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean
    exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    return new NumericTypeValidator(Float.class, exclusiveMaximum, maximum,
        exclusiveMinimum, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new type validator for double values
   *
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createDoubleTypeValidator(Double defaultValue) {
    return createDoubleTypeValidator(null, null, null, defaultValue);
  }

  /**
   * Create a new type validator for double values
   *
   * @param maximum      Maximum value. It can be null
   * @param minimum      Minimum value. It can be null
   * @param multipleOf   Multiple of value. It can be null
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createDoubleTypeValidator(Double maximum, Double minimum, Double multipleOf, Double
    defaultValue) {
    return createDoubleTypeValidator(false, maximum, false, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new type validator for double values
   *
   * @param exclusiveMaximum If true, value will be mark as valid if value < maximum. If false, value will be mark as
   *                        valid if value <= maximum. The default value is false. It can be null
   * @param maximum          Maximum value. It can be null
   * @param exclusiveMinimum If true, value will be mark as valid if value > minimum. If false, value will be mark as
   *                        valid if value >= minimum. The default value is false. It can be null
   * @param minimum          Minimum value. It can be null
   * @param multipleOf       Multiple of value. It can be null
   * @param defaultValue     Default value that will be set if parameter is empty or not found. To apply default
   *                         value you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be
   *                         null
   * @return
   */
  static ParameterTypeValidator createDoubleTypeValidator(Boolean exclusiveMaximum, Double maximum, Boolean
    exclusiveMinimum, Double minimum, Double multipleOf, Object defaultValue) {
    return new NumericTypeValidator(Double.class, exclusiveMaximum, maximum,
        exclusiveMinimum, minimum, multipleOf, defaultValue);
  }

  /**
   * Create a new string type validator
   *
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createStringTypeValidator(Object defaultValue) {
    return createStringTypeValidator(null, null, null, defaultValue);
  }

  /**
   * Create a new string type validator
   *
   * @param pattern      pattern that string have to match. It can be null
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createStringTypeValidator(String pattern, Object defaultValue) {
    return createStringTypeValidator(pattern, null, null, defaultValue);
  }

  /**
   * Create a new string type validator
   *
   * @param pattern      pattern that string have to match. It can be null
   * @param minLength    Minimum length of string. It can be null
   * @param maxLength    Maximum length of string. It can be null
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createStringTypeValidator(String pattern, Integer minLength, Integer maxLength,
                                                          Object defaultValue) {
    return new StringTypeValidator(pattern, minLength, maxLength, (String) defaultValue);
  }

  /**
   * Create a boolean type validator
   *
   * @param defaultValue Default value that will be set if parameter is empty or not found. To apply default value
   *                     you have to set {@link ParameterValidationRule#allowEmptyValue()} true. It can be null
   * @return
   */
  static ParameterTypeValidator createBooleanTypeValidator(Object defaultValue) {
    if (defaultValue != null) {
      if (defaultValue instanceof String) return new BooleanTypeValidator(Boolean.valueOf((String) defaultValue));
      else return new BooleanTypeValidator((Boolean) defaultValue);
    } else {
      return new BooleanTypeValidator(null);
    }
  }

  /**
   * Create an enum type validator
   *
   * @param allowedValues allowed values. It <b>can't be</b> null
   * @return
   */
  static ParameterTypeValidator createStringEnumTypeValidator(List<String> allowedValues) {
    return new EnumTypeValidator(allowedValues, ParameterType.GENERIC_STRING.validationMethod());
  }

  /**
   * Create an enum type validator
   *
   * @param allowedValues  allowed values. It <b>can't be</b> null
   * @param innerValidator After check if value is one of the lists, you can pass the value to an inner validator. It
   *                      can be null
   * @return
   */
  static ParameterTypeValidator createEnumTypeValidatorWithInnerValidator(List<String> allowedValues,
                                                                          ParameterTypeValidator innerValidator) {
    return new EnumTypeValidator(allowedValues, innerValidator);
  }

  /**
   * Create an array type validator
   *
   * @param arrayMembersValidator Type validator that describe array items. It <b>can't be</b> null
   * @return
   */
  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator) {
    return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator);
  }

  /**
   * Create an array type validator
   *
   * @param arrayMembersValidator Type validator that describe array items. It <b>can't be</b> null
   * @param collectionFormat      String that represent a ContainerSerializationStyle. Check out
   * {@link ContainerSerializationStyle} for more informations. The default value is "csv". It can be null
   * @param maxItems              Maximum items in array. It can be null
   * @param minItems              Minimum items in array. It can be null
   * @return
   */
  static ParameterTypeValidator createArrayTypeValidator(ParameterTypeValidator arrayMembersValidator, String
    collectionFormat, Integer maxItems, Integer minItems) {
    return ArrayTypeValidator.ArrayTypeValidatorFactory.createArrayTypeValidator(arrayMembersValidator,
      collectionFormat, maxItems, minItems);
  }

}
