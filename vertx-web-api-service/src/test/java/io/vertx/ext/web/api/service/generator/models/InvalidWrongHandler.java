package io.vertx.ext.web.api.service.generator.models;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface InvalidWrongHandler {

  void someMethod(ServiceRequest context, Handler<AsyncResult<Integer>> resultHandler);
}
