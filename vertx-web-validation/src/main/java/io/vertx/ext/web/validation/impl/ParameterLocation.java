package io.vertx.ext.web.validation.impl;

import io.vertx.codegen.annotations.VertxGen;

/**
 * ParameterLocation describe the location of parameter inside HTTP Request
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ParameterLocation {
  HEADER,
  QUERY,
  PATH,
  COOKIE;

  public String lowerCaseIfNeeded(String parameterName) {
    return this == HEADER ? parameterName.toLowerCase() : parameterName;
  }

}
