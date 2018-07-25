package io.vertx.ext.web.api.generator.models;

import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface MissingHandler {

  void someMethod(Integer id, RequestContext context);
}
