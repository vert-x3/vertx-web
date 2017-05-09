package io.vertx.ext.web.validation;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
public interface ValidationHandler extends Handler<RoutingContext> {

  enum ParameterType {
    STRING(""), //TODO add regexp
    CASE_SENSITIVE_STRING(""),
    NUMBER(""),
    EMAIL(""),
    INT(""),
    FLOAT("");

    public String regexp;

    ParameterType(String regexp) {
      this.regexp = regexp;
    }
  }

  enum ParameterLocation {
    HEADER("header"),
    QUERY("query"),
    PATH("path"),
    BODY_FORM("body form"),
    BODY_JSON("body json"),
    XML_JSON("body xml");

    public String s;

    ParameterLocation(String s) {
      this.s = s;
    }
  }
}
