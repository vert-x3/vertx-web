package io.vertx.ext.web.openapi.service;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
public interface TestService {
  void testA(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void testB(JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void testEmptyServiceResponse(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void testUser(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
  void extraPayload(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }
}
