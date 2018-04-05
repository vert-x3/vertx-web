package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DataObject(generateConverter = true, publicConverter = false)
public class RequestContext {

  private JsonObject params;

  public RequestContext() {
    init();
  }

  public RequestContext(JsonObject json) {
    init();
    RequestContextConverter.fromJson(json, this);
  }

  public RequestContext(Map<String, List<String>> headers, JsonObject params) {
    this.params = params;
  }

  public RequestContext(RequestContext other) {
    this.params = other.getParams();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RequestContextConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.params = null;
  }

  public JsonObject getParams() {
    return params;
  }

  @Fluent public RequestContext setParams(JsonObject params) {
    this.params = params;
    return this;
  }
}
