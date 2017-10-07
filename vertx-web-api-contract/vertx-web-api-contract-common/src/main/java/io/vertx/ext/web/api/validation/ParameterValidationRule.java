package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.api.RequestParameter;

import java.util.List;

/**
 * This function is an inner wrapper for ParameterTypeValidator inside ValidationHandler parameter maps. <b>Don't
 * instantiate this class</b>, if you want to add custom ParameterTypeValidator to a parameter use functions in
 * {@link HTTPRequestValidationHandler}
 *
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
   * This function will be called when there is only a string as parameter. It will throw a ValidationError in an
   * error during validation occurs
   *
   * @param value list of values that will be validated
   * @throws ValidationException
   */
  RequestParameter validateSingleParam(String value) throws ValidationException;

  /**
   * This function will be called when there is a List<String> that need to be validated. It must check if array is
   * expected or not. It will throw a ValidationError in an error during validation occurs
   *
   * @param value list of values that will be validated
   * @throws ValidationException
   */
  RequestParameter validateArrayParam(List<String> value) throws ValidationException;

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
  ParameterTypeValidator parameterTypeValidator();

  /**
   * allowEmptyValue is used in query, header, cookie and form parameters. This is its behaviour:
   * <ol>
   * <li>During validation, the ValidationHandler check if there's a parameter with combination of location and name
   * as defined in this rule </li>
   * <li>If it not exists, It will check allowEmptyValue and if there's a default value set inside
   * ParameterTypeValidator:</li>
   * <ul>
   * <li>If this condition it's true, It marks as validated the parameter and returns the default value (inside
   * RequestParameter)</li>
   * <li>If this condition it's false, It throws ValidationException</li>
   * </ul>
   * <li>If the parameter exists, It checks if parameter is null or empty string:</li>
   * <ul>
   * <li>If allowEmptyValue it's true, It marks as validated the parameter and returns the default value if it exists
   * (inside RequestParameter)</li>
   * <li>If allowEmptyValue it's false, It throws ValidationException</li>
   * </ul>
   * </ol>
   *
   * @return value of allowEmptyValue
   */
  boolean allowEmptyValue();

}
