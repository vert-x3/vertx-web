package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
public interface TestService {
  void testA(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void testB(JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void testEmptyOperationResponse(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void testUser(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
  void extraPayload(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }
}
