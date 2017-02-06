/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.codec;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;
import io.vertx.ext.web.codec.impl.StreamingBodyCodec;
import io.vertx.ext.web.codec.spi.BodyStream;

import java.util.function.Function;

/**
 * A codec for encoding and decoding HTTP bodies.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface BodyCodec<T> {

  /**
   * @return the UTF-8 string codec
   */
  static BodyCodec<String> string() {
    return BodyCodecImpl.STRING;
  }

  /**
   * A codec for strings using a specific {@code encoding}.
   *
   * @param encoding the encoding
   * @return the codec
   */
  static BodyCodec<String> string(String encoding) {
    return BodyCodecImpl.string(encoding);
  }

  /**
   * @return the {@link Buffer} codec
   */
  static BodyCodec<Buffer> buffer() {
    return BodyCodecImpl.BUFFER;
  }

  /**
   * @return the {@link JsonObject} codec
   */
  static BodyCodec<JsonObject> jsonObject() {
    return BodyCodecImpl.JSON_OBJECT;
  }

  /**
   * @return the {@link JsonArray} codec
   */
  static BodyCodec<JsonArray> jsonArray() {
    return BodyCodecImpl.JSON_ARRAY;
  }

  /**
   * Create and return a codec for Java objects encoded using Jackson mapper.
   *
   * @return a codec for mapping POJO to Json
   */
  static <U> BodyCodec<U> json(Class<U> type) {
    return BodyCodecImpl.json(type);
  }

  /**
   * @return a codec that simply discards the response
   */
  static BodyCodec<Void> none() {
    return BodyCodecImpl.NONE;
  }

  /**
   * Create a codec that buffers the entire body and then apply the {@code decode} function and returns the result.
   *
   * @param decode the decode function
   * @return the created codec
   */
  static <T> BodyCodec<T> create(Function<Buffer, T> decode) {
    return new BodyCodecImpl<>(decode);
  }

  /**
   * A body codec that pipes the body to a write stream.
   *
   * @param stream the destination tream
   * @return the body codec for a write stream
   */
  static BodyCodec<Void> pipe(WriteStream<Buffer> stream) {
    return new StreamingBodyCodec(stream);
  }

  /**
   * Create the {@link BodyStream}.
   * <p>
   * This method is usually called for creating the pump for the HTTP response and should not be called directly.
   */
  @GenIgnore
  void create(Handler<AsyncResult<BodyStream<T>>> handler);

}
