package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;

import java.util.regex.Pattern;

/**
 * ParameterType contains regular expressions for parameter validation. Use it to describe parameter type
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ParameterType {
  /**
   * STRING pattern:
   */
  STRING(""), //TODO add regexp
  /**
   * CASE_SENSITIVE_STRING pattern:
   */
  CASE_SENSITIVE_STRING(""),
  /**
   * NUMBER pattern:
   */
  NUMBER(""),
  /**
   * EMAIL pattern:
   */
  EMAIL(""),
  /**
   * INT pattern:
   */
  INT(""),
  /**
   * File type
   */
  FILE(""),
  /**
   * FLOAT pattern:
   */
  FLOAT("");

  public Pattern regexp;

  ParameterType(String regexp) {
    this.regexp = Pattern.compile(regexp);
  }
}
