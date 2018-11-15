package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
public interface PathExtensionTestService {
  void pathLevelGet(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void getPathLevel(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void pathLevelPost(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void postPathLevel(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}
