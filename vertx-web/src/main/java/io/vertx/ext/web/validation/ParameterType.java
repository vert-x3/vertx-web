package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.StringTypeValidator;

/**
 * ParameterType contains regular expressions for parameter validation. Use it to describe parameter type
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ParameterType {
  /**
   * STRING Type accept every string
   */
  GENERIC_STRING(value -> {
  }),
  /**
   * EMAIL does validation with pattern: ^(?:[\w!#\$%&'\*\+\-/=\?\^`\{\|\}~]+\.)*[\w!#\$%&'\*\+\-/=\?\^`\{\|\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\-](?!\.)){0,61}[a-zA-Z0-9]?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\[(?:(?:[01]?\d{1,2}|2[0-4]\d|25[0-5])\.){3}(?:[01]?\d{1,2}|2[0-4]\d|25[0-5])\]))$
   */
  EMAIL(new StringTypeValidator("^(?:[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+\\.)*[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$")),
  /**
   * URI does validation with pattern: ^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$
   */
  URI(new StringTypeValidator("^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$")),
  /**
   * BOOL pattern: ^(?i)(true|false|t|f|1|0)$
   * It allows true, false, t, f, 1, 0
   */
  BOOL(new StringTypeValidator("^(?i)(true|false|t|f|1|0)$")),
  /**
   * INT type does the validation with Integer.parseInt(value)
   */
  INT(ParameterTypeValidator.createIntegerTypeValidator()),
  /**
   * FLOAT type does the validation with Float.parseFloat(value)
   */
  FLOAT(ParameterTypeValidator.createFloatTypeValidator()),
  /**
   * DOUBLE type does the validation with Double.parseDouble(value)
   */
  DOUBLE(ParameterTypeValidator.createDoubleTypeValidator()),
  /**
   * DATE as defined by full-date - RFC3339
   */
  DATE(new StringTypeValidator("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}$")),
  /**
   * DATETIME as defined by date-time - RFC3339
   */
  DATETIME(new StringTypeValidator("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$")),
  /**
   * TIME as defined by partial-time - RFC3339
   */
  TIME(new StringTypeValidator("^\\d{2}:\\d{2}:\\d{2}$")),
  /**
   * BASE64 does validation with pattern: ^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$
   */
  BASE64(new StringTypeValidator("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$")),

  IPV4(new StringTypeValidator("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")),

  IPV6(new StringTypeValidator("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$")),

  HOSTNAME(new StringTypeValidator("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$"));

  private ParameterTypeValidator validationMethod;

  ParameterType(ParameterTypeValidator validationMethod) {
    this.validationMethod = validationMethod;
  }

  public ParameterTypeValidator getValidationMethod() {
    return validationMethod;
  }
}
