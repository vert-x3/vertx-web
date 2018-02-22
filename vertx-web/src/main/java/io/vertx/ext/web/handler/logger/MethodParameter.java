package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

class MethodParameter extends BaseParameter {

  public MethodParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return context.request().rawMethod().toString();
  }
}
