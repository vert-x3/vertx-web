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
package io.vertx.ext.web.codec.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.spi.BodyStream;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BodyCodecImpl<T> implements BodyCodec<T> {

  public static final Function<Buffer, Void> VOID_DECODER = buff -> null;
  public static final Function<Buffer, String> UTF8_DECODER = Buffer::toString;
  public static final Function<Buffer, JsonObject> JSON_OBJECT_DECODER = buff -> new JsonObject(buff.toString());
  public static final Function<Buffer, JsonArray> JSON_ARRAY_DECODER = buff -> new JsonArray(buff.toString());

  public static final BodyCodec<String> STRING = new BodyCodecImpl<>(UTF8_DECODER);
  public static final BodyCodec<Void> NONE = new BodyCodecImpl<>(VOID_DECODER);
  public static final BodyCodec<Buffer> BUFFER = new BodyCodecImpl<>(Function.identity());
  public static final BodyCodec<JsonObject> JSON_OBJECT = new BodyCodecImpl<>(JSON_OBJECT_DECODER);
  public static final BodyCodec<JsonArray> JSON_ARRAY = new BodyCodecImpl<>(JSON_ARRAY_DECODER);

  public static BodyCodecImpl<String> string(String encoding) {
    return new BodyCodecImpl<>(buff -> buff.toString(encoding));
  }

  public static <T> BodyCodec<T> json(Class<T> type) {
    return new BodyCodecImpl<>(jsonDecoder(type));
  }

  public static <T> Function<Buffer, T> jsonDecoder(Class<T> type) {
    return buff -> Json.decodeValue(buff.toString(), type);
  }

  private final Function<Buffer, T> decoder;

  public BodyCodecImpl(Function<Buffer, T> decoder) {
    this.decoder = decoder;
  }

  @Override
  public void create(Handler<AsyncResult<BodyStream<T>>> handler) {
    handler.handle(Future.succeededFuture(new BodyStream<T>() {

      Buffer buffer = Buffer.buffer();
      Future<T> state = Future.future();

      @Override
      public void handle(Throwable cause) {
        if (!state.isComplete()) {
          state.fail(cause);
        }
      }

      @Override
      public Future<T> result() {
        return state;
      }

      @Override
      public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        return this;
      }

      @Override
      public WriteStream<Buffer> write(Buffer data) {
        buffer.appendBuffer(data);
        return this;
      }

      @Override
      public void end() {
        if (!state.isComplete()) {
          T result;
          if (buffer.length() > 0) {
            try {
              result = decoder.apply(buffer);
            } catch (Throwable t) {
              state.fail(t);
              return;
            }
          } else {
            result = null;
          }
          state.complete(result);
        }
      }

      @Override
      public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
        return this;
      }

      @Override
      public boolean writeQueueFull() {
        return false;
      }

      @Override
      public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
        return this;
      }
    }));
  }
}
