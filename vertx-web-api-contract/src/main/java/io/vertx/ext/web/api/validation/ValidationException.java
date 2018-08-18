package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.VertxException;

/**
 * This is the main class for every Validation flow related errors
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ValidationException extends VertxException {

  /**
   * All errors type. You can get this values using {@link ValidationException#type()}
   */
  @VertxGen
  public enum ErrorType {
    /**
     * The provided value not match with ParameterTypeValidator rules
     */
    NO_MATCH, /**
     * Parameter not found in request
     */
    NOT_FOUND, /**
     * It was expected a single value, but found in request an array
     */
    UNEXPECTED_ARRAY, /**
     * It was expected an array, but found in request a single value
     */
    UNEXPECTED_SINGLE_STRING, /**
     * Expected file not found
     */
    FILE_NOT_FOUND, /**
     * Wrong Content-Type header
     */
    WRONG_CONTENT_TYPE, /**
     * Parameter found but with empty value
     */
    EMPTY_VALUE, /**
     * Expected an array size between parameters configured in
     * {@link io.vertx.ext.web.api.validation.impl.ArrayTypeValidator}
     */
    UNEXPECTED_ARRAY_SIZE, /**
     * Error during deserializaton with rule provided
     */
    DESERIALIZATION_ERROR, /**
     * Object field declared as required in {@link io.vertx.ext.web.api.validation.impl.ObjectTypeValidator} not found
     */
    OBJECT_FIELD_NOT_FOUND, /**
     * Json can't be parsed
     */
    JSON_NOT_PARSABLE, /**
     * Json doesn't match the provided schema
     */
    JSON_INVALID, /**
     * XML doesn't match the provided schema
     */
    XML_INVALID
  }

  private String parameterName;
  private ParameterValidationRule validationRule;
  private String value;
  final private ErrorType errorType;

  private ValidationException(String message, String parameterName, String value, ParameterValidationRule validationRule, ErrorType errorType, Throwable cause) {
    super((message != null && message.length() != 0) ? message : "ValidationException{" + "parameterName='" +
      parameterName + '\'' + ", value='" + value + '\'' + ", errorType=" + errorType + '}', cause);
    this.parameterName = parameterName;
    this.validationRule = validationRule;
    this.value = value;
    this.errorType = errorType;
  }

  private ValidationException(String message, String parameterName, String value, ParameterValidationRule validationRule, ErrorType errorType) {
    this(message, parameterName, value, validationRule, errorType, null);
  }

  public ValidationException(String message, ErrorType error, Throwable cause) {
    this(message, null, null, null, error, cause);
  }

  public ValidationException(String message, ErrorType error) {
    this(message, null, null, null, error, null);
  }

  public ValidationException(ErrorType error) {
    this(null, null, null, null, error, null);
  }

  public ValidationException(String message) {
    this(message, null, null, null, null, null);
  }

  @Nullable
  public String parameterName() {
    return parameterName;
  }

  public ParameterValidationRule validationRule() {
    return validationRule;
  }

  public String value() {
    return value;
  }

  public ErrorType type() {
    return errorType;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public void setValidationRule(ParameterValidationRule validationRule) {
    this.validationRule = validationRule;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "ValidationException{" + "parameterName='" + parameterName + '\'' + ", value='" + value + '\'' + ", " +
      "errorType=" + errorType + '}';
  }

  public static class ValidationExceptionFactory {

    public static ValidationException generateWrongContentTypeExpected(String actualContentType, String
      expectedContentType) {
      return new ValidationException("Wrong Content-Type header. Actual: " + actualContentType + " Expected: " +
        expectedContentType, "Content-Type", actualContentType, null, ErrorType.WRONG_CONTENT_TYPE);
    }

    public static ValidationException generateNotFoundValidationException(String parameterName, ParameterLocation
      location) {
      return new ValidationException("Error during validation of request. Parameter \"" + parameterName + "\" inside " +
        location.s + " not found", parameterName, null, null, ErrorType.NOT_FOUND);
    }

    public static ValidationException generateUnexpectedArrayValidationException(String parameterName,
                                                                                 ParameterValidationRule
                                                                                   validationRule) {
      return new ValidationException("Parameter " + parameterName + " not expected as an array", parameterName, null,
        validationRule, ErrorType.UNEXPECTED_ARRAY);
    }

    public static ValidationException generateUnexpectedSingleStringValidationException(String parameterName,
                                                                                        ParameterValidationRule
                                                                                          validationRule) {
      return new ValidationException("Parameter " + parameterName + "  expected as array", parameterName, null,
        validationRule, ErrorType.UNEXPECTED_SINGLE_STRING);
    }

    public static ValidationException generateNotMatchValidationException(String parameterName, String value,
                                                                          ParameterValidationRule validationRule) {
      return new ValidationException("Error during validation of request. Parameter \"" + parameterName + "\" does "
        + "not match the validator rules", parameterName, value, validationRule, ErrorType.NO_MATCH);
    }

    public static ValidationException generateFileNotFoundValidationException(String filename, String contentType) {
      return new ValidationException("Error during validation: File not found or wrong content type. Expected file "
        + "name: \"" + filename + "\". Expected content type: \"" + contentType + "\"", filename, null, null,
        ErrorType.FILE_NOT_FOUND);
    }

    public static ValidationException generateEmptyValueValidationException(String parameterName,
                                                                            ParameterValidationRule rule,
                                                                            ParameterLocation location) {
      return new ValidationException("Parameter " + parameterName + " inside " + location.s + " is empty",
        parameterName, null, rule, ErrorType.EMPTY_VALUE);
    }

    public static ValidationException generateUnexpectedArraySizeValidationException(Integer maxItems, Integer
      minItems, Integer actualSize) {
      return new ValidationException("Array parameter have unexpected size: " + minItems + "<=" + actualSize + "<=" +
        maxItems, ErrorType.UNEXPECTED_ARRAY_SIZE);
    }

    public static ValidationException generateDeserializationError(String message) {
      return new ValidationException(message, ErrorType.DESERIALIZATION_ERROR);
    }

    public static ValidationException generateObjectFieldNotFound(String fieldName) {
      return new ValidationException("Object field not found but required: " + fieldName, ErrorType.OBJECT_FIELD_NOT_FOUND);
    }

    public static ValidationException generateNotMatchValidationException(String message) {
      return new ValidationException(message, ErrorType.NO_MATCH);
    }

    public static ValidationException generateNotParsableJsonBodyException(String message) {
      return new ValidationException(message, ErrorType.JSON_NOT_PARSABLE);
    }

    public static ValidationException generateInvalidJsonBodyException(String jsonPath, String value, String message) {
      String jsonPathWithoutDollar = (jsonPath != null) ? jsonPath.substring(jsonPath.indexOf("$.") + 2) : "";
      return new ValidationException(message, (jsonPathWithoutDollar.length() == 0) ? "body" : "body." + jsonPathWithoutDollar, value, null, ErrorType.JSON_INVALID);
    }

    public static ValidationException generateInvalidXMLBodyException(String message) {
      return new ValidationException(message, ErrorType.XML_INVALID);
    }

  }


}
