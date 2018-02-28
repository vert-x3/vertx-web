package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

public class NonImplementedParameter extends BaseParameter {

  public NonImplementedParameter(String parParam) {
    super(parParam);
  }

  @Override
  protected String getValue(RoutingContext context) {
    return null;
  }
}
