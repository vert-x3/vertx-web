package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.PatternTypeValidator;

import java.util.regex.Pattern;

/**
 * ParameterType contains regular expressions for parameter validation. Use it to describe parameter type
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ParameterType {
  /**
   * STRING Type accept every string
   */
  STRING(null), //TODO add regexp
  /**
   * EMAIL does validation with pattern: ^(?:[\w!#\$%&'\*\+\-/=\?\^`\{\|\}~]+\.)*[\w!#\$%&'\*\+\-/=\?\^`\{\|\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\-](?!\.)){0,61}[a-zA-Z0-9]?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\[(?:(?:[01]?\d{1,2}|2[0-4]\d|25[0-5])\.){3}(?:[01]?\d{1,2}|2[0-4]\d|25[0-5])\]))$
   */
  EMAIL(new PatternTypeValidator("^(?:[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+\\.)*[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$")),
  /**
   * URI does validation with pattern: ^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$
   */
  URI(new PatternTypeValidator("^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$")),
  /**
   * BOOL pattern: ^(?i)(true|false|t|f|1|0)$
   * It allows true, false, t, f, 1, 0
   */
  BOOL(new PatternTypeValidator("^(?i)(true|false|t|f|1|0)$")),
  /**
   * INT type does the validation with Integer.parseInt(value)
   */
  INT((value) -> {
    try {
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }),
  /**
   * FLOAT type does the validation with Float.parseFloat(value)
   */
  FLOAT((value) -> {
    try {
      Float.parseFloat(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }),
  /**
   * DOUBLE type does the validation with Double.parseDouble(value)
   */
  DOUBLE((value) -> {
    try {
      Double.parseDouble(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }),
  /**
   * DATE as defined by full-date - RFC3339
   */
  DATE(new PatternTypeValidator("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}$")),
  /**
   * DATETIME as defined by date-time - RFC3339
   */
  DATETIME(new PatternTypeValidator("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$")),
  /**
   * TIME as defined by partial-time - RFC3339
   */
  TIME(new PatternTypeValidator("^\\d{2}:\\d{2}:\\d{2}$")),
  /**
   * BASE64 does validation with pattern: ^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$
   */
  BASE64(new PatternTypeValidator("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$"));

  private ParameterTypeValidator validationMethod;

  ParameterType(ParameterTypeValidator validationMethod) {
    this.validationMethod = validationMethod;
  }

  public ParameterTypeValidator getValidationMethod() {
    return validationMethod;
  }

  public boolean validate(String value) {
    if (validationMethod != null)
      return validationMethod.isValid(value);
    else
      return true;
  }
}
