package io.vertx.ext.web.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebResponse;

import java.util.function.Function;

public class WebResponseHandler implements Handler<RoutingContext> {

  private final Function<RoutingContext, Future<WebResponse>> function;

  public WebResponseHandler(Function<RoutingContext, Future<WebResponse>> function) {
    this.function = function;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    function.apply(routingContext).setHandler(ar -> {
      if (ar.succeeded()) {
        WebResponseImpl op = (WebResponseImpl) ar.result();
        HttpServerResponse response = routingContext.response().setStatusCode(op.getStatusCode());
        if (op.getStatusMessage() != null)
          response.setStatusMessage(op.getStatusMessage());
        if (op.getHeaders() != null)
          op.getHeaders().forEach(h -> response.putHeader(h.getKey(), h.getValue()));
        if (op.isStream()) {
          response.setChunked(true);
          op.getPayloadStream()
          .pipe()
          .endOnFailure(false)
          .endOnSuccess(true)
          .to(response, streamAr -> {
            if (streamAr.failed() && !routingContext.response().closed())
              routingContext.fail(streamAr.cause());
          });
          op.getPayloadStream().resume();
        }
        else if (op.getPayload() != null)
          response.end(op.getPayload());
        else
          response.end();
      } else {
        routingContext.fail(500, ar.cause());
      }
    });
  }
}
