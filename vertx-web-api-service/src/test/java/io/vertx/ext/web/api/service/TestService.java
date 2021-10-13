package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@WebApiServiceGen
public interface TestService {
  void testA(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void testB(JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void testEmptyServiceResponse(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void testUser(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void extraPayload(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void testAuthorization(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }
}
