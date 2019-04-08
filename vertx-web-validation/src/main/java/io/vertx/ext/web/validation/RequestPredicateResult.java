package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Data object representing a Request predicate result
 */
@DataObject
public class RequestPredicateResult {

  private String errorMessage;

  public RequestPredicateResult(JsonObject obj) {
    this(obj.getString("errorMessage"));
  }

  private RequestPredicateResult(String exception) {
    this.errorMessage = exception;
  }

  public JsonObject toJson() {
    return new JsonObject().put("errorMessage", errorMessage);
  }

  public boolean succeeded() {
    return errorMessage == null;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static RequestPredicateResult success() {
    return new RequestPredicateResult((String)null);
  }

  public static RequestPredicateResult failed(String message) {
    return new RequestPredicateResult(message);
  }

}
