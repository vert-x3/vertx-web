package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.RequestParameter;

import java.util.stream.Collectors;

public class AnotherTestServiceImpl implements AnotherTestService {
  Vertx vertx;

  public AnotherTestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void testC(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      OperationResult.completedWithJsonPayload(new JsonObject().put("anotherResult", body.getString("name") + " " + body.getString("hello") + "!")))
    );
  }

  @Override
  public void testD(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      OperationResult.completedWithJsonPayload(
        new JsonObject()
          .put("content-type", context.getHeaders().get(HttpHeaders.CONTENT_TYPE))
          .put("anotherResult", body.getString("name") + " " + body.getString("hello") + "?")
      )
    ));
  }

  @Override
  public void testE(Integer id, JsonObject body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    resultHandler.handle(
      Future.succeededFuture(
        OperationResult.completedWithJsonPayload(body.copy().put("id", id))
      )
    );
  }

  @Override
  public void testF(Integer id, RequestParameter body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    if (body.isJsonArray())
      resultHandler.handle(
        Future.succeededFuture(
          OperationResult.completedWithJsonPayload(new JsonArray(body.getJsonArray().stream().map(i -> id + (Integer)i).collect(Collectors.toList())))
        )
      );
    else
      testE(id, body.getJsonObject(), context, resultHandler);
  }

  @Override
  public void testDataObject(FilterData body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    JsonObject r = body.toJson();
    r.remove("message");
    resultHandler.handle(Future.succeededFuture(OperationResult.completedWithJsonPayload(r)));
  }

  @Override
  public void bla() {

  }

  @Override
  public void close() {

  }
}
