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
package io.vertx.webclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.webclient.BodyCodec;
import io.vertx.webclient.spi.BodyStream;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BodyCodecImpl<T> implements BodyCodec<T> {

  public static final Function<Buffer, JsonObject> jsonObjectUnmarshaller = buff -> new JsonObject(buff.toString());
  public static final Function<Buffer, String> utf8Unmarshaller = Buffer::toString;

  public static Function<Buffer, String> stringUnmarshaller(String encoding) {
    return buff -> buff.toString(encoding);
  }

  public static <R> Function<Buffer, R> jsonUnmarshaller(Class<R> type) {
    return buff -> Json.decodeValue(buff.toString(), type);
  }

  private final Function<Buffer, T> unmarshaller;

  public BodyCodecImpl(Function<Buffer, T> unmarshaller) {
    this.unmarshaller = unmarshaller;
  }

  @Override
  public void stream(Handler<AsyncResult<BodyStream<T>>> handler) {
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
      public Future<T> state() {
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
          try {
            result = unmarshaller.apply(buffer);
          } catch (Throwable t) {
            state.fail(t);
            return;
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
