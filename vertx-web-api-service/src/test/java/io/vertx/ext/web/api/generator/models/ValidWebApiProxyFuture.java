package io.vertx.ext.web.api.generator.models;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface ValidWebApiProxyFuture {

  Future<OperationResponse> testA(OperationRequest context);

  Future<OperationResponse> testB(Integer id, JsonObject body, OperationRequest context);

  Future<OperationResponse> testC(Integer id, RequestParameter body, OperationRequest context);

  @ProxyClose
  void closeIt();

}
