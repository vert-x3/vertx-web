package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestParameter;

import java.util.stream.Collectors;

public class AnotherTestServiceImpl implements AnotherTestService {
  Vertx vertx;

  public AnotherTestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void testC(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("anotherResult", body.getString("name") + " " + body.getString("hello") + "!")))
    );
  }

  @Override
  public void testD(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      ServiceResponse.completedWithJson(
        new JsonObject()
          .put("content-type", context.getHeaders().get(HttpHeaders.CONTENT_TYPE))
          .put("anotherResult", body.getString("name") + " " + body.getString("hello") + "?")
      )
    ));
  }

  @Override
  public void testE(Integer id, JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(
      Future.succeededFuture(
        ServiceResponse.completedWithJson(new JsonObject().put("id", id).put("value", body.getValue("value")))
      )
    );
  }

  @Override
  public void testF(Integer id, RequestParameter body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    if (body.isJsonArray())
      resultHandler.handle(
        Future.succeededFuture(
          ServiceResponse.completedWithJson(new JsonArray(body.getJsonArray().stream().map(i -> id + (Integer)i).collect(Collectors.toList())))
        )
      );
    else
      testE(id, body.getJsonObject(), context, resultHandler);
  }

  @Override
  public void testDataObject(FilterData body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    JsonObject r = body.toJson();
    r.remove("message");
    resultHandler.handle(Future.succeededFuture(ServiceResponse.completedWithJson(r)));
  }

  @Override
  public void close() {

  }
}
