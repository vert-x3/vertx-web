package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.HTTPOperationRequestValidationHandler;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class HTTPOperationRequestValidationHandlerImpl<OperationSpecType> extends BaseValidationHandler implements HTTPOperationRequestValidationHandler {
  protected OperationSpecType pathSpec;

  public HTTPOperationRequestValidationHandlerImpl(OperationSpecType pathSpec) {
    this.pathSpec = pathSpec;
    parseOperationSpec();
  }
}
