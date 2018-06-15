package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.annotations.WebApiProxyGen;

@WebApiProxyGen
@VertxGen
interface FailureTestService {

  void testFailure(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testException(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  static FailureTestService create(Vertx vertx) {
    return new FailureTestServiceImpl(vertx);
  }

}
