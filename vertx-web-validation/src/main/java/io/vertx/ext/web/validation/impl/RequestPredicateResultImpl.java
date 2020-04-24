package io.vertx.ext.web.validation.impl;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.RequestPredicateResult;

/**
 * Data object representing a Request predicate result
 */
@VertxGen
public class RequestPredicateResultImpl implements RequestPredicateResult {

  private String errorMessage;

  public RequestPredicateResultImpl(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override
  public boolean succeeded() {
    return errorMessage == null;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

}
