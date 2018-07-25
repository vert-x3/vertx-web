package io.vertx.ext.web.api.generator.models;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface ValidWebApiProxy {

  void testA(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testB(Integer id, JsonObject body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testC(Integer id, RequestParameter body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  @ProxyClose
  void closeIt();

}
