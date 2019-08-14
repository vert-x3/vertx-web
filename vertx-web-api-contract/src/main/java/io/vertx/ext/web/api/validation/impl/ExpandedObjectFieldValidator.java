package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.impl.RequestParameterImpl;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class ExpandedObjectFieldValidator implements ParameterTypeValidator {

  private ParameterTypeValidator innerValidator;
  private String objectName;
  private String fieldName;

  public ExpandedObjectFieldValidator(ParameterTypeValidator innerValidator, String objectName, String fieldName) {
    this.innerValidator = innerValidator;
    this.objectName = objectName;
    this.fieldName = fieldName;
  }

  /**
   * Function that checks if parameter is valid. It returns a RequestParameter object that will be linked inside
   * {@link RequestParameters}. For more info, check {@link RequestParameter}.
   *
   * @param value value of parameter to test
   * @return request parameter value
   */
  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    RequestParameter requestParameter = innerValidator.isValid(value);
    requestParameter.setName(fieldName);
    Map<String, RequestParameter> map = new HashMap<>();
    map.put(fieldName, requestParameter);
    return new RequestParameterImpl(objectName, map);
  }
}
