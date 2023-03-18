package io.vertx.ext.web.openapi.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;

public class PathExtensionTestServiceImpl implements PathExtensionTestService {
  @Override
  public Future<ServiceResponse> pathLevelGet(ServiceRequest context) {
    return Future.succeededFuture(new ServiceResponse().setStatusMessage("pathLevelGet"));
  }

  @Override
  public Future<ServiceResponse> getPathLevel(ServiceRequest context) {
    return Future.succeededFuture(new ServiceResponse().setStatusMessage("getPathLevel"));
  }

  @Override
  public Future<ServiceResponse> pathLevelPost(ServiceRequest context) {
    return Future.succeededFuture(new ServiceResponse().setStatusMessage("pathLevelPost"));
  }

  @Override
  public Future<ServiceResponse> postPathLevel(ServiceRequest context) {
    return Future.succeededFuture(new ServiceResponse().setStatusMessage("postPathLevel"));
  }
}
