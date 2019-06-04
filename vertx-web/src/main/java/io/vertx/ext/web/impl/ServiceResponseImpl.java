package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.ServiceResponse;

public class ServiceResponseImpl implements ServiceResponse {

  private final static Integer DEFAULT_STATUS_CODE = 200;

  private Integer statusCode;
  private String statusMessage;
  private Object payload;
  private MultiMap headers;

  public ServiceResponseImpl() {
    init();
  }

  public ServiceResponseImpl(Integer statusCode, String statusMessage, Buffer payload, MultiMap headers) {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.payload = payload;
    this.headers = headers;
  }

  public ServiceResponseImpl(ServiceResponse other) {
    this.statusCode = other.getStatusCode();
    this.statusMessage = other.getStatusMessage();
    this.payload = other.getPayload();
    this.headers = other.getHeaders();
  }

  private void init() {
    this.statusCode = DEFAULT_STATUS_CODE;
    this.payload = null;
    this.headers = MultiMap.caseInsensitiveMultiMap();
  }

  @Override
  public Integer getStatusCode() {
    return statusCode;
  }

  @Override
  public String getStatusMessage() {
    return statusMessage;
  }

  @Override
  public Buffer getPayload() {
    return payload;
  }

  @Override
  public MultiMap getHeaders() {
    return headers;
  }

  @Override
  @Fluent public ServiceResponse setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  @Override
  @Fluent public ServiceResponse setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Override
  @Fluent public ServiceResponse setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
    return this;
  }

  @Override
  @Fluent public ServiceResponse setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  @Override
  @Fluent public ServiceResponse putHeader(String key, String value) {
    this.headers.add(key, value);
    return this;
  }

}
