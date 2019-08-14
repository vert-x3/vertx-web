package io.vertx.ext.web.api.contract;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.api.validation.ValidationHandler;

/**
 * Base interface for HTTP request validation with API specification
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen(concrete = false)
public interface HTTPOperationRequestValidationHandler extends ValidationHandler {
  /**
   * Function that parse the operation specification and generate validation rules
   */
  void parseOperationSpec();
}
