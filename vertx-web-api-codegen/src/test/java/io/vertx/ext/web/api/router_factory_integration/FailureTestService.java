package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
interface FailureTestService {

  void testFailure(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void testException(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  static FailureTestService create(Vertx vertx) {
    return new FailureTestServiceImpl(vertx);
  }

}
