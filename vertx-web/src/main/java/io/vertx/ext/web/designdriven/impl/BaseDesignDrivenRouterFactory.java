package io.vertx.ext.web.designdriven.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.designdriven.DesignDrivenRouterFactory;
import io.vertx.ext.web.validation.ValidationException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
abstract public class BaseDesignDrivenRouterFactory<Specification> implements DesignDrivenRouterFactory {

  protected Vertx vertx;
  protected Specification spec;

  protected boolean enableValidationFailureHandler = true;

  // It can be overriden by the user with function
  protected Handler<RoutingContext> failureHandler = (routingContext -> {
    if (routingContext.failure() instanceof ValidationException) {
      System.out.println(routingContext.failure().getMessage());
    }
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
  public BaseDesignDrivenRouterFactory setValidationFailureHandler(Handler<RoutingContext> handler) {
    this.failureHandler = handler;
    return this;
  }

}
