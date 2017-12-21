package io.vertx.ext.web.api.contract.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.DesignDrivenRouterFactory;
import io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions;
import io.vertx.ext.web.api.validation.ValidationException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
abstract public class BaseDesignDrivenRouterFactory<Specification> implements DesignDrivenRouterFactory<Specification> {

  protected Vertx vertx;
  protected Specification spec;
  protected DesignDrivenRouterFactoryOptions options;

  public BaseDesignDrivenRouterFactory(Vertx vertx, Specification spec, DesignDrivenRouterFactoryOptions options) {
    this.vertx = vertx;
    this.spec = spec;
    this.options = options;
  }

  public BaseDesignDrivenRouterFactory(Vertx vertx, Specification spec) {
    this(vertx, spec, new DesignDrivenRouterFactoryOptions());
  }

  @Override @Deprecated
  public DesignDrivenRouterFactory enableValidationFailureHandler(boolean enable) {
    if (options == null)
      this.options = new DesignDrivenRouterFactoryOptions();
    this.options.setMountValidationFailureHandler(enable);
    return this;
  }

  @Override @Deprecated
  public BaseDesignDrivenRouterFactory setValidationFailureHandler(Handler<RoutingContext> handler) {
    if (options == null)
      this.options = new DesignDrivenRouterFactoryOptions();
    this.options.setValidationFailureHandler(handler);
    return this;
  }

  @Override @Deprecated
  public DesignDrivenRouterFactory mountOperationsWithoutHandlers(boolean enable) {
    if (options == null)
      this.options = new DesignDrivenRouterFactoryOptions();
    this.options.setMountNotImplementedHandler(enable);
    return this;
  }

  @Override
  public DesignDrivenRouterFactory setOptions(DesignDrivenRouterFactoryOptions options) {
    this.options = options;
    return this;
  }

  @Override
  public DesignDrivenRouterFactoryOptions options() {
    return options;
  }
}
