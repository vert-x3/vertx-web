package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
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
   * @return null if value is not a {@link String}, otherwise it returns value
   */
  @Nullable String getString();

  /**
   * @return true if value of this instance is a {@link String} instance
   */
  boolean isString();

  /**
   * @return null if value is not a {@link Number}, otherwise it returns value as {@link Integer}
   */
  @Nullable Integer getInteger();

  /**
   * @return null if value is not a {@link Number}, otherwise it returns value as {@link Long}
   */
  @Nullable Long getLong();

  /**
   * @return null if value is not a {@link Number}, otherwise it returns value as {@link Float}
   */
  @Nullable Float getFloat();

  /**
   * @return null if value is not a {@link Number}, otherwise it returns value as {@link Double}
   */
  @Nullable Double getDouble();

  /**
   * @return true if value of this instance is a {@link Number} instance
   */
  boolean isNumber();

  /**
   * @return null if value is not a {@link Boolean}, otherwise it returns value
   */
  @Nullable Boolean getBoolean();

  /**
   * @return true if value of this instance is a {@link Boolean} instance
   */
  boolean isBoolean();

  /**
   * Returns null if value is not a {@link JsonObject}, otherwise it returns value
   *
   * @return
   */
  @Nullable JsonObject getJsonObject();

  /**
   * @return true if value of this instance is a {@link JsonObject} instance
   */
  boolean isJsonObject();

  /**
   * @return null if value is not a {@link JsonArray}, otherwise it returns value
   */
  @Nullable JsonArray getJsonArray();

  /**
   * @return true if value of this instance is a {@link JsonArray} instance
   */
  boolean isJsonArray();

  /**
   * @return null if value is not a {@link Buffer}, otherwise it returns value
   */
  @Nullable Buffer getBuffer();

  /**
   * @return true if value of this instance is a {@link Buffer} instance
   */
  boolean isBuffer();

  /**
   * @return true if value is null
   */
  boolean isNull();

  /**
   * @return True if it's an empty string, an empty json object/array, an empty buffer or it's null
   */
  boolean isEmpty();

  /**
   * @return the internal value. The internal value is always a valid Vert.x JSON type
   */
  @CacheReturn Object get();

  static RequestParameter create(Object value) {
    return new RequestParameterImpl(value);
  }

}
