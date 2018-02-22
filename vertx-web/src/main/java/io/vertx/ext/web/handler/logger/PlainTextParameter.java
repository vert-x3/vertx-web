package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

class PlainTextParameter implements Parameter {

  private String value;

  PlainTextParameter(String value) {
    this.value = value;
  }

  @Override
  public StringBuilder print(RoutingContext context, boolean immediate) {
    return new StringBuilder(value);
  }
}
