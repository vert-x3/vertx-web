package io.vertx.ext.web.handler.logger;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

abstract class BaseParameter implements Parameter {

  private int[] statusLimits;

  private boolean required;

  private char parName;

  private final String parParam;

  protected BaseParameter(String parParam) {
    this.parParam = parParam;
  }

  public void setParName(char parName) {
    this.parName = parName;
  }

  public void setStatusLimits(int[] statusLimits) {
    this.statusLimits = statusLimits;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  protected abstract String getValue(RoutingContext context, boolean immediate);

  @Override
  public final StringBuilder print(RoutingContext context, boolean immediate) {
    StringBuilder result = new StringBuilder();
    HttpServerRequest request = context.request();

    if (shouldPrint(request.response().getStatusCode())) {
      String value = getValue(context, immediate);
      result.append((value == null) ? "-" : value);
    }
    return result;
  }

  protected boolean shouldPrint(int status) {
    if (this.statusLimits == null) {
      return true;
    }

    for (int i = 0; i < this.statusLimits.length; i++) {
      if (status == this.statusLimits[i]) {
        return this.required;
      }
    }

    return !this.required;
  }

  protected char getParName() {
    return this.parName;
  }

  protected String getParParam() {
    return this.parParam;
  }

  // --------- helper ----------------------------------------------------

  private static boolean isPrint(char c) {
    return c >= 0x20 && c < 0x7f && c != '\\' && c != '"';
  }

  static String escape(String value) {
    // nothing to do for empty values
    if (value == null || value.length() == 0) {
      return value;
    }

    // find the first non-printable
    int i = 0;
    while (i < value.length() && isPrint(value.charAt(i))) {
      i++;
    }

    // if none has been found, just return the value
    if (i >= value.length()) {
      return value;
    }

    // otherwise copy the printable first part in a string buffer
    // and start encoding
    StringBuilder buf = new StringBuilder(value.substring(0, i));
    while (i < value.length()) {
      char c = value.charAt(i);
      if (isPrint(c)) {
        buf.append(c);
      } else if (c == '\n') { // LF
        buf.append("\\n");
      } else if (c == '\r') { // CR
        buf.append("\\r");
      } else if (c == '\t') { // HTAB
        buf.append("\\t");
      } else if (c == '\f') { // VTAB
        buf.append("\\f");
      } else if (c == '\b') { // BSP
        buf.append("\\b");
      } else if (c == '"') { // "
        buf.append("\\\"");
      } else if (c == '\\') { // \
        buf.append("\\\\");
      } else { // encode
        buf.append("\\u");
        if (c < 0x10) {
          buf.append('0'); // leading zero
        }
        if (c < 0x100) {
          buf.append('0'); // leading zero
        }
        if (c < 0x1000) {
          buf.append('0'); // leading zero
        }
        buf.append(Integer.toHexString(c));
      }
      i++;
    }

    // return the encoded string value
    return buf.toString();
  }

}
