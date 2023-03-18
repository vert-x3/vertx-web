package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

public class BinaryTestServiceImpl implements BinaryTestService {

  @Override
  public Future<ServiceResponse> binaryTest(ServiceRequest request) {
    final Buffer buffer = Buffer.buffer(new byte[] {(byte) 0xb0});
    return
      Future.succeededFuture(
        new ServiceResponse(
          200,
          "OK",
          buffer,
          MultiMap
            .caseInsensitiveMultiMap()
            .add("content-type", "application/octet-stream")
        )
      )
    ;
  }

}
