package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

public class ProtocolParameter extends BaseParameter {

  public ProtocolParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    return ProtocolHelper.toString(context.request().version());
  }
}
