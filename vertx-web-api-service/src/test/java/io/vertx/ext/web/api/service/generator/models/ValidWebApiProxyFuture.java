package io.vertx.ext.web.api.service.generator.models;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import io.vertx.ext.web.validation.RequestParameter;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface ValidWebApiProxyFuture {

  Future<ServiceResponse> testA(ServiceRequest context);

  Future<ServiceResponse> testB(Integer id, JsonObject body, ServiceRequest context);

  Future<ServiceResponse> testC(Integer id, RequestParameter body, ServiceRequest context);

  @ProxyClose
  void closeIt();

}
