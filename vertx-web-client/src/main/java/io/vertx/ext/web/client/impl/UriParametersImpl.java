package io.vertx.ext.web.client.impl;

import io.vertx.ext.web.client.UriParameters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UriParametersImpl implements UriParameters {

  Map<String, List<String>> escapedParams;

  public UriParametersImpl() {
    this.escapedParams = new HashMap<>();
  }

  @Override
  public UriParameters param(String key, String value) {
    return escapedParam(key, urlEncode(value));
  }

  @Override
  public UriParameters param(String key, List<String> value) {
    return escapedParam(key, value.stream().map(UriParametersImpl::urlEncode).collect(Collectors.toList()));
  }

  @Override
  public UriParameters escapedParam(String key, String value) {
    escapedParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    return this;
  }

  @Override
  public UriParameters escapedParam(String key, List<String> value) {
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
