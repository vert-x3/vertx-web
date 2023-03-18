package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

@WebApiServiceGen
public interface BinaryTestService {

  @Deprecated
  Future<ServiceResponse> binaryTest(final ServiceRequest request);

}
