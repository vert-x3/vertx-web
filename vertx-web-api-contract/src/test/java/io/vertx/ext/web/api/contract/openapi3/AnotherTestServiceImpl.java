package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

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
  public void testF(Integer id, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    Object body = context.getParams().getValue("body");
    if (body instanceof JsonArray)
      resultHandler.handle(
        Future.succeededFuture(
          OperationResult.completedWithJsonPayload(new JsonArray(((JsonArray)body).stream().map(i -> id + (Integer)i).collect(Collectors.toList())))
        )
      );
    else
      testE(id, (JsonObject)body, context, resultHandler);
  }
}
