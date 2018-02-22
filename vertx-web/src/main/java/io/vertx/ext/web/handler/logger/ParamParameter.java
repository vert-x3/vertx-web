package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

class ParamParameter extends BaseParameter {

  public ParamParameter(String parParam) {
    super(parParam);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return context.request().getParam(getParParam());
  }

}
