package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

/**
 * The query string (prepended with a ? if a query string exists, otherwise an empty string).
 *
 * The '%q' parameter
 */
public class QueryParameter extends BaseParameter {

  public QueryParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    String query = context.request().query();
    return (query == null || query.length() == 0) ? "" : "?" + query;
  }
}
