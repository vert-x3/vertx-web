package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;

/**
 * Request parameter holder
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RequestParameter {

  /**
   * Returns null if value is not a {@link String}, otherwise it returns value
   *
   * @return
   */
  @Nullable String getString();

  /**
   * Returns true if value of this instance is a {@link String} instance
   *
   * @return
   */
  boolean isString();

  /**
   * Returns null if value is not a {@link Number}, otherwise it returns value as {@link Integer}
   *
   * @return
   */
  @Nullable Integer getInteger();

  /**
   * Returns null if value is not a {@link Number}, otherwise it returns value as {@link Long}
   *
   * @return
   */
  @Nullable Long getLong();

  /**
   * Returns null if value is not a {@link Number}, otherwise it returns value as {@link Float}
   *
   * @return
   */
  @Nullable Float getFloat();

  /**
   * Returns null if value is not a {@link Number}, otherwise it returns value as {@link Double}
   *
   * @return
   */
  @Nullable Double getDouble();

  /**
   * Returns true if value of this instance is a {@link Number} instance
   *
   * @return
   */
  boolean isNumber();

  /**
   * Returns null if value is not a {@link Boolean}, otherwise it returns value
   *
   * @return
   */
  @Nullable Boolean getBoolean();

  /**
   * Returns true if value of this instance is a {@link Boolean} instance
   *
   * @return
   */
  boolean isBoolean();

  /**
   * Returns null if value is not a {@link JsonObject}, otherwise it returns value
   *
   * @return
   */
  @Nullable JsonObject getJsonObject();

  /**
   * Returns true if value of this instance is a {@link JsonObject} instance
   *
   * @return
   */
  boolean isJsonObject();

  /**
   * Returns null if value is not a {@link JsonArray}, otherwise it returns value
   *
   * @return
   */
  @Nullable JsonArray getJsonArray();

  /**
   * Returns true if value of this instance is a {@link JsonArray} instance
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
   * A parameter is empty if it's an empty string, an empty json object/array or if it's null
   *
   * @return
   */
  boolean isEmpty();

  /**
   * Converts deeply this instance into a Json representation
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

  static RequestParameter create(Object value) {
    return new RequestParameterImpl(value);
  }

}
