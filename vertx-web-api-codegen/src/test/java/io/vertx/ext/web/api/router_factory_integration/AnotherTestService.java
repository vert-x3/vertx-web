package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.ProxyClose;
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
interface AnotherTestService {

  void testC(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void testD(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void testE(Integer id, JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void testF(Integer id, RequestParameter body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void testDataObject(FilterData body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  @ProxyClose
  void close();

  static AnotherTestService create(Vertx vertx) {
    return new AnotherTestServiceImpl(vertx);
  }

}
