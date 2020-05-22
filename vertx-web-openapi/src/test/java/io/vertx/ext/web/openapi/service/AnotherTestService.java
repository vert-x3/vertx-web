package io.vertx.ext.web.openapi.service;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
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

  void testC(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void testD(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void testE(Integer id, JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void testF(Integer id, RequestParameter body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void testDataObject(FilterData body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  @ProxyClose
  void close();

  static AnotherTestService create(Vertx vertx) {
    return new AnotherTestServiceImpl(vertx);
  }

}
