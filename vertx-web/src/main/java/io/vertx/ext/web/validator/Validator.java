package io.vertx.ext.web.validator;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public interface Validator {
  Future<Void> validateRequest(RoutingContext routingContext, Handler<RoutingContext> next);

  enum ParameterType {
    STRING(""), //TODO add regexp
    CASE_SENSITIVE_STRING(""),
    NUMBER(""),
    EMAIL(""),
    INT(""),
    FLOAT("");

    public String regexp;

    ParameterType(String regexp) {
      this.regexp = regexp;
    }
  }
}
