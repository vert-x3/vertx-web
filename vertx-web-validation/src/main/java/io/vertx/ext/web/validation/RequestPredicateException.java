package io.vertx.ext.web.validation;

/**
 * Represents an error while trying to validate
 */
public class RequestPredicateException extends BadRequestException {

  public RequestPredicateException(String message) {
    super(message, null);
  }

  public RequestPredicateException(String message, Throwable cause) {
    super(message, cause);
  }

}
