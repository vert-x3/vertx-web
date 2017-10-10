package io.vertx.ext.web.api.contract.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.DesignDrivenRouterFactory;
import io.vertx.ext.web.api.validation.ValidationException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
abstract public class BaseDesignDrivenRouterFactory<Specification> implements DesignDrivenRouterFactory<Specification> {

  protected Vertx vertx;
  protected Specification spec;

  protected boolean enableValidationFailureHandler = true;
  protected boolean mount501handlers = true;

  // It can be overriden by the user with function
  protected Handler<RoutingContext> failureHandler = (routingContext -> {
    if (routingContext.failure() instanceof ValidationException) {
      routingContext.response().setStatusCode(400).setStatusMessage("Bad Request").end(routingContext.failure()
        .getMessage());
    } else routingContext.next();
  });

  public BaseDesignDrivenRouterFactory(Vertx vertx, Specification spec) {
    this.vertx = vertx;
    this.spec = spec;
  }

  @Override
  public DesignDrivenRouterFactory enableValidationFailureHandler(boolean enable) {
    this.enableValidationFailureHandler = enable;
    return this;
  }

  @Override
  public BaseDesignDrivenRouterFactory setValidationFailureHandler(Handler handler) {
    this.failureHandler = handler;
    return this;
  }

  @Override
  public DesignDrivenRouterFactory mountOperationsWithoutHandlers(boolean enable) {
    this.mount501handlers = enable;
    return this;
  }
}
