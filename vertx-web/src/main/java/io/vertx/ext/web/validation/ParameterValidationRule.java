package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.ParameterValidationRuleImpl;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface ParameterValidationRule {

  /**
   * This function return the name of the parameter expected into parameter lists
   *
   * @return name of the parameter
   */
  String getName();

  /**
   * This function will be called when there is only a string as parameter. It will throw a ValidationError in an error during validation occurs
   *
   * @param value
   * @throws ValidationException
   */
  void validateSingleParam(String value) throws ValidationException;

  /**
   * This function will be called when there is a List<String> that need to be validated. It must check if array is expected or not. It will throw a ValidationError in an error during validation occurs
   *
   * @param value
   * @throws ValidationException
   */
  void validateArrayParam(List<String> value) throws ValidationException;

  /**
   * Return true if parameter is optional
   *
   * @return true if is optional, false otherwise
   */
  boolean isOptional();

  /**
   * Return ParameterTypeValidator instance used inside this parameter validation rule
   *
   * @return
   */
  ParameterTypeValidator getParameterTypeValidator();

  static ParameterValidationRule createValidationRule(String name, ParameterType type, boolean isOptional, ParameterLocation location) {
    return new ParameterValidationRuleImpl(name, type.getValidationMethod(), isOptional, false, location);
  }

  static ParameterValidationRule createValidationRuleWithCustomTypeValidator(String name, ParameterTypeValidator validator, boolean isOptional, ParameterLocation location) {
    return new ParameterValidationRuleImpl(name, validator, isOptional, false, location);
  }
}
