package io.vertx.ext.web.client.impl;

import io.vertx.ext.web.client.PathParameters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PathParametersImpl implements PathParameters {

  Map<String, List<String>> escapedParams;

  public PathParametersImpl() {
    this.escapedParams = new HashMap<>();
  }

  @Override
  public PathParameters param(String key, String value) {
    return escapedParam(key, urlEncode(value));
  }

  @Override
  public PathParameters param(String key, List<String> value) {
    return escapedParam(key, value.stream().map(PathParametersImpl::urlEncode).collect(Collectors.toList()));
  }

  @Override
  public PathParameters escapedParam(String key, String value) {
    escapedParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public PathParameters escapedParam(String key, List<String> value) {
    escapedParams.computeIfAbsent(key, k -> new ArrayList<>()).addAll(value);
    return this;
  }

  @Override
  public List<String> getEscapedParam(String key) {
    return escapedParams.get(key);
  }

  private static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }
}
