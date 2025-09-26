package io.vertx.ext.web.codec.tests.sse;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.sse.SseBodyCodec;
import io.vertx.ext.web.codec.sse.SseEvent;
import io.vertx.ext.web.codec.spi.BodyStream;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SseBodyCodecTest extends VertxTestBase {

  @Test
  public void testBasicEventParsing() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("event: test\ndata: hello world\nid: 1\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());

        SseEvent event = events.get(0);
        assertEquals("test", event.event());
        assertEquals("hello world", event.data());
        assertEquals("1", event.id());
        assertEquals(0, event.retry());

        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testMultipleEvents() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("event: first\ndata: data1\n\nevent: second\ndata: data2\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(2, events.size());

        assertEquals("first", events.get(0).event());
        assertEquals("data1", events.get(0).data());
        assertEquals("second", events.get(1).event());
        assertEquals("data2", events.get(1).data());

        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testRetryField() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("retry: 5000\ndata: test\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals(5000, events.get(0).retry());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testInvalidRetryField() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("retry: invalid\ndata: test\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.failed());
        assertTrue(writeAr.cause().getMessage().contains("Invalid \"retry\" value"));
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testCommentLines() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer(": this is a comment\ndata: actual data\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("actual data", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testFieldWithoutColon() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data\nevent: test\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("test", events.get(0).event());
        assertEquals("", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testFieldWithLeadingSpace() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: value with space\nevent: test\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("value with space", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testMultipleDataFields() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: line1\ndata: line2\ndata: line3\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("line1line2line3", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testUnknownFieldsIgnored() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("unknown: value\ndata: test\ncustom: ignored\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("test", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testBufferSizeLimit() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      // Create data that exceeds MAX_BUFFER (8000000)
      StringBuilder largeData = new StringBuilder();
      for (int i = 0; i < 8000001; i++) {
        largeData.append('x');
      }

      Buffer data = Buffer.buffer("data: " + largeData);
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.failed());
        assertTrue(writeAr.cause().getMessage().contains("Data is too big"));
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testStreamEnded() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      stream.end().onComplete(endAr -> {
        assertTrue(endAr.succeeded());

        Buffer data = Buffer.buffer("data: test\n\n");
        stream.write(data).onComplete(writeAr -> {
          assertTrue(writeAr.failed());
          assertTrue(writeAr.cause().getMessage().contains("Stream is ended"));
          testComplete();
        });
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testEndWithPendingData() {
    List<SseEvent> events = new ArrayList<>();
    AtomicReference<SseEvent> endEvent = new AtomicReference<>();

    WriteStream<SseEvent> handler = new WriteStream<SseEvent>() {
      @Override
      public WriteStream<SseEvent> exceptionHandler(io.vertx.core.Handler<Throwable> handler) {
        return this;
      }

      @Override
      public Future<Void> write(SseEvent data) {
        events.add(data);
        return Future.succeededFuture();
      }

      @Override
      public Future<Void> end() {
        return Future.succeededFuture();
      }

      @Override
      public Future<Void> end(SseEvent data) {
        endEvent.set(data);
        return Future.succeededFuture();
      }

      @Override
      public WriteStream<SseEvent> setWriteQueueMaxSize(int maxSize) {
        return this;
      }

      @Override
      public boolean writeQueueFull() {
        return false;
      }

      @Override
      public WriteStream<SseEvent> drainHandler(io.vertx.core.Handler<Void> handler) {
        return this;
      }
    };

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: incomplete");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());

        stream.end().onComplete(endAr -> {
          assertTrue(endAr.succeeded());
          assertNotNull(endEvent.get());
          assertEquals("incomplete", endEvent.get().data());
          testComplete();
        });
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testCarriageReturnLineSeparator() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: test\r\r");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("test", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testWriteQueueMethods() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      stream.setWriteQueueMaxSize(100);
      assertFalse(stream.writeQueueFull());

      AtomicReference<Void> drainCalled = new AtomicReference<>();
      stream.drainHandler(v -> drainCalled.set(v));

      testComplete();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testExceptionHandler() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      AtomicReference<Throwable> exception = new AtomicReference<>();
      stream.exceptionHandler(exception::set);

      // Simulate exception by calling handle directly
      RuntimeException testException = new RuntimeException("Test exception");
      stream.handle(testException);

      assertEquals(testException, exception.get());
      testComplete();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testResultFuture() {
    List<SseEvent> events = new ArrayList<>();
    WriteStream<SseEvent> handler = createEventHandler(events);

    BodyCodec<Void> codec = SseBodyCodec.sseStream(handler);

    try {
      BodyStream<Void> stream = codec.stream();

      Future<Void> result = stream.result();
      assertTrue(result.succeeded());
      assertNull(result.result());
      testComplete();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  private WriteStream<SseEvent> createEventHandler(List<SseEvent> events) {
    return new WriteStream<SseEvent>() {
      @Override
      public WriteStream<SseEvent> exceptionHandler(io.vertx.core.Handler<Throwable> handler) {
        return this;
      }

      @Override
      public Future<Void> write(SseEvent data) {
        events.add(data);
        return Future.succeededFuture();
      }

      @Override
      public Future<Void> end() {
        return Future.succeededFuture();
      }

      @Override
      public Future<Void> end(SseEvent data) {
        if (data != null) {
          events.add(data);
        }
        return Future.succeededFuture();
      }

      @Override
      public WriteStream<SseEvent> setWriteQueueMaxSize(int maxSize) {
        return this;
      }

      @Override
      public boolean writeQueueFull() {
        return false;
      }

      @Override
      public WriteStream<SseEvent> drainHandler(io.vertx.core.Handler<Void> handler) {
        return this;
      }
    };
  }
}
