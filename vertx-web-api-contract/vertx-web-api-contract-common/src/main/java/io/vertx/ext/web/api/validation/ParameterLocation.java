package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.VertxGen;

/**
 * ParameterLocation describe the location of parameter inside HTTP Request
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public enum ParameterLocation {
  HEADER("header"), QUERY("query"), PATH("path"), FILE("body multipart/form"), BODY_FORM("body form"), BODY("body"),
  BODY_JSON("body json"), BODY_XML("body xml"), COOKIE("cookie");

  public String s;

  ParameterLocation(String s) {
    this.s = s;
  }

}
