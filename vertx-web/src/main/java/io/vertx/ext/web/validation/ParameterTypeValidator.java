package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;

/**
 * Interface for declaration of method for validate a specific parameter type
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface ParameterTypeValidator {
  /**
   * Function that check if parameter is valid
   * @param value value of parameter to test
   * @return true if parameter is valid
   */
  boolean isValid(String value);
}
