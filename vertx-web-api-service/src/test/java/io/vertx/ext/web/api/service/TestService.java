package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@WebApiServiceGen
public interface TestService {
  @Deprecated
  Future<ServiceResponse> testA(ServiceRequest context);
  @Deprecated
  Future<ServiceResponse> testB(JsonObject body, ServiceRequest context);
  @Deprecated
  Future<ServiceResponse> testEmptyServiceResponse(ServiceRequest context);
  @Deprecated
  Future<ServiceResponse> testUser(ServiceRequest context);
  @Deprecated
  Future<ServiceResponse> extraPayload(ServiceRequest context);
  @Deprecated
  Future<ServiceResponse> testAuthorization(ServiceRequest context);

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }
}
