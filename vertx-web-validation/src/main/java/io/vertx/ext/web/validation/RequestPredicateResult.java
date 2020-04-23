package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

/**
 * Data object representing a Request predicate result
 */
@VertxGen
public class RequestPredicateResult {

  private String errorMessage;

  private RequestPredicateResult(String exception) {
    this.errorMessage = exception;
  }

  public boolean succeeded() {
    return errorMessage == null;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static RequestPredicateResult success() {
    return new RequestPredicateResult(null);
  }

  public static RequestPredicateResult failed(String message) {
    return new RequestPredicateResult(message);
  }

}
