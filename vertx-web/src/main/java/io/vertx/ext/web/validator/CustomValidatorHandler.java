package io.vertx.ext.web.validator;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public interface CustomValidatorHandler {
  Future<Void> validate(RoutingContext routingContext);
}
