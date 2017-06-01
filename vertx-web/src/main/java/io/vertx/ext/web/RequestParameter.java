package io.vertx.ext.web;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.impl.RequestParameterImpl;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RequestParameter {

  void setName(String name);

  void setValue(Object value);

  @Nullable
  String getName();

  @Nullable
  List<String> getObjectKeys();

  RequestParameter getObjectValue(String key);

  boolean isObject();

  @Nullable
  List<RequestParameter> getArray();

  boolean isArray();

  @Nullable
  String getString();

  boolean isString();

  @Nullable
  Integer getInteger();

  boolean isInteger();

  @Nullable
  Long getLong();

  boolean isLong();

  @Nullable
  Float getFloat();

  boolean isFloat();

  @Nullable
  Double getDouble();

  boolean isDouble();

  @Nullable
  Boolean getBoolean();

  @Nullable
  JsonObject getJsonObject();

  boolean isJsonObject();

  @Nullable
  JsonArray getJsonArray();

  boolean isJsonArray();

  boolean isBoolean();

  boolean isNull();

  static RequestParameter create(String name, Object value) {
    return new RequestParameterImpl(name, value);
  }

  static RequestParameter create(Object value) {
    return new RequestParameterImpl(null, value);
  }

}
