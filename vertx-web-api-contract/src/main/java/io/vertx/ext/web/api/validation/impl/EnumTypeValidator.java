package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.impl.RequestParameterImpl;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class EnumTypeValidator<T> extends SingleValueParameterTypeValidator<T> {

  private List<T> allowedValues;
  private ParameterTypeValidator innerValidator;

  @SuppressWarnings("unchecked")
  public EnumTypeValidator(List<T> allowedValues, ParameterTypeValidator innerValidator) {
    super((innerValidator != null) ? (T) innerValidator.getDefault() : null);
    this.innerValidator = innerValidator;
    this.allowedValues = allowedValues;
  }

  @Override
  public RequestParameter isValidSingleParam(String value) {
    RequestParameterImpl parsedValue = (RequestParameterImpl)(
      (this.innerValidator != null) ? innerValidator.isValid(value) : RequestParameter.create(value)
    );
    if (!allowedValues.contains(parsedValue.getValue()))
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException("Value " + value + " "
        + "in not inside enum list " + allowedValues.toString());
    else return parsedValue;
  }
}
