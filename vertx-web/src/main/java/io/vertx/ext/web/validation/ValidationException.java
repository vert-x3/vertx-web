package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.VertxException;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.impl.PatternTypeValidator;

import javax.xml.validation.Validator;
import java.util.regex.Pattern;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public class ValidationException extends VertxException {

  public enum ErrorType {
    NO_MATCH,
    NOT_FOUND,
    UNEXPECTED_ARRAY,
    FILE_NOT_FOUND,
    WRONG_CONTENT_TYPE
  }

  private String parameterName;
  private ParameterTypeValidator parameterTypeValidator;
  private String value;
  private ParameterLocation location;
  private RoutingContext routingContext;
  private ErrorType errorType;

  public ValidationException(String message, String parameterName, String value, ParameterTypeValidator parameterTypeValidator, ParameterLocation location, RoutingContext routingContext, ErrorType errorType) {
    super((message != null && message.length() != 0) ? message :
      "ValidationException{" +
        "parameterName='" + parameterName + '\'' +
        ", value='" + value + '\'' +
        ", location=" + location +
        ", routingContext=" + routingContext +
        ", errorType=" + errorType +
        '}');
    this.parameterName = parameterName;
    this.parameterTypeValidator = parameterTypeValidator;
    this.value = value;
    this.location = location;
    this.routingContext = routingContext;
    this.errorType = errorType;
  }

  public String getParameterName() {
    return parameterName;
  }

  public ParameterTypeValidator getParameterTypeValidator() {
    return parameterTypeValidator;
  }

  public String getValue() {
    return value;
  }

  public ParameterLocation getLocation() {
    return location;
  }

  public RoutingContext getRoutingContext() {
    return routingContext;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  @Override
  public String toString() {
    return "ValidationException{" +
      "parameterName='" + parameterName + '\'' +
      ", value='" + value + '\'' +
      ", location=" + location +
      ", routingContext=" + routingContext +
      ", errorType=" + errorType +
      '}';
  }

  public static ValidationException generateWrongContentTypeExpected(String actualContentType, String expectedContentType, RoutingContext routingContext) {
    return new ValidationException("Wrong Content-Type header. Actual: " + actualContentType + " Expected: " + expectedContentType, "Content-Type", actualContentType, null, ParameterLocation.HEADER, routingContext, ErrorType.WRONG_CONTENT_TYPE);
  }

  public static ValidationException generateNotFoundValidationException(String parameterName, ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException("Error during validation of request. Parameter \"" + parameterName + "\" inside " + location.s + "not found", parameterName, null, null, location, routingContext, ErrorType.NOT_FOUND);
  }

  public static ValidationException generateUnexpectedArrayValidationException(String parameterName, ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException("Parameter " + parameterName + " not expected as an array", parameterName, null, null, location, routingContext, ErrorType.UNEXPECTED_ARRAY);
  }

  public static ValidationException generateNotMatchValidationException(String parameterName, String value, ParameterTypeValidator validator, ParameterLocation location, RoutingContext routingContext) {
    if (validator instanceof PatternTypeValidator) {
      String pattern = ((PatternTypeValidator) validator).getPattern().toString();
      return new ValidationException(
        "Error during validation of request. Parameter \"" + parameterName + "\" inside " + location.s + " does not match the pattern \"" + pattern + "\"",
        parameterName,
        value,
        validator,
        location,
        routingContext,
        ErrorType.NO_MATCH);
    } else {
      return new ValidationException(
        "Error during validation of request. Parameter \"" + parameterName + "\" inside " + location.s + " is invalid",
        parameterName,
        value,
        validator,
        location,
        routingContext,
        ErrorType.NO_MATCH);
    }
  }

  public static ValidationException generateFileNotFoundValidationException(String filename, RoutingContext routingContext) {
    return new ValidationException("Error during validation: File not found: " + filename, filename, null, null, ParameterLocation.FILE, routingContext, ErrorType.FILE_NOT_FOUND);
  }
}
