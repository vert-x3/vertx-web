package io.vertx.ext.web.api.validation;

/**
 * @author Francesco Guardiani @slinkydeveloper
 * @deprecated You should use the new module vertx-web-openapi
 */
@Deprecated
public class SpecFeatureNotSupportedException extends RuntimeException {
  public SpecFeatureNotSupportedException(String message) {
    super(message);
  }
}
