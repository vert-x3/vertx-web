package io.vertx.ext.web.validation;

import io.vertx.ext.web.RoutingContext;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public interface CustomValidatorHandler {
  void validate(RoutingContext routingContext) throws ValidationException;
}
