package io.vertx.ext.web.validation;

import io.vertx.core.VertxException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ValidationException extends VertxException {

  public enum ErrorType {
    NO_MATCH,
    NOT_FOUND,
    UNEXPECTED_ARRAY,
    UNEXPECTED_SINGLE_STRING,
    FILE_NOT_FOUND,
    WRONG_CONTENT_TYPE,
    EMPTY_VALUE,
    UNEXPECTED_ARRAY_SIZE,
    DESERIALIZATION_ERROR,
    OBJECT_FIELD_NOT_FOUND,
    JSON_NOT_PARSABLE,
    JSON_INVALID,
    XML_INVALID
  }

  private String parameterName;
  private ParameterValidationRule validationRule;
  private String value;
  final private ErrorType errorType;

  public ValidationException(String message, String parameterName, String value, ParameterValidationRule validationRule, ErrorType errorType) {
    super((message != null && message.length() != 0) ? message :
      "ValidationException{" +
        "parameterName='" + parameterName + '\'' +
        ", value='" + value + '\'' +
        ", errorType=" + errorType +
        '}');
    this.parameterName = parameterName;
    this.validationRule = validationRule;
    this.value = value;
    this.errorType = errorType;
  }

  public ValidationException(String message, ErrorType error) {
    this(message, null, null, null, error);
  }

  public ValidationException(ErrorType error) {
    this(null, null, null, null, error);
  }

  public String getParameterName() {
    return parameterName;
  }

  public ParameterValidationRule getValidationRule() {
    return validationRule;
  }

  public String getValue() {
    return value;
  }

  public ErrorType getErrorType() {
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
    return "ValidationException{" +
      "parameterName='" + parameterName + '\'' +
      ", value='" + value + '\'' +
      ", errorType=" + errorType +
      '}';
  }

  public static ValidationException generateWrongContentTypeExpected(String actualContentType, String expectedContentType) {
    return new ValidationException("Wrong Content-Type header. Actual: " + actualContentType + " Expected: " + expectedContentType, "Content-Type", actualContentType, null, ErrorType.WRONG_CONTENT_TYPE);
  }

  public static ValidationException generateNotFoundValidationException(String parameterName, ParameterLocation location) {
    return new ValidationException("Error during validation of request. Parameter \"" + parameterName + "\" inside " + location.s + "not found", parameterName, null, null, ErrorType.NOT_FOUND);
  }

  public static ValidationException generateUnexpectedArrayValidationException(String parameterName, ParameterValidationRule validationRule) {
    return new ValidationException("Parameter " + parameterName + " not expected as an array", parameterName, null, validationRule, ErrorType.UNEXPECTED_ARRAY);
  }

  public static ValidationException generateUnexpectedSingleStringValidationException(String parameterName, ParameterValidationRule validationRule) {
    return new ValidationException("Parameter " + parameterName + "  expected as array", parameterName, null, validationRule, ErrorType.UNEXPECTED_SINGLE_STRING);
  }

  public static ValidationException generateNotMatchValidationException(String parameterName, String value, ParameterValidationRule validationRule) {
    return new ValidationException(
      "Error during validation of request. Parameter \"" + parameterName + "\" does not match the validator rules",
        parameterName,
        value,
      validationRule,
        ErrorType.NO_MATCH);
  }

  public static ValidationException generateFileNotFoundValidationException(String filename) {
    return new ValidationException("Error during validation: File not found: " + filename, filename, null, null, ErrorType.FILE_NOT_FOUND);
  }

  public static ValidationException generateEmptyValueValidationException(String parameterName, ParameterValidationRule rule, ParameterLocation location) {
    return new ValidationException("Parameter " + parameterName + " inside " + location.s + " is empty", parameterName, null, rule, ErrorType.EMPTY_VALUE);
  }

  public static ValidationException generateUnexpectedArraySizeValidationException(Integer maxItems, Integer minItems, Integer actualSize) {
    return new ValidationException("Array parameter have unexpected size: " + minItems + "<=" + actualSize + "<=" + maxItems, ErrorType.UNEXPECTED_ARRAY_SIZE);
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

  public static ValidationException generateNotParsableJsonBodyException() {
    return new ValidationException(null, ErrorType.JSON_NOT_PARSABLE);
  }

  public static ValidationException generateInvalidJsonBodyException(String message) {
    return new ValidationException(message, ErrorType.JSON_INVALID);
  }

  public static ValidationException generateInvalidXMLBodyException(String message) {
    return new ValidationException(message, ErrorType.XML_INVALID);
  }


}
