package io.vertx.ext.web.validation;

import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;

/**
 * Represents an exception while processing a request with {@link ValidationHandler}. Possible failures could be:
 * <ul>
 *   <li>{@link ParameterProcessorException}: Failure while processing the request parameter</li>
 *   <li>{@link BodyProcessorException}: Failure while processing the request body</li>
 *   <li>{@link RequestPredicateException}: A request predicate doesn't match</li>
 * </ul>
 */
public abstract class BadRequestException extends VertxException {

  public BadRequestException(String message, Throwable cause) {
    super("[Bad Request] " + message, cause);
  }

  /**
   * Returns a Json representation of the exception
   *
   * @return
   */
  public JsonObject toJson() {
    JsonObject res = new JsonObject()
      .put("type", this.getClass().getSimpleName())
      .put("message", this.getMessage());
    if (this.getCause() != null) {
      res
        .put("causeType", this.getCause().getClass().getSimpleName())
        .put("causeMessage", this.getCause().getMessage());
    }
    return res;
  }

}
