package io.vertx.ext.web.codec.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.SseEvent;
import io.vertx.ext.web.codec.spi.BodyStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A codec for processing and decoding Server Sent Event from streaming HTTP body content .
 */
public class SseBodyCodec implements BodyCodec<Void> {

  private final Handler<ReadStream<SseEvent>> handler;

  public SseBodyCodec(Handler<ReadStream<SseEvent>> handler) {
    this.handler = handler;
  }

  @Override
  public void create(Handler<AsyncResult<BodyStream<Void>>> completionHandler) {
    SseBodyStream stream = new SseBodyStream();
    handler.handle(stream);
    completionHandler.handle(Future.succeededFuture(stream));
  }

  static class SseBodyStream implements BodyStream<Void>, ReadStream<SseEvent> {

    private static final int LOW_WATERMARK = 1024;
    private static final int HIGH_WATERMARK = 4 * 1024;

    private Handler<SseEvent> handler;
    private Handler<Void> endHandler;
    private final AtomicLong demand = new AtomicLong(Long.MAX_VALUE);
    private Buffer content = Buffer.buffer();
    private volatile boolean ended;
    private Handler<Void> drainHandler;
    private Handler<Throwable> errorHandler;
    private volatile boolean writeQueueFull;
    private volatile boolean failed;
    private final Object lock = new Object();
    private final Promise<Void> promise = Promise.promise();

    @Override
    public ReadStream<SseEvent> handler(@Nullable Handler<SseEvent> handler) {
      this.handler = handler;
      return this;
    }

    @Override
    public ReadStream<SseEvent> pause() {
      demand.set(0L);
      return this;
    }

    @Override
    public ReadStream<SseEvent> resume() {
      demand.set(Long.MAX_VALUE);
      check();
      return this;
    }

    @Override
    public ReadStream<SseEvent> fetch(long l) {
      if (l <= 0) {
        return this;
      }
      demand.getAndAdd(l);
      check();
      return this;
    }

    @Override
    public ReadStream<SseEvent> endHandler(@Nullable Handler<Void> handler) {
      this.endHandler = handler;
      return this;
    }

    SseEvent nextSseEvent() {
      SseEventBuilder eventBuilder = new SseEventBuilder();
      int lineStart = 0;
      byte[] bytes = content.getBytes();

      for (int i = 0; i < bytes.length; i++) {
        byte b = bytes[i];
        if (b == '\n' || b == '\r') {
          // Extract the line without the newline character
          String line = content.getString(lineStart, i, "UTF-8");

          if (line.isEmpty()) {
            // Empty line dispatches the event
            content = content.getBuffer(i + 1, content.length());
            return eventBuilder.build();
          } else {
            eventBuilder.parseLine(line);
          }

          lineStart = i + 1;
        }
      }
      return null;
    }

    void check() {
      if (failed) {
        return;
      }
      while (true) {
        if (demand.get() == 0L) {
          break;
        }
        SseEvent event;
        try {
          synchronized (lock) {
            event = nextSseEvent();
            writeQueueFull |= writeQueueFull();
          }
        } catch (Exception e) {
          failed = true;
          handle(e);
          handleEnd();
          return;
        }
        if (event == null) {
          if (ended) {
            handleEnd();
          }
          break;
        }
        demand.updateAndGet(d -> d == Long.MAX_VALUE ? d : d - 1);
        Handler<SseEvent> h = handler;
        if (h != null) {
          h.handle(event);
        }
      }
      Handler<Void> h = null;
      synchronized (lock) {
        if (content.length() < LOW_WATERMARK && writeQueueFull) {
          writeQueueFull = false;
          h = drainHandler;
        }
      }
      if (h != null) {
        h.handle(null);
      }
    }

    private void handleEnd() {
      Handler<Void> h = endHandler;
      if (h != null) {
        h.handle(null);
      }
    }

    @Override
    public void write(Buffer buffer, Handler<AsyncResult<Void>> handler) {
      synchronized (lock) {
        content.appendBuffer(buffer);
      }
      check();
      if (handler != null) {
        handler.handle(Future.succeededFuture());
      }
    }

    @Override
    public Future<Void> write(Buffer buffer) {
      Promise<Void> promise = Promise.promise();
      write(buffer, promise);
      return promise.future();
    }

    @Override
    public boolean writeQueueFull() {
      return content.length() >= HIGH_WATERMARK;
    }

    @Override
    public WriteStream<Buffer> drainHandler(@Nullable Handler<Void> handler) {
      drainHandler = handler;
      return this;
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
      ended = true;
      check();
      promise.tryComplete();
      if (handler != null) {
        handler.handle(Future.succeededFuture());
      }
    }

    @Override
    public Future<Void> result() {
      return promise.future();
    }

    @Override
    public SseBodyStream exceptionHandler(@Nullable Handler<Throwable> handler) {
      this.errorHandler = handler;
      return this;
    }

    @Override
    public void handle(Throwable throwable) {
      Handler<Throwable> h = errorHandler;
      if (h != null) {
        h.handle(throwable);
      }
    }

    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(int i) {
      return this;
    }
  }

  private static class SseEventBuilder {

    private String id;
    private String event = "message";
    private StringBuilder data = new StringBuilder();
    private int retry;

    SseEventBuilder id(String id) {
      this.id = id;
      return this;
    }

    SseEventBuilder event(String event) {
      this.event = event;
      return this;
    }

    SseEventBuilder data(String data) {
      if (this.data.length() > 0) {
        this.data.append('\n');
      }
      this.data.append(data);
      return this;
    }

    SseEventBuilder retry(int retry) {
      this.retry = retry;
      return this;
    }

    void parseLine(String line) {
      int colonIndex = line.indexOf(':');
      if (colonIndex == 0) {
        return;
      }
      if (colonIndex == -1) {
        processField(line, "");
        return;
      }
      String field = line.substring(0, colonIndex);
      String value = line.substring(colonIndex + 1);
      // Remove leading space from value if present (SSE spec)
      if (value.startsWith(" ")) {
        value = value.substring(1);
      }
      processField(field, value);
    }

    private void processField(String field, String value) {
      // Field names must be compared literally, with no case folding performed.
      switch (field) {
        case "event":
          event(value);
          break;
        case "data":
          data(value);
          break;
        case "id":
          id(value);
          break;
        case "retry":
          // If the field value consists of only ASCII digits, then interpret the field value as an
          // integer in base ten, and set the event stream's reconnection time to that integer.
          // Otherwise, ignore the field.
          try {
            retry(Integer.parseInt(value));
          } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid \"retry\" value:" + value, ex);
          }
          break;
        default:
          // Ignore unknown fields as per SSE spec
          break;
      }
    }

    public SseEvent build() {
      String dataStr = this.data.toString();
      // Remove trailing LF if present (SSE spec requirement)
      if (dataStr.endsWith("\n")) {
        dataStr = dataStr.substring(0, dataStr.length() - 1);
      }
      return new SseEvent(this.id, this.event, dataStr, this.retry);
    }
  }
}
