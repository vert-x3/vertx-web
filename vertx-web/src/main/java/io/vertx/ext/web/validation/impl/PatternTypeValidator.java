package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ParameterTypeValidator;

import java.util.regex.Pattern;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class PatternTypeValidator implements ParameterTypeValidator {

  private Pattern pattern;

  public PatternTypeValidator(String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  public Pattern getPattern() {
    return pattern;
  }

  /**
   * Function that check if parameter is valid
   *
   * @param value value of parameter to test
   * @return true if parameter is valid
   */
  @Override
  public boolean isValid(String value) {
    return pattern.matcher(value).matches();
  }
}
