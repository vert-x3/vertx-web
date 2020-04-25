package io.vertx.ext.web.validation.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestParameter;

import java.util.Objects;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class RequestParameterImpl implements RequestParameter {

  private Object value;

  public RequestParameterImpl(Object value) {
    this.value = value;
  }

  @Override
  public RequestParameter merge(RequestParameter m) {
    RequestParameterImpl mergingObj = (RequestParameterImpl) m;
    if (this.isJsonArray() && mergingObj.isJsonArray()) {
      mergingObj.value = mergingObj.getJsonArray().addAll(this.getJsonArray());
    } else if (this.isJsonObject() && mergingObj.isJsonObject()) {
      mergingObj.value = mergingObj.getJsonObject().mergeIn(this.getJsonObject());
    }
    return mergingObj;
  }

  public RequestParameterImpl() {
    this(null);
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
    return (isNumber()) ? ((Number) value).intValue() : null;
  }

  @Override
  public @Nullable Long getLong() {
    return (isNumber()) ? ((Number) value).longValue() : null;
  }

  @Override
  public @Nullable Float getFloat() {
    return (isNumber()) ? ((Number) value).floatValue() : null;
  }

  @Override
  public @Nullable Double getDouble() {
    return (isNumber()) ? ((Number) value).doubleValue() : null;
  }

  @Override
  public boolean isNumber() {
    return !isNull() && value instanceof Number;
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
    return isNull() ||
      (isString() && getString().isEmpty()) ||
      (isJsonObject() && getJsonObject().isEmpty()) ||
      (isJsonArray() && getJsonArray().isEmpty());
  }

  @Override
  public Object toJson() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestParameterImpl that = (RequestParameterImpl) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
