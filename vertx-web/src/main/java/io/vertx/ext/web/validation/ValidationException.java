package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.VertxException;
import io.vertx.ext.web.RoutingContext;

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
  private Pattern pattern;
  private String value;
  private ParameterLocation location;
  private RoutingContext routingContext;
  private ErrorType errorType;

  public ValidationException(String parameterName, String value, Pattern pattern, ParameterLocation location, RoutingContext routingContext, ErrorType errorType, String message) {
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

  public ValidationException(String parameterName, ParameterLocation location, RoutingContext routingContext, ErrorType errorType) {
    super("Error during validation of request. Parameter \"" + parameterName + "\" inside " + location.s + "not found");
    this.parameterName = parameterName;
    this.location = location;
    this.routingContext = routingContext;
    this.errorType = errorType;
  }

  public ValidationException(String parameterName, String value, Pattern pattern, ParameterLocation location, RoutingContext routingContext) {
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

  public Pattern getPattern() {
    return pattern;
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
      ", pattern='" + pattern + '\'' +
      ", value='" + value + '\'' +
      ", location=" + location +
      ", routingContext=" + routingContext +
      ", errorType=" + errorType +
      '}';
  }

  public static ValidationException generateWrongContentTypeExpected(String actualContentType, String expectedContentType, RoutingContext routingContext) {
    return new ValidationException("Content-Type", actualContentType, Pattern.compile(Pattern.quote(expectedContentType)), ParameterLocation.HEADER, routingContext, ErrorType.WRONG_CONTENT_TYPE, "Wrong Content-Type header. Actual: " + actualContentType + " Expected: " + expectedContentType);
  }

  public static ValidationException generateNotFoundValidationException(String parameterName, ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException(parameterName, location, routingContext, ErrorType.NOT_FOUND);
  }

  public static ValidationException generateUnexpectedArrayValidationException(String parameterName, ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException(parameterName, location, routingContext, ErrorType.UNEXPECTED_ARRAY);
  }

  public static ValidationException generateNotMatchValidationException(String parameterName, String value, Pattern pattern, ParameterLocation location, RoutingContext routingContext) {
    return new ValidationException(parameterName, value, pattern, location, routingContext);
  }

  public static ValidationException generateFileNotFoundValidationException(String filename, RoutingContext routingContext) {
    return new ValidationException(filename, null, null, ParameterLocation.FILE, routingContext, ErrorType.FILE_NOT_FOUND, "Error during validation: File not found: " + filename);
  }
}
