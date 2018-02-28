package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

/**
 * The thread ID that serviced the request.
 *
 * The '%P' parameter. A '%{format}P' version is not supported
 */
public class ThreadParameter extends BaseParameter {

  public ThreadParameter(String parParam) {
    super(parParam);
  }

  @Override
  protected String getValue(RoutingContext context) {
    return Thread.currentThread().getName();
  }
}
