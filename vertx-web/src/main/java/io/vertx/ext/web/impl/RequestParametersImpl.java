package io.vertx.ext.web.impl;

import com.google.common.collect.Maps;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.RequestParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RequestParametersImpl implements RequestParameters {

  private Map<String, RequestParameter> pathParameters;
  private Map<String, RequestParameter> queryParameters;
  private Map<String, RequestParameter> headerParameters;
  private Map<String, RequestParameter> cookieParameters;
  private Map<String, RequestParameter> formParameters;

  public RequestParametersImpl() {
    pathParameters = Maps.newHashMap();
    queryParameters = Maps.newHashMap();
    headerParameters = Maps.newHashMap();
    cookieParameters = Maps.newHashMap();
    formParameters = Maps.newHashMap();
  }

  public void setPathParameters(Map<String, RequestParameter> pathParameters) {
    this.pathParameters = pathParameters;
  }

  public void setQueryParameters(Map<String, RequestParameter> queryParameters) {
    this.queryParameters = queryParameters;
  }

  public void setHeaderParameters(Map<String, RequestParameter> headerParameters) {
    this.headerParameters = headerParameters;
  }

  public void setCookieParameters(Map<String, RequestParameter> cookieParameters) {
    this.cookieParameters = cookieParameters;
  }

  public void setFormParameters(Map<String, RequestParameter> formParameters) {
    this.formParameters = formParameters;
  }

  @Override
  public List<String> getPathParametersNames() {
    return new ArrayList<>(pathParameters.keySet());
  }

  @Override
  public RequestParameter getPathParameter(String name) {
    return pathParameters.get(name);
  }

  @Override
  public List<String> getQueryParametersNames() {
    return new ArrayList<>(queryParameters.keySet());
  }

  @Override
  public RequestParameter getQueryParameter(String name) {
    return queryParameters.get(name);
  }

  @Override
  public List<String> getHeaderParametersNames() {
    return new ArrayList<>(headerParameters.keySet());
  }

  @Override
  public RequestParameter getHeaderParameter(String name) {
    return headerParameters.get(name);
  }

  @Override
  public List<String> getCookieParametersNames() {
    return new ArrayList<>(cookieParameters.keySet());
  }

  @Override
  public RequestParameter getCookieParameter(String name) {
    return cookieParameters.get(name);
  }

  @Override
  public List<String> getFormParametersNames() {
    return new ArrayList<>(formParameters.keySet());
  }

  @Override
  public RequestParameter getFormParameter(String name) {
    return formParameters.get(name);
  }
}
