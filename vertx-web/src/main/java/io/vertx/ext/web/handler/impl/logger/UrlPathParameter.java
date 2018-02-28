package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

/**
 * The URL path requested, not including any query string.
 *
 * The '%U' log parameter
 */
public class UrlPathParameter extends BaseParameter {

  public UrlPathParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    return context.request().path();
  }
}
