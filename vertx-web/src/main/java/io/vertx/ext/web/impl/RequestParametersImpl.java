package io.vertx.ext.web.impl;

import io.vertx.ext.web.RequestParameters;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RequestParametersImpl implements RequestParameters {
  Map<String, String> pathParameters;
  Map<String, String> undecodedPathParameters;
  Map<String, String> queryParameters;
  Map<String, String> undecodedQueryParameters;

  RequestParametersImpl() {
    pathParameters = new HashMap<>();
    undecodedPathParameters = new HashMap<>();
    queryParameters = new HashMap<>();
    undecodedQueryParameters = new HashMap<>();
  }

  @Override
  public Map<String, String> getPathParameters() {
    return pathParameters;
  }

  @Override
  public Map<String, String> getUndecodedPathParameters() {
    return undecodedPathParameters;
  }

  @Override
  public Map<String, String> getQueryParameters() {
    return queryParameters;
  }

  @Override
  public Map<String, String> getUndecodedQueryParameters() {
    return undecodedQueryParameters;
  }
}
