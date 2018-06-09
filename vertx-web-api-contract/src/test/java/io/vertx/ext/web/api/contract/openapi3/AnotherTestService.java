package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.codegen.annotations.WebApiProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResult;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.RequestParameter;

@WebApiProxyGen
@VertxGen
interface AnotherTestService {

  void testC(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testD(RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testE(Integer id, JsonObject body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testF(Integer id, RequestParameter body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  void testDataObject(FilterData body, RequestContext context, Handler<AsyncResult<OperationResult>> resultHandler);

  @ProxyIgnore
  void bla();

  @ProxyClose
  void close();

  static AnotherTestService create(Vertx vertx) {
    return new AnotherTestServiceImpl(vertx);
  }

}
