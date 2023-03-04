package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@WebApiServiceGen
public interface TestService {
  @Deprecated
  void testA(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  @Deprecated
  void testB(JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  @Deprecated
  void testEmptyServiceResponse(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  @Deprecated
  void testUser(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  @Deprecated
  void extraPayload(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  @Deprecated
  void testAuthorization(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }
}
