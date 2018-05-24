package io.vertx.ext.web.api.contract.openapi3.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.RequestParameters;

public class RouteToEventBusOperationHandler implements Handler<RoutingContext> {

  EventBus eventBus;
  String address;
  String actionName;

  public RouteToEventBusOperationHandler(EventBus eventBus, String address, String actionName) {
    this.eventBus = eventBus;
    this.address = address;
    this.actionName = actionName;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    eventBus.send(address, buildPayload(routingContext), new DeliveryOptions().addHeader("action", actionName), (AsyncResult<Message<JsonObject>> res) -> {
      if (res.succeeded()) {
        OperationResult op = new OperationResult(res.result().body());
        HttpServerResponse response = routingContext.response().setStatusCode(op.getStatusCode());
        // op.getHeaders().forEach(e -> response.putHeader(e.getKey(), e.getValue()));
        response.end(op.getPayload().toString());
      } else {
        routingContext.fail(res.cause());
      }
    });
  }

  private static JsonObject buildPayload(RoutingContext context) {
    return new JsonObject().put("context", new RequestContext(
      context.request().headers(),
      ((RequestParameters)context.get("parsedParameters")).toJsonObject()
    ).toJson());
  }

}
