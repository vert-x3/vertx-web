package io.vertx.ext.web.api.generator.models;

import io.vertx.ext.web.api.RequestContext;
import io.vertx.ext.web.api.generator.WebApiProxyGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiProxyGen
public interface MissingHandler {

  void someMethod(Integer id, RequestContext context);
}
