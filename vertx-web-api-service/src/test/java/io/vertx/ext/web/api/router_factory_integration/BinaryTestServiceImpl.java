package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class BinaryTestServiceImpl implements BinaryTestService {
  @Override
  public void binaryTest(OperationRequest request, Handler<AsyncResult<OperationResponse>> response) {
    final Buffer buffer = Buffer.buffer(new byte[] {(byte) 0xb0});

    response.handle(
      Future.succeededFuture(
        new OperationResponse(
          200,
          "OK",
          buffer,
          MultiMap
            .caseInsensitiveMultiMap()
            .add("content-type", "application/octet-stream")
        )
      )
    );
  }
}
