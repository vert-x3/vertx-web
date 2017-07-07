package io.vertx.ext.web.designdriven.impl;

import io.vertx.ext.web.designdriven.HTTPOperationRequestValidationHandler;
import io.vertx.ext.web.validation.impl.BaseValidationHandler;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class HTTPOperationRequestValidationHandlerImpl<OperationSpecType> extends BaseValidationHandler
  implements HTTPOperationRequestValidationHandler {
  protected OperationSpecType pathSpec;

  public HTTPOperationRequestValidationHandlerImpl(OperationSpecType pathSpec) {
    this.pathSpec = pathSpec;
  }
}
