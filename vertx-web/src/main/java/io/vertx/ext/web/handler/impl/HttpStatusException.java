package io.vertx.ext.web.handler.impl;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpStatusException extends Throwable {

  private final int statusCode;
  private final String payload;

  public HttpStatusException(int statusCode) {
    this(statusCode, null);
  }

  public HttpStatusException(int statusCode, String payload) {
    super(HttpResponseStatus.valueOf(statusCode).reasonPhrase(), null, false, false);
    this.statusCode = statusCode;
    this.payload = payload;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getPayload() {
    return payload;
  }
}
