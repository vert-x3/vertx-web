package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.impl.BooleanTypeValidator;
import io.vertx.ext.web.api.validation.impl.RegularExpressions;
import io.vertx.ext.web.api.validation.impl.StringTypeValidator;

/**
 * ParameterType contains prebuilt type validators. To access to ParameterTypeValidator of every ParameterType, use
 * {@link ParameterType#validationMethod()}
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ParameterType {
  /**
   * STRING Type accept every string
   */
  GENERIC_STRING(RequestParameter::create), EMAIL(new StringTypeValidator(RegularExpressions.EMAIL)),
  URI(new StringTypeValidator(RegularExpressions.URI)), /**
   * It allows true, false, t, f, 1, 0
   */
  BOOL(new BooleanTypeValidator(null)), /**
   * INT type does the validation with Integer.parseInt(value)
   */
  INT(ParameterTypeValidator.createIntegerTypeValidator(null)), /**
   * FLOAT type does the validation with Float.parseFloat(value)
   */
  FLOAT(ParameterTypeValidator.createFloatTypeValidator(null)), /**
   * DOUBLE type does the validation with Double.parseDouble(value)
   */
  DOUBLE(ParameterTypeValidator.createDoubleTypeValidator(null)), /**
   * DATE as defined by full-date - RFC3339
   */
  DATE(new StringTypeValidator(RegularExpressions.DATE)), /**
   * DATETIME as defined by date-time - RFC3339
   */
  DATETIME(new StringTypeValidator(RegularExpressions.DATETIME)), /**
   * TIME as defined by partial-time - RFC3339
   */
  TIME(new StringTypeValidator(RegularExpressions.TIME)), BASE64(new StringTypeValidator(RegularExpressions.BASE64)),
  IPV4(new StringTypeValidator(RegularExpressions.IPV4)), IPV6(new StringTypeValidator(RegularExpressions.IPV6)),
  HOSTNAME(new StringTypeValidator(RegularExpressions.HOSTNAME)),

  /**
   * UUID as defined by RFC4122
   */
  UUID(new StringTypeValidator(RegularExpressions.UUID));

  private ParameterTypeValidator validationMethod;

  ParameterType(ParameterTypeValidator validationMethod) {
    this.validationMethod = validationMethod;
  }

  public ParameterTypeValidator validationMethod() {
    return validationMethod;
  }
}
