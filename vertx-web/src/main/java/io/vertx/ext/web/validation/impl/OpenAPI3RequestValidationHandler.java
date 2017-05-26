package io.vertx.ext.web.validation.impl;

import com.reprezen.kaizen.oasparser.model3.Operation;
import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.RequestBody;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ParameterValidationRule;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RequestValidationHandler extends HTTPOperationRequestValidationHandlerImpl<Operation> implements io.vertx.ext.web.validation.OpenAPI3RequestValidationHandler {

  public OpenAPI3RequestValidationHandler(Operation pathSpec) {
    super(pathSpec);
  }

  @Override
  public void parseOperationSpec() {
    // Extract from path spec parameters description
    for (Parameter opParameter : this.pathSpec.getParameters()) {
      this.parseParameter(opParameter);
    }
    this.parseRequestBody(this.pathSpec.getRequestBody());
  }

  private ParameterTypeValidator resolveTypeValidator(Parameter parameter) {

  }

  private void parseParameter(Parameter parameter) {
  }

  private void parseRequestBody(RequestBody requestBody) {

  }
}
