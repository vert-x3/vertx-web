package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.generator.WebApiProxyGen;

@WebApiProxyGen
@VertxGen
interface AnotherTestService {

  void testC(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testD(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testE(Integer id, JsonObject body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testF(Integer id, RequestParameter body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testDataObject(FilterData body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  @ProxyClose
  void close();

  static AnotherTestService create(Vertx vertx) {
    return new AnotherTestServiceImpl(vertx);
  }

}
