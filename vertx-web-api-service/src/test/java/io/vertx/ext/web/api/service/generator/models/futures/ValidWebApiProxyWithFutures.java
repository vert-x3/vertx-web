package io.vertx.ext.web.api.service.generator.models.futures;

import io.vertx.core.Future;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import io.vertx.ext.web.validation.RequestParameter;

@WebApiServiceGen
public interface ValidWebApiProxyWithFutures {

  Future<ServiceResponse> testFutureWithRequestParameter(RequestParameter param, ServiceRequest context);

  Future<ServiceResponse> testFutureWithParam(String param, ServiceRequest context);

  Future<ServiceResponse> testFuture(ServiceRequest context);
}
