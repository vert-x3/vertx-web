package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.impl.RequestParameterImpl;

import java.util.List;

/**
 * Request parameter holder
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RequestParameter {

  /**
   * Set parameter name
   *
   * @param name
   */
  void setName(String name);

  /**
   * Set value
   *
   * @param value
   */
  void setValue(Object value);

  /**
   * Get parameter name
   *
   * @return
   */
  @Nullable String getName();

  /**
   * If value is a map of fields, it returns keys of map, otherwise it returns null
   *
   * @return
   */
  @Nullable List<String> getObjectKeys();

  /**
   * If value is a map of fields, it returns value of field with key provided, otherwise it returns null
   *
   * @param key
   * @return
   */
  @Nullable RequestParameter getObjectValue(String key);

  /**
   * Returns true if value of RequestParameter is a map of fields
   *
   * @return
   */
  boolean isObject();

  /**
   * Returns null if value is not a list, otherwise it returns value
   *
   * @return
   */
  @Nullable List<RequestParameter> getArray();

  /**
   * Returns true if value of RequestParameter is an array
   *
   * @return
   */
  boolean isArray();

  /**
   * Returns null if value is not a String, otherwise it returns value
   *
   * @return
   */
  @Nullable String getString();

  /**
   * Returns true if value of RequestParameter is a String instance
   *
   * @return
   */
  boolean isString();

  /**
   * Returns null if value is not an Integer, otherwise it returns value
   *
   * @return
   */
  @Nullable Integer getInteger();

  /**
   * Returns true if value of RequestParameter is an Integer instance
   *
   * @return
   */
  boolean isInteger();

  /**
   * Returns null if value is not a Long, otherwise it returns value
   *
   * @return
   */
  @Nullable Long getLong();

  /**
   * Returns true if value of RequestParameter is a Long instance
   *
   * @return
   */
  boolean isLong();

  /**
   * Returns null if value is not a Float, otherwise it returns value
   *
   * @return
   */
  @Nullable Float getFloat();

  /**
   * Returns true if value of RequestParameter is a Float instance
   *
   * @return
   */
  boolean isFloat();

  /**
   * Returns null if value is not a Double, otherwise it returns value
   *
   * @return
   */
  @Nullable Double getDouble();

  /**
   * Returns true if value of RequestParameter is a Double instance
   *
   * @return
   */
  boolean isDouble();

  /**
   * Returns null if value is not a Boolean, otherwise it returns value
   *
   * @return
   */
  @Nullable Boolean getBoolean();

  /**
   * Returns true if value of RequestParameter is a Boolean instance
   *
   * @return
   */
  boolean isBoolean();

  /**
   * Returns null if value is not a JsonObject, otherwise it returns value
   *
   * @return
   */
  @Nullable JsonObject getJsonObject();

  /**
   * Returns true if value of RequestParameter is a JsonObject instance
   *
   * @return
   */
  boolean isJsonObject();

  /**
   * Returns null if value is not a JsonArray, otherwise it returns value
   *
   * @return
   */
  @Nullable JsonArray getJsonArray();

  /**
   * Returns true if value of RequestParameter is a JsonArray instance
   *
   * @return
   */
  boolean isJsonArray();

  /**
   * Returns true if value is null
   *
   * @return
   */
  boolean isNull();

  /**
   * Alias of {@link RequestParameter#isNull()}
   *
   * @return
   */
  boolean isEmpty();

  /**
   * Converts deeply this RequestParameter in a Json representation
   *
   * @return
   */
  @CacheReturn Object toJson();

  /**
   * Merge this request parameter with another one. Note: the parameter passed by argument has the priority
   *
   * @param otherParameter
   * @return
   */
  RequestParameter merge(RequestParameter otherParameter);

  static RequestParameter create(String name, Object value) {
    return new RequestParameterImpl(name, value);
  }

  static RequestParameter create(Object value) {
    return new RequestParameterImpl(null, value);
  }

}
