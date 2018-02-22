package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

class NonImplementedParameter extends BaseParameter {

  NonImplementedParameter(String parParam) {
    super(parParam);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return null;
  }
}
