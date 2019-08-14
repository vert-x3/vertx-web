package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class PathExtensionTestServiceImpl implements PathExtensionTestService {
  @Override
  public void pathLevelGet(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("pathLevelGet")));
  }

  @Override
  public void getPathLevel(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("getPathLevel")));
  }

  @Override
  public void pathLevelPost(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("pathLevelPost")));
  }

  @Override
  public void postPathLevel(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("postPathLevel")));
  }
}
