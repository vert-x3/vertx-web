package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.OperationRequest;

public class FailureTestServiceImpl implements FailureTestService {
  Vertx vertx;

  public FailureTestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void testFailure(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.failedFuture(new Exception("error for " + body.getString("name"))));
  }

  @Override
  public void testException(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    throw new IllegalArgumentException();
  }
}
