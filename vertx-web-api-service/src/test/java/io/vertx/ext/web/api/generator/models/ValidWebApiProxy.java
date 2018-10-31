package io.vertx.ext.web.api.generator.models;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface ValidWebApiProxy {

  void testA(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void testB(Integer id, JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void testC(Integer id, RequestParameter body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  @ProxyClose
  void closeIt();

}
