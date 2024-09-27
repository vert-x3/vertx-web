package io.vertx.ext.web.api.service.tests;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

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
