package io.vertx.ext.web.openapi.future.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;

public class TestFutureServiceImpl implements TestFutureService {
  Vertx vertx;

  public TestFutureServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Future<ServiceResponse> testA(ServiceRequest context) {
    JsonObject body = context.getParams().getJsonObject("body");
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "!"))
    );
  }

  @Override
  public Future<ServiceResponse> testB(JsonObject body, ServiceRequest context) {
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "?"))
    );
  }

  @Override
  public Future<ServiceResponse> testEmptyServiceResponse(ServiceRequest context) {
    return Future.succeededFuture(
      new ServiceResponse()
    );
  }

  @Override
  public Future<ServiceResponse> testUser(ServiceRequest context) {
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getUser().getString("username") + "!"))
    );
  }

  @Override
  public Future<ServiceResponse> extraPayload(ServiceRequest context) {
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getExtra().getString("username") + "!"))
    );
  }
}
