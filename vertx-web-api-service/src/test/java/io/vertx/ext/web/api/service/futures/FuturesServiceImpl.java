package io.vertx.ext.web.api.service.futures;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.validation.RequestParameter;

public class FuturesServiceImpl implements FuturesService {

  @Override
  public Future<ServiceResponse> testFutureWithRequestParameter(final RequestParameter param, final ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithJson(new JsonObject().put("param", param.getInteger())));
  }

  @Override
  public Future<ServiceResponse> testFutureWithIntParameter(final int param, final ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithJson(new JsonObject().put("param", param)));
  }

  @Override
  public Future<ServiceResponse> testFuture(final ServiceRequest context) {
    return Future.succeededFuture(ServiceResponse.completedWithJson(new JsonObject().put("foo", "bar")));
  }
}
