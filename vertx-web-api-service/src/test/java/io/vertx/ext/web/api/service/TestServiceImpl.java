package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class TestServiceImpl implements TestService {
  Vertx vertx;

  public TestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void testA(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "!")))
    );
  }

  @Override
  public void testB(JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "?")))
    );
  }

  @Override
  public void testEmptyServiceResponse(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      new ServiceResponse()
    ));
  }

  @Override
  public void testUser(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getUser().getString("username") + "!")))
    );
  }

  @Override
  public void extraPayload(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getExtra().getString("username") + "!")))
    );
  }

  @Override
  public void testAuthorization(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("result", context.getHeaders().get("Authorization"))))
    );
  }
}
