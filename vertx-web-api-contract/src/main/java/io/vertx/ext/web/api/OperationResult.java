package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

@DataObject(generateConverter = true, publicConverter = false)
public class OperationResult {

  private final static Integer DEFAULT_STATUS_CODE = 200;

  private Integer statusCode;
  private Buffer payload;

  public OperationResult() {
    init();
  }

  public OperationResult(JsonObject json) {
    init();
    OperationResultConverter.fromJson(json, this);
  }

  public OperationResult(Integer statusCode, Buffer payload) {
    this.statusCode = statusCode;
    this.payload = payload;
  }

  public OperationResult(OperationResult other) {
    this.statusCode = other.getStatusCode();
    this.payload = other.getPayload();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    OperationResultConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.statusCode = DEFAULT_STATUS_CODE;
    this.payload = null;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public Buffer getPayload() {
    return payload;
  }

  @Fluent public OperationResult setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Fluent public OperationResult setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }
}
