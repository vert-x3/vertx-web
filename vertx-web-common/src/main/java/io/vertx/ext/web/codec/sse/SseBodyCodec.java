package io.vertx.ext.web.codec.sse;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.spi.BodyStream;
import java.nio.charset.StandardCharsets;

public class SseBodyCodec implements BodyCodec<Void> {

  private final WriteStream<SseEvent> eventHandler;

  private SseBodyCodec(WriteStream<SseEvent> eventHandler) {
    this.eventHandler = eventHandler;
  }

  private static final int MAX_BUFFER = 8000000;

  @Override
  public BodyStream<Void> stream() throws Exception {
    return new BodyStream<Void>() {
      private Buffer lineBuffer = Buffer.buffer();
      private SseEvent.Builder eventBuilder = SseEvent.builder();
      private Handler<Throwable> exceptionHandler;
      private Handler<Void> drainHandler;
      private int maxQueueSize = 8192;
      private boolean ended = false;

      @Override
      public Future<Void> result() {
        return Future.succeededFuture();
      }

      @Override
      public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
      }

      @Override
      public Future<Void> write(Buffer data) {
        if (ended) {
          return Future.failedFuture("Stream is ended");
        }

        try {
          for (byte b : data.getBytes()) {
            if (b == '\n' || b == '\r') {
              // Process the current line (could be empty)
              String line = lineBuffer.toString(StandardCharsets.UTF_8);
              lineBuffer = Buffer.buffer();

              if (line.isEmpty()) {
                // Empty line dispatches the event
                if (eventHandler != null) {
                  eventHandler.write(eventBuilder.build());
                }
                eventBuilder = SseEvent.builder();
              } else {
                parseLine(line, eventBuilder);
              }
            } else {
              if (lineBuffer.length() > MAX_BUFFER) {
                return Future.failedFuture("Data is too big");
              }
              lineBuffer.appendByte(b);
            }
          }
          return Future.succeededFuture();
        } catch (Exception e) {
          if (exceptionHandler != null) {
            exceptionHandler.handle(e);
          }
          return Future.failedFuture(e);
        }
      }

      @Override
      public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
        this.maxQueueSize = maxSize;
        return this;
      }

      @Override
      public boolean writeQueueFull() {
        return lineBuffer.length() >= maxQueueSize;
      }

      @Override
      public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
        this.drainHandler = handler;
        return this;
      }

      @Override
      public void handle(Throwable event) {
        if (exceptionHandler != null) {
          exceptionHandler.handle(event);
        }
      }

      @Override
      public Future<Void> end() {
        ended = true;
        // Process any remaining data in buffer
        if (lineBuffer.length() > 0) {
          String line = lineBuffer.toString(StandardCharsets.UTF_8);
          if (!line.isEmpty()) {
            parseLine(line, eventBuilder);
          }
        }
        // Dispatch final event if there's data
        if (eventHandler != null && (eventBuilder.toString().length() > 0)) {
          eventHandler.end(eventBuilder.build());
        }
        return Future.succeededFuture();
      }
    };
  }

  public void parseLine(String line, SseEvent.Builder builder) {
    int colonIndex = line.indexOf(':');
    if (colonIndex == 0) {
      return;
    }
    if (colonIndex == -1) {
      processField(builder, line, "");
      return;
    }
    String field = line.substring(0, colonIndex);
    String value = line.substring(colonIndex + 1);
    // Remove leading space from value if present (SSE spec)
    if (value.startsWith(" ")) {
      value = value.substring(1);
    }
    processField(builder, field, value);
  }

  public void processField(SseEvent.Builder builder, String field, String value) {
    // Field names must be compared literally, with no case folding performed.
    switch (field) {
      case "event":
        builder.event(value);
        break;
      case "data":
        builder.data(value);
        break;
      case "id":
        builder.id(value);
        break;
      case "retry":
        // If the field value consists of only ASCII digits, then interpret the field value as an
        // integer in base ten, and set the event stream's reconnection time to that integer.
        // Otherwise, ignore the field.
        try {
          builder.retry(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
          throw new RuntimeException("Invalid \"retry\" value:" + value);
        }
        break;
      default:
        // Ignore unknown fields as per SSE spec
        break;
    }
  }

  public static BodyCodec<Void> sseStream(WriteStream<SseEvent> handler) {
    return new SseBodyCodec(handler);
  }
}
