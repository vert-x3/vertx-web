package io.vertx.ext.web.handler.logger;


import io.vertx.ext.web.RoutingContext;

class EnvironmentVarParameter extends BaseParameter {

  protected EnvironmentVarParameter(String parParam) {
    super(parParam);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return System.getenv(getParParam());
  }
}
