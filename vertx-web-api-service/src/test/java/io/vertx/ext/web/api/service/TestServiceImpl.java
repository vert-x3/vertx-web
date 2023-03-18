package io.vertx.ext.web.api.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class TestServiceImpl implements TestService {
  Vertx vertx;

  public TestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Future<ServiceResponse> testA(ServiceRequest context) {
    JsonObject body = context.getParams().getJsonObject("body");
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "!")))
    ;
  }

  @Override
  public Future<ServiceResponse> testB(JsonObject body, ServiceRequest context) {
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "?")))
    ;
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
      ServiceResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getUser().getString("username") + "!")))
    ;
  }

  @Override
  public Future<ServiceResponse> extraPayload(ServiceRequest context) {
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getExtra().getString("username") + "!")))
    ;
  }

  @Override
  public Future<ServiceResponse> testAuthorization(ServiceRequest context) {
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", context.getHeaders().get("Authorization"))))
    ;
  }
}
