package io.vertx.ext.web.validation;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.ValidationHandler.ParameterLocation;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public class ValidationException extends Exception {

  public enum ErrorType {
    NO_MATCH,
    NOT_FOUND,
    UNEXPECTED_ARRAY,
    FILE_NOT_FOUND
  }

  private String parameterName;
  private String pattern;
  private String value;
  private ValidationHandler.ParameterLocation location;
  private RoutingContext routingContext;
  private ErrorType errorType;

  public ValidationException(String parameterName, String value, String pattern, ParameterLocation location, RoutingContext routingContext, ErrorType errorType, String message) {
    super((message != null && message.length() != 0) ? message :
      "ValidationException{" +
        "parameterName='" + parameterName + '\'' +
        ", pattern='" + pattern + '\'' +
        ", value='" + value + '\'' +
        ", location=" + location +
        ", routingContext=" + routingContext +
        ", errorType=" + errorType +
        '}');
    this.parameterName = parameterName;
    this.pattern = pattern;
    this.value = value;
    this.location = location;
    this.routingContext = routingContext;
    this.errorType = errorType;
  }

  public ValidationException(String parameterName, ValidationHandler.ParameterLocation location, RoutingContext routingContext, ErrorType errorType) {
    super("Error during validation of request. Parameter \"" + parameterName + "\" inside " + location.s + "not found");
    this.parameterName = parameterName;
    this.location = location;
    this.routingContext = routingContext;
    this.errorType = errorType;
  }

  public ValidationException(String parameterName, String value, String pattern, ValidationHandler.ParameterLocation location, RoutingContext routingContext) {
    super("Error during validation of request. Parameter \"" + parameterName + "\" inside " + location.s + "does not match the pattern \"" + pattern + "\"");
    this.parameterName = parameterName;
    this.value = value;
    this.pattern = pattern;
    this.location = location;
    this.routingContext = routingContext;
    this.errorType = ErrorType.NO_MATCH;
  }

  public String getParameterName() {
    return parameterName;
  }

  public String getPattern() {
    return pattern;
  }

  public ValidationHandler.ParameterLocation getLocation() {
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
      ", pattern='" + pattern + '\'' +
      ", value='" + value + '\'' +
      ", location=" + location +
      ", routingContext=" + routingContext +
      ", errorType=" + errorType +
      '}';
  }

  protected static ValidationException generateNotFoundValidationException(String parameterName, ValidationHandler.ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException(parameterName, location, routingContext, ErrorType.NOT_FOUND);
  }

  protected static ValidationException generateUnexpectedArrayValidationException(String parameterName, ValidationHandler.ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException(parameterName, location, routingContext, ErrorType.UNEXPECTED_ARRAY);
  }

  protected static ValidationException generateNotMatchValidationException(String parameterName, String value, String pattern, ValidationHandler.ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException(parameterName, value, pattern, location, routingContext);
  }

  protected static ValidationException generateFileNotFoundValidationException(String filename, RoutingContext routingContext) {
    return new ValidationException(filename, null, null, ParameterLocation.BODY_FORM, routingContext, ErrorType.FILE_NOT_FOUND, "Error during validation: File not found: " + filename);
  }
}
