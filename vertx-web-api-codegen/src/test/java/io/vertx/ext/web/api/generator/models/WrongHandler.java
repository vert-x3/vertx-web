package io.vertx.ext.web.api.generator.models;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.generator.WebApiProxyGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiProxyGen
public interface WrongHandler {

  void someMethod(RequestContext context, Handler<AsyncResult<Integer>> resultHandler);
}
