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

public class RouteToServiceProxyHandler implements Handler<RoutingContext> {

  EventBus eventBus;
  String address;
  DeliveryOptions deliveryOptions;

  public RouteToServiceProxyHandler(EventBus eventBus, String address, DeliveryOptions deliveryOptions) {
    this.eventBus = eventBus;
    this.address = address;
    this.deliveryOptions = deliveryOptions;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    eventBus.send(address, buildPayload(routingContext), deliveryOptions, (AsyncResult<Message<JsonObject>> res) -> {
      OperationResult op = new OperationResult(res.result().body());
      HttpServerResponse response = routingContext.response().setStatusCode(op.getStatusCode());
      op.getHeaders().forEach(h -> response.putHeader(h.getKey(), h.getValue()));
      response.end(op.getPayload().toString());
    });
  }

  private static JsonObject buildPayload(RoutingContext context) {
    return new JsonObject().put("context", new RequestContext(
      context.request().headers(),
      ((RequestParameters)context.get("parsedParameters")).toJsonObject()
    ).toJson());
  }

  public static RouteToServiceProxyHandler build(EventBus eventBus, String address, String actionName) {
    return new RouteToServiceProxyHandler(eventBus, address, new DeliveryOptions().addHeader("action", actionName));
  }

  public static RouteToServiceProxyHandler build(EventBus eventBus, String address, String actionName, JsonObject deliveryOptions) {
    DeliveryOptions opt = new DeliveryOptions(deliveryOptions).addHeader("action", actionName);
    return new RouteToServiceProxyHandler(eventBus, address, opt);
  }

}
