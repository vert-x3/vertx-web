package io.vertx.ext.web.tests.sse;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.SseEvent;
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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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
    AtomicReference<Throwable> caught = new AtomicReference<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(evt -> fail("Should not receive event"));
      stream.exceptionHandler(err -> {
        caught.set(err);
        testComplete();
      });
    });

    try {
      BodyStream<Void> stream = codec.stream();
      Buffer data = Buffer.buffer("retry: invalid\ndata: test\n\n");
      stream.write(data);
    } catch (Exception e) {
      fail(e);
    }

    await();
    assertNotNull(caught.get());
    assertTrue(caught.get().getMessage().contains("Invalid \"retry\" value"));
  }

  @Test
  public void testCommentLines() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: line1\ndata: line2\ndata: line3\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        // Per SSE spec: multiple data fields should be concatenated with newlines
        assertEquals("line1\nline2\nline3", events.get(0).data());
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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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
  public void testBackpressure() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      // Write enough data to trigger backpressure (HIGH_WATERMARK = 4096)
      StringBuilder largeData = new StringBuilder();
      for (int i = 0; i < 5000; i++) {
        largeData.append('x');
      }

      Buffer data = Buffer.buffer("data: " + largeData);
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertTrue(stream.writeQueueFull());
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
    AtomicReference<Boolean> endCalled = new AtomicReference<>(false);

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> endCalled.set(true));
    });

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: test\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());

        stream.end().onComplete(endAr -> {
          assertTrue(endAr.succeeded());
          assertTrue(endCalled.get());
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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: incomplete");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(0, events.size()); // No complete event yet

        stream.end().onComplete(endAr -> {
          assertTrue(endAr.succeeded());
          // Incomplete data should not be dispatched
          assertEquals(0, events.size());
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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

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

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      AtomicReference<Throwable> exception = new AtomicReference<>();
      stream.exceptionHandler(exception::set);

      // The exceptionHandler returns null in the current implementation
      // This test just verifies it doesn't crash
      testComplete();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testResultFuture() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      Future<Void> result = stream.result();
      assertTrue(result.isComplete()); // Returns succeeded future immediately
      assertTrue(result.succeeded());
      testComplete();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testPauseResume() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.pause();
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: test1\n\ndata: test2\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        // Events should not be delivered while paused
        assertEquals(0, events.size());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testFetch() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.pause();
      stream.handler(events::add);
      stream.fetch(1);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      Buffer data = Buffer.buffer("data: test1\n\ndata: test2\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        // Only one event should be delivered
        assertEquals(1, events.size());
        assertEquals("test1", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testMultipleDataFieldsWithTrailingNewline() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      // According to SSE spec, when concatenating multiple data fields:
      // 1. Append each field value with a newline
      // 2. Remove the final trailing newline before dispatching
      Buffer data = Buffer.buffer("data: first\ndata: second\ndata: third\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        // The trailing newline after "third" should be stripped
        assertEquals("first\nsecond\nthird", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testSingleDataFieldNoTrailingNewline() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      // Single data field - no trailing newline to strip
      Buffer data = Buffer.buffer("data: single line\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("single line", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testMultipleDataFieldsWithEmptyLines() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      // Test with empty data fields (should still add newlines)
      Buffer data = Buffer.buffer("data: line1\ndata:\ndata: line3\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        // Empty data field still contributes a newline
        assertEquals("line1\n\nline3", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }

  @Test
  public void testDataFieldWithActualNewlines() {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    try {
      BodyStream<Void> stream = codec.stream();

      // SSE spec allows multiline data by using multiple data: fields
      Buffer data = Buffer.buffer("data: This is\ndata: a multiline\ndata: message\n\n");
      stream.write(data).onComplete(writeAr -> {
        assertTrue(writeAr.succeeded());
        assertEquals(1, events.size());
        assertEquals("This is\na multiline\nmessage", events.get(0).data());
        testComplete();
      });
    } catch (Exception e) {
      fail(e.getMessage());
    }

    await();
  }
}
