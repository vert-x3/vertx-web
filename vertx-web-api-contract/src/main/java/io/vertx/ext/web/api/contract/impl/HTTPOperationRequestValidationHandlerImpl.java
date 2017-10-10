package io.vertx.ext.web.api.contract.impl;

import io.vertx.ext.web.api.contract.HTTPOperationRequestValidationHandler;
import io.vertx.ext.web.api.validation.impl.BaseValidationHandler;

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
