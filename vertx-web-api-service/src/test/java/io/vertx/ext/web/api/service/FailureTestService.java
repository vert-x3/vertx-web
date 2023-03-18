package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@WebApiServiceGen
public
interface FailureTestService {

  @Deprecated
  Future<ServiceResponse> testFailure(ServiceRequest context);

  @Deprecated
  Future<ServiceResponse> testException(ServiceRequest context);

  static FailureTestService create(Vertx vertx) {
    return new FailureTestServiceImpl(vertx);
  }

}
