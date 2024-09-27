package io.vertx.ext.web.api.service.tests;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;

public class FailureTestServiceImpl implements FailureTestService {
  Vertx vertx;

  public FailureTestServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Future<ServiceResponse> testFailure(ServiceRequest context) {
    JsonObject body = context.getParams().getJsonObject("body");
    return Future.failedFuture(new Exception("error for " + body.getString("name")));
  }

  @Override
  public Future<ServiceResponse> testException(ServiceRequest context) {
    JsonObject body = context.getParams().getJsonObject("body");
    throw new IllegalArgumentException();
  }
}
