package io.vertx.ext.web.api.generator.models;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface InvalidMissingContext {

  void someMethod(Integer id, Handler<AsyncResult<OperationResponse>> resultHandler);
}
