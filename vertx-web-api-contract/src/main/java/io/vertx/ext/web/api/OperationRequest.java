package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;

import java.util.Map;

@DataObject(generateConverter = true, publicConverter = false)
public class OperationRequest {

  /**
   * Request parsedParameters as JSON
   */
  private JsonObject params;
  /**
   * Request headers
   */
  private MultiMap headers;

  public OperationRequest() {
    init();
  }

  public OperationRequest(JsonObject json) {
    init();
    OperationRequestConverter.fromJson(json, this);
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

  public OperationRequest(MultiMap headers, JsonObject params) {
    this.params = params;
    this.headers = headers;
  }

  public OperationRequest(OperationRequest other) {
    this.params = other.getParams();
    this.headers = other.getHeaders();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    OperationRequestConverter.toJson(this, json);
    if (headers != null) {
      JsonObject hJson = new JsonObject();
      headers.entries().forEach(entry -> hJson.put(entry.getKey(), entry.getValue()));
      json.put("headers", hJson);
    }
    return json;
  }

  private void init() {
    this.params = null;
    this.headers = MultiMap.caseInsensitiveMultiMap();
  }

  /**
   * Get request parsedParameters as JSON
   */
  public JsonObject getParams() {
    return params;
  }

  /**
   * Get request headers
   */
  public MultiMap getHeaders() {
    return headers;
  }

  @Fluent public OperationRequest setParams(JsonObject params) {
    this.params = params;
    return this;
  }

  @Fluent public OperationRequest setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }
}
