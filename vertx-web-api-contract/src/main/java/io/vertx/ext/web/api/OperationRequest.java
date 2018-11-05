package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.function.Function;

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
  /**
   * Contains routingContext.user().principal() if an user is authenticated
   */
  private JsonObject user;
  /**
   * Extra payload provided by {@link io.vertx.ext.web.api.contract.RouterFactoryOptions#setExtraOperationContextPayloadMapper(Function)}
   */
  private JsonObject extra;

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

  public OperationRequest(JsonObject params, MultiMap headers, JsonObject user, JsonObject extra) {
    this.params = params;
    this.headers = headers;
    this.user = user;
    this.extra = extra;
  }

  public OperationRequest(OperationRequest other) {
    this.params = other.getParams();
    this.headers = other.getHeaders();
    this.user = other.getUser();
    this.extra = other.getExtra();
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
    this.params = new JsonObject();
    this.headers = MultiMap.caseInsensitiveMultiMap();
    this.user = null;
    this.extra = null;
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

  /**
   * Get request principal user as routingContext.user().principal(), null if no user is authenticated
   */
  public JsonObject getUser() { return user; }

  /**
   * Get extra payload
   * @return
   */
  public JsonObject getExtra() {
    return extra;
  }

  @Fluent public OperationRequest setParams(JsonObject params) {
    this.params = params;
    return this;
  }

  @Fluent public OperationRequest setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  @Fluent public OperationRequest setUser(JsonObject user) {
    this.user = user;
    return this;
  }

  @Fluent
  public OperationRequest setExtra(JsonObject extra) {
    this.extra = extra;
    return this;
  }
}
