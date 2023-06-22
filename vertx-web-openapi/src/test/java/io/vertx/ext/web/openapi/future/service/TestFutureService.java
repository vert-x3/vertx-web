package io.vertx.ext.web.openapi.future.service;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
public interface TestFutureService {
  Future<ServiceResponse> testA(ServiceRequest context);
  Future<ServiceResponse> testB(JsonObject body, ServiceRequest context);
  Future<ServiceResponse> testEmptyServiceResponse(ServiceRequest context);
  Future<ServiceResponse> testUser(ServiceRequest context);
  Future<ServiceResponse> extraPayload(ServiceRequest context);

  static TestFutureService create(Vertx vertx) {
    return new TestFutureServiceImpl(vertx);
  }
}
