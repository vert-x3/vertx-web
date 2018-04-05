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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
      OperationResult op = new OperationResult(res.result().body());
      HttpServerResponse response = routingContext.response().setStatusCode(op.getStatusCode());
      // op.getHeaders().forEach(e -> response.putHeader(e.getKey(), e.getValue()));
      response.end(op.getPayload().toString());
    });
  }

  private static JsonObject buildPayload(RoutingContext context) {
    return new JsonObject().put("context", new RequestContext(
      context.request().headers().entries().stream().reduce(new HashMap<String, List<String>>(), (m, e) -> {
        if (!m.containsKey(e.getKey()))
          m.put(e.getKey(), new ArrayList<>());
        m.get(e.getKey()).add(e.getValue());
        return m;
      }, (m1, m2) -> {
        m1.putAll(m2);
        return m1;
      }),
      ((RequestParameters)context.get("parsedParameters")).toJsonObject()
    ).toJson());
  }

}
