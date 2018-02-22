package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

class ProtocolParameter extends BaseParameter {

  public ProtocolParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return ProtocolHelper.toString(context.request().version());
  }
}
