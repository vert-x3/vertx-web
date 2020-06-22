package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;

import java.util.*;
import java.util.stream.Collector;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RequestParametersImpl implements RequestParameters {

  private Map<String, RequestParameter> pathParameters;
  private Map<String, RequestParameter> queryParameters;
  private Map<String, RequestParameter> headerParameters;
  private Map<String, RequestParameter> cookieParameters;
  private RequestParameter body;

  public RequestParametersImpl() {
    pathParameters = new HashMap<>();
    queryParameters = new HashMap<>();
    headerParameters = new HashMap<>();
    cookieParameters = new HashMap<>();
    body = null;
  }

  public void setPathParameters(Map<String, RequestParameter> pathParameters) {
    if (pathParameters != null) {
      this.pathParameters = pathParameters;
    }
  }

  public void setQueryParameters(Map<String, RequestParameter> queryParameters) {
    if (queryParameters != null) {
      this.queryParameters = queryParameters;
    }
  }

  public void setHeaderParameters(Map<String, RequestParameter> headerParameters) {
    if (headerParameters != null) {
      this.headerParameters = headerParameters;
    }
  }

  public void setCookieParameters(Map<String, RequestParameter> cookieParameters) {
    if (cookieParameters != null) {
      this.cookieParameters = cookieParameters;
    }
  }

  public void setBody(RequestParameter body) {
    if (body != null) {
      this.body = body;
    }
  }

  public void merge(RequestParametersImpl other) {
    if (other.pathParameters != null)
      this.pathParameters.putAll(other.pathParameters);
    if (other.queryParameters != null)
      this.queryParameters.putAll(other.queryParameters);
    if (other.headerParameters != null)
      this.headerParameters.putAll(other.headerParameters);
    if (other.cookieParameters != null)
      this.cookieParameters.putAll(other.cookieParameters);
    this.body = (other.body == null) ? this.body : other.body;
  }

  @Override
  public List<String> pathParametersNames() {
    return new ArrayList<>(pathParameters.keySet());
  }

  @Override
  public RequestParameter pathParameter(String name) {
    return pathParameters.get(name);
  }

  @Override
  public List<String> queryParametersNames() {
    return new ArrayList<>(queryParameters.keySet());
  }

  @Override
  public RequestParameter queryParameter(String name) {
    return queryParameters.get(name);
  }

  @Override
  public List<String> headerParametersNames() {
    return new ArrayList<>(headerParameters.keySet());
  }

  @Override
  public RequestParameter headerParameter(String name) {
    return headerParameters.get(name);
  }

  @Override
  public List<String> cookieParametersNames() {
    return new ArrayList<>(cookieParameters.keySet());
  }

  @Override
  public RequestParameter cookieParameter(String name) {
    return cookieParameters.get(name);
  }

  @Override
  public RequestParameter body() {
    return body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestParametersImpl that = (RequestParametersImpl) o;
    return Objects.equals(pathParameters, that.pathParameters) &&
      Objects.equals(queryParameters, that.queryParameters) &&
      Objects.equals(headerParameters, that.headerParameters) &&
      Objects.equals(cookieParameters, that.cookieParameters) &&
      Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pathParameters, queryParameters, headerParameters, cookieParameters, body);
  }

  @Override
  public JsonObject toJson() {
    JsonObject root = new JsonObject();
    root.put("path", mapToJsonObject(pathParameters));
    root.put("query", mapToJsonObject(queryParameters));
    root.put("header", mapToJsonObject(headerParameters));
    root.put("cookie", mapToJsonObject(cookieParameters));
    if (body != null)
      root.put("body", body.get());
    return root;
  }

  private JsonObject mapToJsonObject(Map<String, RequestParameter> params) {
    return params
      .entrySet()
      .stream()
      .collect(Collector.of(
        JsonObject::new,
        (j, e) -> j.put(e.getKey(), e.getValue().get()),
        JsonObject::mergeIn
      ));
  }
}
