package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

public class StatusParameter extends BaseParameter {

  public StatusParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    return String.valueOf(context.response().getStatusCode());
  }
}
