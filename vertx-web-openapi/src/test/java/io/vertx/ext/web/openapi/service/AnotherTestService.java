package io.vertx.ext.web.openapi.service;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import io.vertx.ext.web.validation.RequestParameter;

@WebApiServiceGen
@VertxGen
public interface AnotherTestService {

  Future<ServiceResponse> testC(ServiceRequest context);

  Future<ServiceResponse> testD(ServiceRequest context);

  Future<ServiceResponse> testE(Integer id, JsonObject body, ServiceRequest context);

  Future<ServiceResponse> testF(Integer id, RequestParameter body, ServiceRequest context);

  Future<ServiceResponse> testDataObject(FilterData body, ServiceRequest context);

  @ProxyClose
  void close();

  static AnotherTestService create(Vertx vertx) {
    return new AnotherTestServiceImpl(vertx);
  }

}
