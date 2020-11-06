package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.SchemaRouterOptions;

import java.util.HashMap;
import java.util.Map;

@DataObject(generateConverter = true)
public class OpenAPILoaderOptions {

  private Map<String, String> authQueryParams;
  private Map<String, String> authHeaders;

  public OpenAPILoaderOptions() {
    authHeaders = new HashMap<>();
    authQueryParams = new HashMap<>();
  }

  public OpenAPILoaderOptions(JsonObject obj) {
    OpenAPILoaderOptionsConverter.fromJson(obj, this);
  }

  public JsonObject toJson() {
    JsonObject obj = new JsonObject();
    OpenAPILoaderOptionsConverter.toJson(this, obj);
    return obj;
  }

  @Fluent
  public OpenAPILoaderOptions putAuthHeader(String headerName, String headerValue) {
    authHeaders.put(headerName, headerValue);
    return this;
  }

  @Fluent
  public OpenAPILoaderOptions putAuthQueryParam(String queryParamName, String queryParamValue) {
    authQueryParams.put(queryParamName, queryParamValue);
    return this;
  }

  public Map<String, String> getAuthQueryParams() {
    return authQueryParams;
  }

  public Map<String, String> getAuthHeaders() {
    return authHeaders;
  }

  @GenIgnore
  public SchemaRouterOptions toSchemaRouterOptions() {
    SchemaRouterOptions opt = new SchemaRouterOptions();
    authHeaders.forEach(opt::putAuthHeader);
    authQueryParams.forEach(opt::putAuthQueryParam);
    return opt;
  }

}
