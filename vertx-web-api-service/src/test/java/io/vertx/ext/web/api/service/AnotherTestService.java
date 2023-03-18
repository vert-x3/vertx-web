package io.vertx.ext.web.api.service;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestParameter;

@WebApiServiceGen
public
interface AnotherTestService {

  @Deprecated
  Future<ServiceResponse> testC(ServiceRequest context);

  @Deprecated
  Future<ServiceResponse> testD(ServiceRequest context);

  @Deprecated
  Future<ServiceResponse> testE(Integer id, JsonObject body, ServiceRequest context);

  @Deprecated
  Future<ServiceResponse> testF(Integer id, RequestParameter body, ServiceRequest context);

  @Deprecated
  Future<ServiceResponse> testDataObject(FilterData body, ServiceRequest context);

  @ProxyClose
  void close();

  static AnotherTestService create(Vertx vertx) {
    return new AnotherTestServiceImpl(vertx);
  }

}
