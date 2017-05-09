package io.vertx.ext.web.validator;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public class HTTPSpecRequestValidator<PathSpecType> extends BaseValidator {
  PathSpecType pathSpec;

  public HTTPSpecRequestValidator(PathSpecType pathSpec) {
    this.pathSpec = pathSpec;
  }
}
