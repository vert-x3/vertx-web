package io.vertx.ext.web.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.ServiceResponse;

import java.util.function.Function;

public class ServiceResponseHandler implements Handler<RoutingContext> {

  private final Function<RoutingContext, Future<ServiceResponse>> function;

  public ServiceResponseHandler(Function<RoutingContext, Future<ServiceResponse>> function) {
    this.function = function;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    function.apply(routingContext).setHandler(ar -> {
      if (ar.succeeded()) {
        ServiceResponse op = ar.result();
        HttpServerResponse response = routingContext.response().setStatusCode(op.getStatusCode());
        if (op.getStatusMessage() != null)
          response.setStatusMessage(op.getStatusMessage());
        if (op.getHeaders() != null)
          op.getHeaders().forEach(h -> response.putHeader(h.getKey(), h.getValue()));
        if (op.getPayload() != null)
          response.end(op.getPayload());
        else
          response.end();
      } else {
        routingContext.fail(500, ar.cause());
      }
    });
  }
}
