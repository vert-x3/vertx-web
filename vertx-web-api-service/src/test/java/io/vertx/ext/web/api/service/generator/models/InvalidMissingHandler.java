package io.vertx.ext.web.api.service.generator.models;

import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface InvalidMissingHandler {

  void someMethod(Integer id, ServiceRequest context);
}
