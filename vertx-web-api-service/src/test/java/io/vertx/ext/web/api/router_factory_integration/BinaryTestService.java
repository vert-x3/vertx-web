package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
public interface BinaryTestService {

  void binaryTest(
    final OperationRequest request,
    final Handler<AsyncResult<OperationResponse>> response);

}
