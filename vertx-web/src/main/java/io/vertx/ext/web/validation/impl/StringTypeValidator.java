package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterTypeValidator;

import java.util.regex.Pattern;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class StringTypeValidator implements ParameterTypeValidator {

  private Pattern pattern;
  private Integer minLength;
  private Integer maxLength;

  public StringTypeValidator(String pattern, Integer minLength, Integer maxLength) {
    this.pattern = (pattern != null) ? Pattern.compile(pattern) : null;
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public StringTypeValidator(String pattern) {
    this(pattern, null, null);
  }

  public Pattern getPattern() {
    return pattern;
  }

  private boolean checkMinLength(String value) {
    if (minLength != null)
      return value.length() >= minLength;
    else return true;
  }

  private boolean checkMaxLength(String value) {
    if (maxLength != null)
      return value.length() <= maxLength;
    else return true;
  }

  /**
   * Function that check if parameter is valid
   *
   * @param value value of parameter to test
   * @return true if parameter is valid
   */
  @Override
  public boolean isValid(String value) {
    return (checkMinLength(value) && checkMaxLength(value) && (pattern == null || pattern.matcher(value).matches()));
  }
}
