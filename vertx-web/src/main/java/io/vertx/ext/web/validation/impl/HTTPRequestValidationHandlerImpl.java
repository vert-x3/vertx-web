package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.CustomValidator;
import io.vertx.ext.web.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.validation.ParameterType;

import java.util.regex.Pattern;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class HTTPRequestValidationHandlerImpl extends BaseValidationHandler implements HTTPRequestValidationHandler {

  public HTTPRequestValidationHandlerImpl() {
    super();
  }

  /**
   * Add a path parameter with included parameter types
   *
   * @param parameterName expected name of parameter inside the path
   * @param type          expected type of parameter
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addPathParam(String parameterName, ParameterType type) {
    this.addPathParamRule(new ValidationRule(parameterName, type, false, false));
    return this;
  }

  /**
   * Add a path parameter with a custom pattern
   *
   * @param parameterName expected name of parameter inside the path
   * @param pattern       regular expression for validation
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addPathParamWithPattern(String parameterName, String pattern) {
    this.addPathParamRule(new ValidationRule(parameterName, new PatternTypeValidator(pattern), false, false));
    return this;
  }

  /**
   * Add a query parameter with included parameter types
   *
   * @param parameterName expected name of parameter inside the query
   * @param type          expected type of parameter
   * @param required      true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addQueryParam(String parameterName, ParameterType type, boolean required) {
    this.addQueryParamRule(new ValidationRule(parameterName, type, !required, false));
    return this;
  }

  /**
   * Add a query parameter with a custom pattern
   *
   * @param parameterName expected name of parameter inside the query
   * @param pattern       regular expression for validation
   * @param required      true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addQueryParamWithPattern(String parameterName, String pattern, boolean required) {
    this.addQueryParamRule(new ValidationRule(parameterName, new PatternTypeValidator(pattern), !required, false));
    return this;
  }

  /**
   * Add a query parameters array with included parameter types.
   *
   * @param arrayName expected name of array inside the query
   * @param type      expected type of parameter
   * @param required  true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addQueryParamsArray(String arrayName, ParameterType type, boolean required) {
    this.addQueryParamRule(new ValidationRule(arrayName, type, !required, true));
    return this;
  }

  /**
   * Add a query parameters array with a custom pattern
   *
   * @param arrayName expected name of array inside the query
   * @param pattern   regular expression for validation
   * @param required  true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addQueryParamsArrayWithPattern(String arrayName, String pattern, boolean required) {
    this.addQueryParamRule(new ValidationRule(arrayName, new PatternTypeValidator(pattern), !required, true));
    return this;
  }

  /**
   * Add a header parameter with included parameter types
   *
   * @param headerName expected header name
   * @param type       expected type of parameter
   * @param required   true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addHeaderParam(String headerName, ParameterType type, boolean required) {
    this.addHeaderParamRule(new ValidationRule(headerName, type, !required, false));
    return this;
  }

  /**
   * Add a header parameter with a custom pattern
   *
   * @param headerName expected header name
   * @param pattern    regular expression for validation
   * @param required   true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addHeaderParamWithPattern(String headerName, String pattern, boolean required) {
    this.addHeaderParamRule(new ValidationRule(headerName, new PatternTypeValidator(pattern), !required, false));
    return this;
  }

  /**
   * Add a single parameter inside a form with included parameter types
   *
   * @param parameterName expected name of parameter inside the form
   * @param type          expected type of parameter
   * @param required      true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addFormParam(String parameterName, ParameterType type, boolean required) {
    this.addFormParamRule(new ValidationRule(parameterName, type, !required, false));
    return this;
  }

  /**
   * Add a single parameter inside a form with a custom pattern
   *
   * @param parameterName expected name of parameter inside the form
   * @param pattern       regular expression for validation
   * @param required      true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addFormParamWithPattern(String parameterName, String pattern, boolean required) {
    this.addFormParamRule(new ValidationRule(parameterName, new PatternTypeValidator(pattern), !required, false));
    return this;
  }

  /**
   * Add a form parameters array with included parameter types
   *
   * @param parameterName expected name of array of parameters inside the form
   * @param type          expected type of array of parameters
   * @param required      true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addFormParamsArray(String parameterName, ParameterType type, boolean required) {
    this.addFormParamRule(new ValidationRule(parameterName, type, !required, true));
    return this;
  }

  /**
   * Add a query parameters array with a custom pattern
   *
   * @param parameterName expected name of array of parameters inside the form
   * @param pattern       regular expression for validation
   * @param required      true if parameter is required
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addFormParamsArrayWithPattern(String parameterName, String pattern, boolean required) {
    this.addFormParamRule(new ValidationRule(parameterName, new PatternTypeValidator(pattern), !required, false));
    return this;
  }

  /**
   * Add a custom validator. For more informations about custom validator, see {@link CustomValidator}
   *
   * @param customValidator
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addCustomValidatorFunction(CustomValidator customValidator) {
    this.addCustomValidator(customValidator);
    return this;
  }

  /**
   * Add a json schema for body with Content-Type "application/json"
   *
   * @param jsonSchema
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addJsonBodySchema(String jsonSchema) {
    this.setJsonSchema(jsonSchema);
    return this;
  }

  /**
   * Add a xml schema for body with Content-Type "application/xml"
   *
   * @param xmlSchema
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addXMLBodySchema(String xmlSchema) {
    this.setXmlSchema(xmlSchema);
    return this;
  }

  /**
   * Add a required file name
   *
   * @param filename name of the file inside the form
   * @return this handler
   */
  @Override
  public HTTPRequestValidationHandler addRequiredFile(String filename) {
    this.addFileUploadName(filename);
    return this;
  }
}
