package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.RequestPredicateResultImpl;

/**
 * Result of a {@link RequestPredicate}
 */
@VertxGen
public interface RequestPredicateResult {

  /**
   * Predicate succeeded
   *
   * @return
   */
  boolean succeeded();

  /**
   * Get error of failure
   *
   * @return
   */
  String getErrorMessage();

  static RequestPredicateResult success() {
    return new RequestPredicateResultImpl(null);
  }

  static RequestPredicateResult failed(String message) {
    return new RequestPredicateResultImpl(message);
  }
}
