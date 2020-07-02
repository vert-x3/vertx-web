package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.api.validation.ValidationHandler;

/**
 * Base interface for HTTP request validation with API specification
 *
 * @author Francesco Guardiani @slinkydeveloper
 * @deprecated You should use the new module vertx-web-openapi
 */
@VertxGen(concrete = false)
@Deprecated
public interface HTTPOperationRequestValidationHandler extends ValidationHandler {
  /**
   * Function that parse the operation specification and generate validation rules
   */
  void parseOperationSpec();
}
