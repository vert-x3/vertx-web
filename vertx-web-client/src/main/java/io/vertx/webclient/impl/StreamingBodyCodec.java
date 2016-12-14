package io.vertx.webclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.webclient.BodyCodec;
import io.vertx.webclient.spi.BodyStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class StreamingBodyCodec implements BodyCodec<Void> {

  private final WriteStream<Buffer> stream;

  public StreamingBodyCodec(WriteStream<Buffer> stream) {
    this.stream = stream;
  }

  @Override
  public void writeStream(Handler<AsyncResult<BodyStream<Void>>> handler) {
    handler.handle(Future.succeededFuture(new BodyStream<Void>() {

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
        stream.exceptionHandler(handler);
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
    }));
  }
}
