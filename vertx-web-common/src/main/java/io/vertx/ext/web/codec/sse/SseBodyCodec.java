/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.vertx.ext.web.codec.sse;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.codec.BodyCodec;

@VertxGen
public interface SseBodyCodec<T> extends BodyCodec<T> {

  /**
   * A body codec that parse the response as a Server-SentEvent stream.
   *
   * @param handler the non-null hander to handle the stream of Server-Sent Events.
   * @return the body codec for a write stream
   */
  static BodyCodec<Void> sseStream(Handler<ReadStream<SseEvent>> handler) {
    return new SseBodyCodecImpl(handler);
  }
}
