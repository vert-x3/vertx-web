package io.vertx.ext.web.api.service.generator.models;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import io.vertx.ext.web.validation.RequestParameter;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface ValidWebApiProxy {

  void testA(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void testB(Integer id, JsonObject body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void testC(Integer id, RequestParameter body, ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  @ProxyClose
  void closeIt();

}
