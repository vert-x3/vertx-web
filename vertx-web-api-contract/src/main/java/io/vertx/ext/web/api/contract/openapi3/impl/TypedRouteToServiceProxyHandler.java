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

public class TypedRouteToServiceProxyHandler implements Handler<RoutingContext> {

  EventBus eventBus;
  String address;
  DeliveryOptions deliveryOptions;

  public TypedRouteToServiceProxyHandler(EventBus eventBus, String address, DeliveryOptions deliveryOptions) {
    this.eventBus = eventBus;
    this.address = address;
    this.deliveryOptions = deliveryOptions;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    eventBus.send(address, buildPayload(routingContext), deliveryOptions, (AsyncResult<Message<JsonObject>> res) -> {
      if (res.succeeded()) {
        OperationResult op = new OperationResult(res.result().body());
        HttpServerResponse response = routingContext.response().setStatusCode(op.getStatusCode());
        op.getHeaders().forEach(h -> response.putHeader(h.getKey(), h.getValue()));
        response.end(op.getPayload().toString());
      } else {
        routingContext.fail(res.cause());
      }
    });
  }

  private static JsonObject buildPayload(RoutingContext context) {
    RequestParameters params = context.get("parsedParameters");
    RequestContext ctx = new RequestContext(
      context.request().headers(),
      params.toJsonObject()
    );
    JsonObject result = new JsonObject().put("context", ctx.toJson());
    params.pathParametersNames().forEach(s -> result.put(s, params.pathParameter(s).toJson()));
    params.queryParametersNames().forEach(s -> result.put(s, params.queryParameter(s).toJson()));
    params.headerParametersNames().forEach(s -> result.put(s, params.headerParameter(s).toJson()));
    params.cookieParametersNames().forEach(s -> result.put(s, params.cookieParameter(s).toJson()));
    // TODO merge form parameters in body json!
    if (params.body() != null) result.put("body", params.body().toJson());
    return result;
  }

  public static TypedRouteToServiceProxyHandler build(EventBus eventBus, String address, String actionName) {
    return new TypedRouteToServiceProxyHandler(eventBus, address, new DeliveryOptions().addHeader("action", actionName));
  }

  public static TypedRouteToServiceProxyHandler build(EventBus eventBus, String address, String actionName, JsonObject deliveryOptions) {
    DeliveryOptions opt = new DeliveryOptions(deliveryOptions).addHeader("action", actionName);
    return new TypedRouteToServiceProxyHandler(eventBus, address, opt);
  }

}
