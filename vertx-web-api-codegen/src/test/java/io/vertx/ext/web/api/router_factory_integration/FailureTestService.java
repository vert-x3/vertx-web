package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.generator.WebApiProxyGen;

@WebApiProxyGen
@VertxGen
interface FailureTestService {

  void testFailure(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testException(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  static FailureTestService create(Vertx vertx) {
    return new FailureTestServiceImpl(vertx);
  }

}
