package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;

/**
 * ParameterLocation describe the location of parameter inside HTTP Request
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ParameterLocation {
  HEADER("header"),
  QUERY("query"),
  PATH("path"),
  FILE("body multipart/form"),
  BODY_FORM("body form"),
  BODY_JSON("body json"),
  XML_JSON("body xml");

  public String s;

  ParameterLocation(String s) {
    this.s = s;
  }
}
