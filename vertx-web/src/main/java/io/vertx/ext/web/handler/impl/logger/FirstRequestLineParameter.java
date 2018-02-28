package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

/**
 * First line of request. Consists of method, path+query, httpVersion, e.g.:
 * <pre>GET /foo?param=bar HTTP/1.1</pre>
 *
 * The '%r' parameter
 */
public class FirstRequestLineParameter extends BaseParameter {

  public FirstRequestLineParameter() {
    super(null);
  }

  @Override
  protected String getValue(RoutingContext context) {
    String query = context.request().query();
    query = (query == null || query.length() == 0) ? "" : "?" + query;

    String versionFormatted = ProtocolHelper.toString(context.request().version());

    return context.request().rawMethod() + " " + context.request().path() + query + " "
      + (versionFormatted != null ? versionFormatted : "-");
  }
}
