package io.vertx.ext.web.api.service.tests.generator.models;

import io.vertx.core.Future;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;

/**
 * @author <a href="https://github.com/slinkydeveloper">Francesco Guardiani</a>
 */
@WebApiServiceGen
public interface InvalidMissingContext {

  Future<ServiceResponse> someMethod(Integer id);
}
