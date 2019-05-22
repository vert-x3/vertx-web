package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationCookie;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

import java.util.Collections;

public class TestServiceImpl implements TestService {
  Vertx vertx;

  public TestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void testA(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      OperationResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "!")))
    );
  }

  @Override
  public void testB(JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      OperationResponse.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "?")))
    );
  }

  @Override
  public void testEmptyOperationResponse(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      new OperationResponse()
    ));
  }

  @Override
  public void testUser(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      OperationResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getUser().getString("username") + "!")))
    );
  }

  @Override
  public void extraPayload(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(
      OperationResponse.completedWithJson(new JsonObject().put("result", "Hello " + context.getExtra().getString("username") + "!")))
    );
  }

  @Override
  public void testCookie(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    OperationCookie cookie = new OperationCookie("test", "test-value");
    resultHandler.handle(Future.succeededFuture(
      OperationResponse.completedWithPlainText(Buffer.buffer("hello")).setCookies(Collections.singletonList(cookie))
    ));
  }
}
