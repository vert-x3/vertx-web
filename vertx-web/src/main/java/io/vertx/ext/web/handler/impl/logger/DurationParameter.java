package io.vertx.ext.web.handler.impl.logger;

import static io.vertx.ext.web.handler.impl.LoggerHandlerImpl.CONTEXT_REQUEST_DURATION_PARAM;

import io.vertx.ext.web.RoutingContext;

public class DurationParameter extends BaseParameter {

  public DurationParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    long duration = context.get(CONTEXT_REQUEST_DURATION_PARAM);
    return String.valueOf(duration);
  }
}
