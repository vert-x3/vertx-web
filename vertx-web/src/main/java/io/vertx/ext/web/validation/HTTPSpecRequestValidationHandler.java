package io.vertx.ext.web.validation;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public class HTTPSpecRequestValidationHandler<PathSpecType> extends BaseValidationHandler {
  PathSpecType pathSpec;

  public HTTPSpecRequestValidationHandler(PathSpecType pathSpec) {
    this.pathSpec = pathSpec;
  }
}
