package io.vertx.ext.web.openapi.service;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

@WebApiServiceGen
@VertxGen
public interface PathExtensionTestService {
  Future<ServiceResponse> pathLevelGet(ServiceRequest context);

  Future<ServiceResponse> getPathLevel(ServiceRequest context);

  Future<ServiceResponse> pathLevelPost(ServiceRequest context);

  Future<ServiceResponse> postPathLevel(ServiceRequest context);
}
