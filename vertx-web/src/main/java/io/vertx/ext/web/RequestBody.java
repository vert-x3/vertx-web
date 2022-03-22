package io.vertx.ext.web;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@VertxGen
public interface RequestBody {

  /**
   * @return  the entire HTTP request body as a string, assuming UTF-8 encoding if the request does not provide the
   * content type charset attribute. If a charset is provided in the request that it shall be respected. The context
   * must have first been routed to a {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   */
  @Nullable String asString();

  /**
   * Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been
   * routed to a {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   *
   * @param encoding  the encoding, e.g. "UTF-16"
   * @return the body
   */
  @Nullable String asString(String encoding);

  /**
   * Gets the current body buffer as a {@link JsonObject}. If a positive limit is provided the parsing will only happen
   * if the buffer length is smaller or equal to the limit. Otherwise an {@link IllegalStateException} is thrown.
   *
   * When the application is only handling uploads in JSON format, it is recommended to set a limit on
   * {@link io.vertx.ext.web.handler.BodyHandler#setBodyLimit(long)} as this will avoid the upload to be parsed and
   * loaded into the application memory.
   *
   * @param maxAllowedLength if the current buffer length is greater than the limit an {@link IllegalStateException} is
   *                         thrown. This can be used to avoid DDoS attacks on very long JSON payloads that could take
   *                         over the CPU while attempting to parse the data.
   *
   * @return Get the entire HTTP request body as a {@link JsonObject}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * <br/>
   * When the body is {@code null} or the {@code "null"} JSON literal then {@code null} is returned.
   */
  @Nullable JsonObject asJsonObject(int maxAllowedLength);

  /**
   * Gets the current body buffer as a {@link JsonArray}. If a positive limit is provided the parsing will only happen
   * if the buffer length is smaller or equal to the limit. Otherwise an {@link IllegalStateException} is thrown.
   *
   * When the application is only handling uploads in JSON format, it is recommended to set a limit on
   * {@link io.vertx.ext.web.handler.BodyHandler#setBodyLimit(long)} as this will avoid the upload to be parsed and
   * loaded into the application memory.
   *
   * @param maxAllowedLength if the current buffer length is greater than the limit an {@link IllegalStateException} is
   *                         thrown. This can be used to avoid DDoS attacks on very long JSON payloads that could take
   *                         over the CPU while attempting to parse the data.
   *
   * @return Get the entire HTTP request body as a {@link JsonArray}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * <br/>
   * When the body is {@code null} or the {@code "null"} JSON literal then {@code null} is returned.
   */
  @Nullable JsonArray asJsonArray(int maxAllowedLength);

  /**
   * @return Get the entire HTTP request body as a {@link JsonObject}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * <br/>
   * When the body is {@code null} or the {@code "null"} JSON literal then {@code null} is returned.
   */
  default @Nullable JsonObject asJsonObject() {
    return asJsonObject(-1);
  }

  /**
   * @return Get the entire HTTP request body as a {@link JsonArray}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * <br/>
   * When the body is {@code null} or the {@code "null"} JSON literal then {@code null} is returned.
   */
  default @Nullable JsonArray asJsonArray() {
    return asJsonArray(-1);
  }

  /**
   * @return Get the entire HTTP request body as a {@link Buffer}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   */
  @Nullable Buffer buffer();

  /**
   * @return Get the entire HTTP request body as a POJO. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * <br/>
   * When the body is {@code null} or the {@code "null"} JSON literal then {@code null} is returned.
   *
   * <b>WARNING:</b> This feature requires jackson-databind. Or another JSON codec that implements POJO parsing
   *
   * @param maxAllowedLength if the current buffer length is greater than the limit an {@link IllegalStateException} is
   *                         thrown. This can be used to avoid DDoS attacks on very long JSON payloads that could take
   *                         over the CPU while attempting to parse the data.
   */
  <R> @Nullable R asPojo(Class<R> clazz, int maxAllowedLength);

  /**
   * @return Get the entire HTTP request body as a POJO. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   * <br/>
   * When the body is {@code null} or the {@code "null"} JSON literal then {@code null} is returned.
   *
   * <b>WARNING:</b> This feature requires jackson-databind. Or another JSON codec that implements POJO parsing
   */
  default <R> @Nullable R asPojo(Class<R> clazz) {
    return asPojo(clazz, -1);
  }

  /**
   * Returns the total length of the body buffer. This is the length in bytes. When there is no buffer the length is
   * {@code -1}.
   *
   * @return length in bytes.
   */
  int length();

  /**
   * Return {@code true} if a {@link io.vertx.ext.web.handler.BodyHandler} was executed before this call.
   * @return {@code true} if body is available.
   */
  boolean available();
}
