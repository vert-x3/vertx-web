package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

class StatusParameter extends BaseParameter {

  public StatusParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return String.valueOf(context.response().getStatusCode());
  }
}
