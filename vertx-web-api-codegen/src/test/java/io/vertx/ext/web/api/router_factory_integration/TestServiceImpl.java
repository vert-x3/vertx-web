package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

public class TestServiceImpl implements TestService {
  Vertx vertx;

  public TestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }


  @Override
  public void testA(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      OperationResult.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "!")))
    );
  }

  @Override
  public void testB(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.succeededFuture(
      OperationResult.completedWithJson(new JsonObject().put("result", body.getString("hello") + " " + body.getString("name") + "?")))
    );
  }
}
