package io.vertx.ext.web.openapi.service;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
public interface PathExtensionTestService {
  void pathLevelGet(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void getPathLevel(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void pathLevelPost(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void postPathLevel(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
}
