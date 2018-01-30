package io.vertx.ext.web.api.validation.impl;

import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ValidationException;

import java.util.regex.Pattern;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class StringTypeValidator extends SingleValueParameterTypeValidator<String> {

  private Pattern pattern;
  private Integer minLength;
  private Integer maxLength;

  public StringTypeValidator(String pattern, Integer minLength, Integer maxLength, String defaultValue) {
    super(defaultValue);
    this.pattern = (pattern != null) ? Pattern.compile(pattern) : null;
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public StringTypeValidator(String pattern, String defaultValue) {
    this(pattern, null, null, defaultValue);
  }

  public StringTypeValidator(String pattern) {
    this(pattern, null, null, null);
  }

  public Pattern getPattern() {
    return pattern;
  }

  private boolean checkMinLength(String value) {
    if (minLength != null) return value.length() >= minLength;
    else return true;
  }

  private boolean checkMaxLength(String value) {
    if (maxLength != null) return value.length() <= maxLength;
    else return true;
  }

  @Override
  public RequestParameter isValidSingleParam(String value) {
    if (!checkMinLength(value) || !checkMaxLength(value) || (pattern != null && !pattern.matcher(value).matches()))
      throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(null);
    else return RequestParameter.create(value);
  }
}
