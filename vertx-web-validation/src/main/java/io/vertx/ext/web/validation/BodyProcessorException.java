package io.vertx.ext.web.validation;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

/**
 * Represents an exception while processing a body
 */
public class BodyProcessorException extends BadRequestException {

  public enum BodyProcessorErrorType {
    /**
     * Error during parsing. The cause of the exception is instance of {@link MalformedValueException}
     */
    PARSING_ERROR,
    /**
     * Error during validation
     */
    VALIDATION_ERROR,
    /**
     * The provided content type doesn't match any of the managed content type by the mounted {@link ValidationHandler}
     */
    MISSING_MATCHING_BODY_PROCESSOR
  }

  private String actualContentType;
  private BodyProcessorErrorType errorType;

  public BodyProcessorException(String message, Throwable cause, String actualContentType, BodyProcessorErrorType errorType) {
    super(message, cause);
    this.actualContentType = actualContentType;
    this.errorType = errorType;
  }

  public String getActualContentType() {
    return actualContentType;
  }

  public BodyProcessorErrorType getErrorType() {
    return errorType;
  }

  @Override
  public JsonObject toJson() {
    return super.toJson()
      .put("actualContentType", this.actualContentType)
      .put("errorType", this.errorType.name());
  }

  public static BodyProcessorException createParsingError(String contentType, MalformedValueException cause) {
    return new BodyProcessorException(
      String.format("Body %s parsing error: %s", contentType, cause.getMessage()), cause, contentType, BodyProcessorErrorType.PARSING_ERROR
    );
  }

  public static BodyProcessorException createParsingError(String contentType, DecodeException cause) {
    return new BodyProcessorException(
      String.format("Json body %s parsing error: %s", contentType, cause.getMessage()), cause, contentType, BodyProcessorErrorType.PARSING_ERROR
    );
  }

  public static BodyProcessorException createValidationError(String contentType, Throwable cause) {
    return new BodyProcessorException(
      String.format("Validation error for body %s: %s", contentType, cause.getMessage()), cause, contentType, BodyProcessorErrorType.VALIDATION_ERROR
    );
  }

  public static BodyProcessorException createMissingMatchingBodyProcessor(String contentType) {
    return new BodyProcessorException(
      String.format("Cannot find body processor for content type %s", contentType), null, contentType, BodyProcessorErrorType.MISSING_MATCHING_BODY_PROCESSOR
    );
  }
}
