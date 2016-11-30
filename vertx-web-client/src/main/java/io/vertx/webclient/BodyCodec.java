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
package io.vertx.webclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.webclient.impl.BodyCodecImpl;
import io.vertx.webclient.spi.BodyStream;

import java.util.function.Function;

/**
 * A builder for configuring client-side HTTP responses.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface BodyCodec<T> {

  static BodyCodec<String> string() {
    return new BodyCodecImpl<>(Buffer::toString);
  }

  static BodyCodec<String> string(String enc) {
    return new BodyCodecImpl<>(BodyCodecImpl.stringUnmarshaller(enc));
  }

  static BodyCodec<Buffer> buffer() {
    return new BodyCodecImpl<>(Function.identity());
  }

  static BodyCodec<JsonObject> jsonObject() {
    return new BodyCodecImpl<>(BodyCodecImpl.jsonObjectUnmarshaller);
  }

  @GenIgnore
  static <U> BodyCodec<U> json(Class<U> type) {
    return new BodyCodecImpl<>(BodyCodecImpl.jsonUnmarshaller(type));
  }

  static BodyCodec<AsyncFile> tempFile() {
    throw new UnsupportedOperationException("Todo");
  }

  /**
   * A body codec that writes the body to a write stream
   *
   * @param stream the destination tream
   * @return the body codec for a write stream
   */
  static BodyCodec<Void> stream(WriteStream<Buffer> stream) {
    return new BodyCodec<Void>() {
      @Override
      public BodyStream<Void> stream() {
        return new BodyStream<Void>() {

          Future<Void> fut = Future.future();

          @Override
          public Future<Void> state() {
            return fut;
          }

          @Override
          public void handle(Throwable cause) {
            if (!fut.isComplete()) {
              fut.fail(cause);
            }
          }

          @Override
          public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
            return this;
          }

          @Override
          public WriteStream<Buffer> write(Buffer data) {
            stream.write(data);
            return this;
          }

          @Override
          public void end() {
            stream.end();
            if (!fut.isComplete()) {
              fut.complete();
            }
          }

          @Override
          public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
            return this;
          }

          @Override
          public boolean writeQueueFull() {
            return stream.writeQueueFull();
          }

          @Override
          public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
            stream.drainHandler(handler);
            return this;
          }
        };
      }
    };
  }

  @GenIgnore
  BodyStream<T> stream();
}
