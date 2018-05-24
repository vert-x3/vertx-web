package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

public class FailureTestServiceImpl implements FailureTestService {
  Vertx vertx;

  public FailureTestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void testFailure(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler) {
    JsonObject body = context.getParams().getJsonObject("body");
    resultHandler.handle(Future.failedFuture(new Exception("error for " + body.getString("name"))));
  }
}
