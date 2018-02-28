package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

public class MethodParameter extends BaseParameter {

  public MethodParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    return context.request().rawMethod().toString();
  }
}
