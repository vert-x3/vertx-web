package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.codegen.annotations.OpenApiProxyGen;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

@OpenApiProxyGen
@VertxGen
public interface TestService {
  void testA(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testB(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }
}
