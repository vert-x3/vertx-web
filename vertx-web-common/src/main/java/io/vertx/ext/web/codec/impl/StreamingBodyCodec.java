/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.codec.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.spi.BodyStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class StreamingBodyCodec implements BodyCodec<Void> {

  private final WriteStream<Buffer> stream;
  private final boolean close;

  public StreamingBodyCodec(WriteStream<Buffer> stream) {
    this(stream, true);
  }

  public StreamingBodyCodec(WriteStream<Buffer> stream, boolean close) {
	this.stream = stream;
	this.close = close;
  }

  @Override
  public void create(Handler<AsyncResult<BodyStream<Void>>> handler) {
    handler.handle(Future.succeededFuture(new BodyStream<Void>() {

      Future<Void> fut = Future.future();

      @Override
      public Future<Void> result() {
        return fut;
      }

      @Override
      public void handle(Throwable cause) {
        fut.tryFail(cause);
      }

      @Override
      public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        stream.exceptionHandler(handler);
        return this;
      }

      @Override
      public WriteStream<Buffer> write(Buffer data, Handler<AsyncResult<Void>> handler) {
        stream.write(data, handler);
        return this;
      }

      @Override
      public WriteStream<Buffer> write(Buffer data) {
        return write(data, null);
      }

      @Override
      public void end() {
        end((Handler<AsyncResult<Void>>) null);
      }

      @Override
      public void end(Handler<AsyncResult<Void>> handler) {
        stream.end(ar -> {
          if (ar.succeeded()) {
            fut.tryComplete();
          } else {
            fut.tryFail(ar.cause());
          }
          if (handler != null) {
            handler.handle(ar);
          }
        });
      }

      @Override
      public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
        stream.setWriteQueueMaxSize(maxSize);
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
    }));
  }
}
