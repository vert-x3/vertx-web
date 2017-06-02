package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ValidationException;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class AnyOfTypeValidator implements ParameterTypeValidator {

  List<ParameterTypeValidator> validators;

  public AnyOfTypeValidator(List<ParameterTypeValidator> validators) {
    this.validators = validators;
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    for (ParameterTypeValidator validator : validators) {
      try {
        return validator.isValid(value);
      } catch (ValidationException e) {
      }
    }
    throw ValidationException.generateNotMatchValidationException(value + " doesn't match anyOf schemas");
  }
}
