package io.vertx.ext.web.api.validation;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class SpecFeatureNotSupportedException extends RuntimeException {
  public SpecFeatureNotSupportedException(String message) {
    super(message);
  }
}
