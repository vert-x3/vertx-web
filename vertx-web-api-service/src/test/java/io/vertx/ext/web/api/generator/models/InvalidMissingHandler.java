package io.vertx.ext.web.api.generator.models;

import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface InvalidMissingHandler {

  void someMethod(Integer id, OperationRequest context);
}
