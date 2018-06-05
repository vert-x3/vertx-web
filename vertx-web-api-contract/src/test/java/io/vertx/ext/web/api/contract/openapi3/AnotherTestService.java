package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

@ProxyGen
@VertxGen
interface AnotherTestService {

  void testC(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testD(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testE(Integer id, JsonObject body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testF(Integer id, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  static AnotherTestService create(Vertx vertx) {
    return new AnotherTestServiceImpl(vertx);
  }

}
