package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@WebApiServiceGen
public
interface FailureTestService {

  void testFailure(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void testException(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  static FailureTestService create(Vertx vertx) {
    return new FailureTestServiceImpl(vertx);
  }

}
