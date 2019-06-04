package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.WebResponse;

public class WebResponseImpl implements WebResponse {

  private final static Integer DEFAULT_STATUS_CODE = 200;

  private Integer statusCode;
  private String statusMessage;
  private Buffer payload;
  private ReadStream<Buffer> payloadStream;
  private MultiMap headers;

  public WebResponseImpl() {
    this.statusCode = DEFAULT_STATUS_CODE;
    this.headers = MultiMap.caseInsensitiveMultiMap();
  }

  protected Integer getStatusCode() {
    return statusCode;
  }

  protected String getStatusMessage() {
    return statusMessage;
  }

  protected boolean isStream() {
    return payloadStream != null;
  }

  protected Buffer getPayload() {
    return payload;
  }

  public ReadStream<Buffer> getPayloadStream() {
    return payloadStream;
  }

  protected MultiMap getHeaders() {
    return headers;
  }

  @Override
  @Fluent public WebResponse setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  @Override
  @Fluent public WebResponse setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Override
  @Fluent public WebResponse setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
    return this;
  }

  @Override
  @Fluent public WebResponse setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  @Override
  public WebResponse setStream(ReadStream<Buffer> bufferReadStream) {
    this.payloadStream = bufferReadStream;
    return this;
  }

  @Override
  @Fluent public WebResponse putHeader(String key, String value) {
    this.headers.add(key, value);
    return this;
  }

}
