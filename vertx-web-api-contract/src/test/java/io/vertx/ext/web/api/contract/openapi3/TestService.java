package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;

@ProxyGen
@VertxGen
public interface TestService {
  // A couple of factory methods to create an instance and a proxy
  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }

  void testA(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);
}
