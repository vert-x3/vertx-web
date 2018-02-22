package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

/**
 * TODO: How to do it best way ??
 */
class DurationParameter extends BaseParameter {

  public DurationParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    long duration = context.get("logger-requestDuration");
    context.remove("logger-requestDuration");
    return String.valueOf(duration);
  }
}
