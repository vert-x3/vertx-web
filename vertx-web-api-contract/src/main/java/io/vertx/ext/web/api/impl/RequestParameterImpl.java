package io.vertx.ext.web.api.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.RequestParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RequestParameterImpl implements RequestParameter {

  String name;
  Object value;

  public RequestParameterImpl(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public RequestParameter merge(RequestParameter mergingObj) {
    if (this.isArray() && mergingObj.isArray()) {
      mergingObj.getArray().addAll(this.getArray());
    } else if (this.isObject() && mergingObj.isObject()) {
      Map<String, RequestParameter> map = new HashMap<>();
      map.putAll(((Map<String, RequestParameter>) value));
      for (String key : mergingObj.getObjectKeys())
        map.put(key, mergingObj.getObjectValue(key));
      mergingObj.setValue(map);
    }
    return mergingObj;
  }

  public RequestParameterImpl(String name) {
    this(name, null);
  }

  public RequestParameterImpl() {
    this(null, null);
  }

  @Override
  public @Nullable String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public @Nullable List<String> getObjectKeys() {
    return (isObject()) ? new ArrayList<>(((Map<String, RequestParameter>) value).keySet()) : null;
  }

  @Override
  public RequestParameter getObjectValue(String key) {
    return (isObject()) ? ((Map<String, RequestParameter>) value).get(key) : null;
  }

  @Override
  public boolean isObject() {
    return !isNull() && value instanceof Map;
  }

  @Override
  public @Nullable List<RequestParameter> getArray() {
    return (isArray()) ? ((List<RequestParameter>) value) : null;
  }

  @Override
  public boolean isArray() {
    return !isNull() && value instanceof List;
  }

  @Override
  public @Nullable String getString() {
    return (isString()) ? ((String) value) : null;
  }

  @Override
  public boolean isString() {
    return !isNull() && value instanceof String;
  }

  @Override
  public @Nullable Integer getInteger() {
    return (isInteger()) ? ((Integer) value) : null;
  }

  @Override
  public boolean isInteger() {
    return !isNull() && value instanceof Integer;
  }

  @Override
  public @Nullable Long getLong() {
    return (isLong()) ? ((Long) value) : null;
  }

  @Override
  public boolean isLong() {
    return !isNull() && value instanceof Long;
  }

  @Override
  public @Nullable Float getFloat() {
    return (isFloat()) ? ((Float) value) : null;
  }

  @Override
  public boolean isFloat() {
    return !isNull() && value instanceof Float;
  }

  @Override
  public @Nullable Double getDouble() {
    return (isDouble()) ? ((Double) value) : null;
  }

  @Override
  public boolean isDouble() {
    return !isNull() && value instanceof Double;
  }

  @Override
  public @Nullable Boolean getBoolean() {
    return (isBoolean()) ? ((Boolean) value) : null;
  }

  @Override
  public boolean isBoolean() {
    return !isNull() && value instanceof Boolean;
  }

  @Override
  public @Nullable JsonObject getJsonObject() {
    return (isJsonObject()) ? ((JsonObject) value) : null;
  }

  @Override
  public boolean isJsonObject() {
    return !isNull() && value instanceof JsonObject;
  }

  @Override
  public @Nullable JsonArray getJsonArray() {
    return (isJsonArray()) ? ((JsonArray) value) : null;
  }

  @Override
  public boolean isJsonArray() {
    return !isNull() && value instanceof JsonArray;
  }

  @Override
  public boolean isNull() {
    return value == null;
  }

  @Override
  public boolean isEmpty() {
    return isNull();
  }
}
