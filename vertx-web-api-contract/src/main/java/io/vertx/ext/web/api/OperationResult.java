package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;

@DataObject(generateConverter = true, publicConverter = false)
public class OperationResult {

  private final static Integer DEFAULT_STATUS_CODE = 200;

  private Integer statusCode;
  private Buffer payload;
  private MultiMap headers;

  public OperationResult() {
    init();
  }

  public OperationResult(JsonObject json) {
    init();
    OperationResultConverter.fromJson(json, this);
    JsonObject hdrs = json.getJsonObject("headers", null);
    if (hdrs != null) {
      headers = new CaseInsensitiveHeaders();
      for (Map.Entry<String, Object> entry: hdrs) {
        if (!(entry.getValue() instanceof String)) {
          throw new IllegalStateException("Invalid type for message header value " + entry.getValue().getClass());
        }
        headers.set(entry.getKey(), (String)entry.getValue());
      }
    }
  }

  public OperationResult(Integer statusCode, Buffer payload, MultiMap headers) {
    this.statusCode = statusCode;
    this.payload = payload;
    this.headers = headers;
  }

  public OperationResult(OperationResult other) {
    this.statusCode = other.getStatusCode();
    this.payload = other.getPayload();
    this.headers = other.getHeaders();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    OperationResultConverter.toJson(this, json);
    if (headers != null) {
      JsonObject hJson = new JsonObject();
      headers.entries().forEach(entry -> hJson.put(entry.getKey(), entry.getValue()));
      json.put("headers", hJson);
    }
    return json;
  }

  private void init() {
    this.statusCode = DEFAULT_STATUS_CODE;
    this.payload = null;
    this.headers = MultiMap.caseInsensitiveMultiMap();
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public Buffer getPayload() {
    return payload;
  }

  public MultiMap getHeaders() {
    return headers;
  }

  @Fluent public OperationResult setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  @Fluent public OperationResult setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Fluent public OperationResult setPayload(Buffer payload) {
    this.payload = payload;
    return this;
  }

  @Fluent public OperationResult putHeader(String key, String value) {
    this.headers.add(key, value);
    return this;
  }

  public static OperationResult completedWithJsonPayload(JsonObject jsonObject) {
    OperationResult op = new OperationResult();
    op.setStatusCode(200);
    op.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json");
    op.setPayload(jsonObject.toBuffer());
    return op;
  }

  public static OperationResult completedWithJsonPayload(JsonArray jsonArray) {
    OperationResult op = new OperationResult();
    op.setStatusCode(200);
    op.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json");
    op.setPayload(jsonArray.toBuffer());
    return op;
  }
}
