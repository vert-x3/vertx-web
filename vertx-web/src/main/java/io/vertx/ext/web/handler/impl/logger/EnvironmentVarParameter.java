package io.vertx.ext.web.handler.impl.logger;


import io.vertx.ext.web.RoutingContext;

public class EnvironmentVarParameter extends BaseParameter {

  public EnvironmentVarParameter(String parParam) {
    super(parParam);
  }

  @Override
  protected String getValue(RoutingContext context) {
    String val = System.getenv(getParParam());
    return val == null ? System.getProperty(getParParam()) : val;
  }
}
