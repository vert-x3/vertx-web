package io.vertx.ext.web.codec.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.spi.BodyStream;

import java.util.Objects;

public class JsonStreamBodyCodec implements BodyCodec<Void> {

  private final JsonParser parser;
  private final StreamingBodyCodec delegate;

  public JsonStreamBodyCodec(JsonParser parser) {
    this.parser = Objects.requireNonNull(parser, "The parser must be set");
    this.delegate = new StreamingBodyCodec(new WriteStream<Buffer>() {
      @Override
      public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        parser.exceptionHandler(handler);
        return this;
      }

      @Override
      public WriteStream<Buffer> write(Buffer buffer) {
        parser.handle(buffer);
        return this;
      }

      @Override
      public WriteStream<Buffer> write(Buffer data, Handler<AsyncResult<Void>> handler) {
        parser.handle(data);
        if (handler != null) {
          handler.handle(Future.succeededFuture());
        }
        return this;
      }

      @Override
      public void end(Handler<AsyncResult<Void>> handler) {
        parser.end();
        if (handler != null) {
          handler.handle(Future.succeededFuture());
        }
      }

      @Override
      public void end() {
        parser.end();
      }

      @Override
      public WriteStream<Buffer> setWriteQueueMaxSize(int i) {
        return this;
      }

      @Override
      public boolean writeQueueFull() {
        return false;
      }

      @Override
      public WriteStream<Buffer> drainHandler(@Nullable Handler<Void> handler) {
        return this;
      }
    });
  }

  /**
   * @return the JSON parser.
   */
  JsonParser getParser() {
    return parser;
  }


  @Override
  public void create(Handler<AsyncResult<BodyStream<Void>>> handler) {
    delegate.create(handler);
  }
}
