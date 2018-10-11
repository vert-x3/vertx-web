package io.vertx.ext.web.api.contract.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactory;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
abstract public class BaseRouterFactory<Specification> implements RouterFactory<Specification> {

  protected Vertx vertx;
  protected Specification spec;
  protected RouterFactoryOptions options;

  public BaseRouterFactory(Vertx vertx, Specification spec, RouterFactoryOptions options) {
    this.vertx = vertx;
    this.spec = spec;
    this.options = options;
  }

  public BaseRouterFactory(Vertx vertx, Specification spec) {
    this(vertx, spec, new RouterFactoryOptions());
  }

  @Override
  public RouterFactory setOptions(RouterFactoryOptions options) {
    this.options = options;
    return this;
  }

  @Override
  public RouterFactoryOptions getOptions() {
    return options;
  }
}
