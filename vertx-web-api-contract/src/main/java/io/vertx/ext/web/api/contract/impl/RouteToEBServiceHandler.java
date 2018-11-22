package io.vertx.ext.web.api.contract.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.RequestParameters;

import java.util.function.Function;

public class RouteToEBServiceHandler implements Handler<RoutingContext> {

  EventBus eventBus;
  String address;
  DeliveryOptions deliveryOptions;
  Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper;

  public RouteToEBServiceHandler(EventBus eventBus, String address, DeliveryOptions deliveryOptions, Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper) {
    this.eventBus = eventBus;
    this.address = address;
    this.deliveryOptions = deliveryOptions;
    this.extraOperationContextPayloadMapper = extraOperationContextPayloadMapper;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    eventBus.send(address, buildPayload(routingContext), deliveryOptions, (AsyncResult<Message<JsonObject>> res) -> {
      if (res.succeeded()) {
        OperationResponse op = new OperationResponse(res.result().body());
        HttpServerResponse response = routingContext.response().setStatusCode(op.getStatusCode());
        if (op.getStatusMessage() != null)
          response.setStatusMessage(op.getStatusMessage());
        if (op.getHeaders() != null)
          op.getHeaders().forEach(h -> response.putHeader(h.getKey(), h.getValue()));
        if (op.getPayload() != null)
          response.end(op.getPayload().toString());
        else
          response.end();
      } else {
        routingContext.fail(res.cause());
      }
    });
  }

  private JsonObject buildPayload(RoutingContext context) {
    return new JsonObject().put("context", new OperationRequest(
      ((RequestParameters)context.get("parsedParameters")).toJson(),
      context.request().headers(),
      (context.user() != null) ? context.user().principal() : null,
      (this.extraOperationContextPayloadMapper != null) ? this.extraOperationContextPayloadMapper.apply(context) : null
    ).toJson());
  }

  public static RouteToEBServiceHandler build(EventBus eventBus, String address, String actionName, Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper) {
    return new RouteToEBServiceHandler(eventBus, address, new DeliveryOptions().addHeader("action", actionName), extraOperationContextPayloadMapper);
  }

  public static RouteToEBServiceHandler build(EventBus eventBus, String address, String actionName, JsonObject deliveryOptions, Function<RoutingContext, JsonObject> extraOperationContextPayloadMapper) {
    DeliveryOptions opt = new DeliveryOptions(deliveryOptions).addHeader("action", actionName);
    return new RouteToEBServiceHandler(eventBus, address, opt, extraOperationContextPayloadMapper);
  }

}
