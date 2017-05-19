package io.vertx.ext.web.validation.impl;

import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.vertx.ext.web.validation.ParameterType;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class Swagger2RequestValidatorImpl extends HTTPOperationRequestValidationHandlerImpl<Operation> implements io.vertx.ext.web.validation.Swagger2RequestValidator {

  public Swagger2RequestValidatorImpl(Operation operation) {
    super(operation);
  }

  /**
   * Function that parse the operation specification and generate validation rules
   */
  @Override
  public void parseOperationSpec() {
    // Extract from path spec parameters description
    for (Parameter opParameter : this.pathSpec.getParameters()) {
      this.parseParameter(opParameter);
    }
  }

  private ParameterType parseIncludedType(Parameter parameter) {
    //switch (parameter): //TODO
    return null;
  }

  private void parseParameter(Parameter parameter) {
  }
}
