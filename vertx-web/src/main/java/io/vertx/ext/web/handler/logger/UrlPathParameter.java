package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

/**
 * The URL path requested, not including any query string.
 *
 * The '%U' log parameter
 */
class UrlPathParameter extends BaseParameter {

  public UrlPathParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context, boolean immediate) {
    return context.request().path();
  }
}
