package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OneOfTypeValidator implements ParameterTypeValidator {

  List<ParameterTypeValidator> validators;

  public OneOfTypeValidator(List<ParameterTypeValidator> validators) {
    this.validators = validators;
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    RequestParameter resultParam = null;
    for (ParameterTypeValidator validator : validators) {
      try {
        RequestParameter validatedParam = validator.isValid(value);
        if (validatedParam != null) {
          if (resultParam == null) resultParam = validatedParam;
          else
            throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(value + " match " +
              "" + "multiple schemas inside oneOf field");
        }
      } catch (ValidationException e) {
      }
    }
    if (resultParam != null) return resultParam;
    else
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(value + " doesn't " +
        "match any of oneOf schemas");
  }
}
