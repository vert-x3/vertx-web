package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

public class PlainTextParameter implements Parameter {

  private String value;

  public PlainTextParameter(String value) {
    this.value = value;
  }

  @Override
  public StringBuilder print(RoutingContext context) {
    return new StringBuilder(value);
  }
}
