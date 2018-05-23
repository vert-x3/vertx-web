package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;

import java.util.Map;

@DataObject(generateConverter = true, publicConverter = false)
public class RequestContext {

  private JsonObject params;
  private MultiMap headers;

  public RequestContext() {
    init();
  }

  public RequestContext(JsonObject json) {
    init();
    RequestContextConverter.fromJson(json, this);
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

  public RequestContext(MultiMap headers, JsonObject params) {
    this.params = params;
    this.headers = headers;
  }

  public RequestContext(RequestContext other) {
    this.params = other.getParams();
    this.headers = other.getHeaders();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RequestContextConverter.toJson(this, json);
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

  public JsonObject getParams() {
    return params;
  }

  public MultiMap getHeaders() {
    return headers;
  }

  @Fluent public RequestContext setParams(JsonObject params) {
    this.params = params;
    return this;
  }

  @Fluent public RequestContext setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }
}
