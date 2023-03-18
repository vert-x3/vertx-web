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
  public Future<ServiceResponse> testC(ServiceRequest context) {
    JsonObject body = context.getParams().getJsonObject("body");
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(new JsonObject().put("anotherResult", body.getString("name") + " " + body.getString("hello") + "!")))
    ;
  }

  @Override
  public Future<ServiceResponse> testD(ServiceRequest context) {
    JsonObject body = context.getParams().getJsonObject("body");
    return Future.succeededFuture(
      ServiceResponse.completedWithJson(
        new JsonObject()
          .put("content-type", context.getHeaders().get(HttpHeaders.CONTENT_TYPE))
          .put("anotherResult", body.getString("name") + " " + body.getString("hello") + "?")
      )
    );
  }

  @Override
  public Future<ServiceResponse> testE(Integer id, JsonObject body, ServiceRequest context) {
    return
      Future.succeededFuture(
        ServiceResponse.completedWithJson(new JsonObject().put("id", id).put("value", body.getValue("value")))
      )
    ;
  }

  @Override
  public Future<ServiceResponse> testF(Integer id, RequestParameter body, ServiceRequest context) {
    if (body.isJsonArray())
      return
        Future.succeededFuture(
          ServiceResponse.completedWithJson(new JsonArray(body.getJsonArray().stream().map(i -> id + (Integer)i).collect(Collectors.toList())))
        )
      ;
    else
      return testE(id, body.getJsonObject(), context);
  }

  @Override
  public Future<ServiceResponse> testDataObject(FilterData body, ServiceRequest context) {
    JsonObject r = body.toJson();
    r.remove("message");
    return Future.succeededFuture(ServiceResponse.completedWithJson(r));
  }

  @Override
  public void close() {

  }
}
