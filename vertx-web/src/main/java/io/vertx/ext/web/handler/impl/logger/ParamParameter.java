package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

public class ParamParameter extends BaseParameter {

  public ParamParameter(String parParam) {
    super(parParam);
  }

  @Override
  protected String getValue(RoutingContext context) {
    return context.request().getParam(getParParam());
  }

}
