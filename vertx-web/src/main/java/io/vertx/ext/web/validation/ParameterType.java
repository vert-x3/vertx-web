package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.validation.impl.BooleanTypeValidator;
import io.vertx.ext.web.validation.impl.RegularExpressions;
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
  GENERIC_STRING(value -> RequestParameter.create(value)),
  /**
   * EMAIL does validation with pattern: ^(?:[\w!#\$%&'\*\+\-/=\?\^`\{\|\}~]+\.)*[\w!#\$%&'\*\+\-/=\?\^`\{\|\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\-](?!\.)){0,61}[a-zA-Z0-9]?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\[(?:(?:[01]?\d{1,2}|2[0-4]\d|25[0-5])\.){3}(?:[01]?\d{1,2}|2[0-4]\d|25[0-5])\]))$
   */
  EMAIL(new StringTypeValidator(RegularExpressions.EMAIL)),
  /**
   * URI does validation with pattern: ^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$
   */
  URI(new StringTypeValidator(RegularExpressions.URI)),
  /**
   * BOOL pattern: ^(?i)(true|false|t|f|1|0)$
   * It allows true, false, t, f, 1, 0
   */
  BOOL(new BooleanTypeValidator(null)),
  /**
   * INT type does the validation with Integer.parseInt(value)
   */
  INT(ParameterTypeValidator.createIntegerTypeValidator(null)),
  /**
   * FLOAT type does the validation with Float.parseFloat(value)
   */
  FLOAT(ParameterTypeValidator.createFloatTypeValidator(null)),
  /**
   * DOUBLE type does the validation with Double.parseDouble(value)
   */
  DOUBLE(ParameterTypeValidator.createDoubleTypeValidator(null)),
  /**
   * DATE as defined by full-date - RFC3339
   */
  DATE(new StringTypeValidator(RegularExpressions.DATE)),
  /**
   * DATETIME as defined by date-time - RFC3339
   */
  DATETIME(new StringTypeValidator(RegularExpressions.DATETIME)),
  /**
   * TIME as defined by partial-time - RFC3339
   */
  TIME(new StringTypeValidator(RegularExpressions.TIME)),
  /**
   * BASE64 does validation with pattern: ^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$
   */
  BASE64(new StringTypeValidator(RegularExpressions.BASE64)),

  IPV4(new StringTypeValidator(RegularExpressions.IPV4)),

  IPV6(new StringTypeValidator(RegularExpressions.IPV6)),

  HOSTNAME(new StringTypeValidator(RegularExpressions.HOSTNAME));

  private ParameterTypeValidator validationMethod;

  ParameterType(ParameterTypeValidator validationMethod) {
    this.validationMethod = validationMethod;
  }

  public ParameterTypeValidator getValidationMethod() {
    return validationMethod;
  }
}
