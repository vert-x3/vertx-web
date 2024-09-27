package io.vertx.ext.web.api.service.tests;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

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
