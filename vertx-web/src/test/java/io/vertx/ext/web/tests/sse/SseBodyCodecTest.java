package io.vertx.ext.web.tests.sse;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.SseEvent;
import io.vertx.ext.web.codec.spi.BodyStream;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@VertxTest
public class SseBodyCodecTest {

  @Test
  public void testBasicEventParsing() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("event: test\ndata: hello world\nid: 1\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());

    SseEvent event = events.get(0);
    assertEquals("test", event.event());
    assertEquals("hello world", event.data());
    assertEquals("1", event.id());
    assertEquals(0, event.retry());
  }

  @Test
  public void testWriteQueueExcludesConsumedBytes() throws Exception {
    AtomicReference<BodyStream<Void>> body = new AtomicReference<>();
    List<Boolean> full = new ArrayList<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream ->
      stream.handler(event -> full.add(body.get().writeQueueFull())));
    body.set(codec.stream());

    body.get().write(Buffer.buffer("data: " + "a".repeat(4096) + "\n\ndata: next\n\n")).await();

    assertEquals(List.of(false, false), full);
  }

  @Test
  public void testMultipleEvents() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("event: first\ndata: data1\n\nevent: second\ndata: data2\n\n");
    stream.write(data).await();
    assertEquals(2, events.size());

    assertEquals("first", events.get(0).event());
    assertEquals("data1", events.get(0).data());
    assertEquals("second", events.get(1).event());
    assertEquals("data2", events.get(1).data());
  }

  @Test
  public void testRetryField() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("retry: 5000\ndata: test\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals(5000, events.get(0).retry());
  }

  @Test
  public void testInvalidRetryField(Checkpoint checkpoint) throws Exception {
    AtomicReference<Throwable> caught = new AtomicReference<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(evt -> fail("Should not receive event"));
      stream.exceptionHandler(err -> {
        caught.set(err);
        assertNotNull(caught.get());
        assertTrue(caught.get().getMessage().contains("Invalid \"retry\" value"));
        checkpoint.flag();
      });
    });

    BodyStream<Void> stream = codec.stream();
    Buffer data = Buffer.buffer("retry: invalid\ndata: test\n\n");
    stream.write(data);
  }

  @Test
  public void testCommentLines() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer(": this is a comment\ndata: actual data\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals("actual data", events.get(0).data());
  }

  @Test
  public void testFieldWithoutColon() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data\nevent: test\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals("test", events.get(0).event());
    assertEquals("", events.get(0).data());
  }

  @Test
  public void testFieldWithLeadingSpace() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data: value with space\nevent: test\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals("value with space", events.get(0).data());
  }

  @Test
  public void testMultipleDataFields() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data: line1\ndata: line2\ndata: line3\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    // Per SSE spec: multiple data fields should be concatenated with newlines
    assertEquals("line1\nline2\nline3", events.get(0).data());
  }

  @Test
  public void testUnknownFieldsIgnored() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("unknown: value\ndata: test\ncustom: ignored\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals("test", events.get(0).data());
  }

  @Test
  public void testLargeSplitEventDoesNotFillWriteQueue() throws Exception {
    List<SseEvent> events = new ArrayList<>();
    BodyStream<Void> stream = BodyCodec.sseStream(eventsStream -> eventsStream.handler(events::add)).stream();

    String halfPayload = "x".repeat(5_000);
    stream.write(Buffer.buffer("data: " + halfPayload)).await();
    assertFalse(stream.writeQueueFull());
    assertTrue(events.isEmpty());

    stream.write(Buffer.buffer(halfPayload + "\n\n")).await();
    assertEquals(1, events.size());
    assertEquals(halfPayload.repeat(2), events.get(0).data());
  }

  @Test
  public void testDrainAfterFetchingBufferedEvents() throws Exception {
    AtomicReference<ReadStream<SseEvent>> events = new AtomicReference<>();
    BodyStream<Void> body = BodyCodec.sseStream(stream -> events.set(stream.pause())).stream();
    AtomicInteger drains = new AtomicInteger();
    body.drainHandler(v -> drains.incrementAndGet());

    String event = "data: " + "x".repeat(1_500) + "\n\n";
    body.write(Buffer.buffer(event.repeat(4))).await();
    assertTrue(body.writeQueueFull());

    // Fetching one event leaves two events' worth of bytes between the watermarks.
    events.get().fetch(1);
    assertEquals(0, drains.get());
    assertTrue(body.writeQueueFull());

    events.get().fetch(2);
    assertEquals(1, drains.get());
    assertFalse(body.writeQueueFull());

    events.get().fetch(1);
    assertEquals(1, drains.get());
  }

  @Test
  public void testStreamEnded() throws Exception {
    List<SseEvent> events = new ArrayList<>();
    AtomicReference<Boolean> endCalled = new AtomicReference<>(false);

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.endHandler(v -> endCalled.set(true));
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data: test\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());

    stream.end().await();
    assertTrue(endCalled.get());
  }

  @Test
  public void testEndWithPendingData() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data: incomplete");
    stream.write(data).await();
    assertEquals(0, events.size()); // No complete event yet

    stream.end().await();
    // Incomplete data should not be dispatched
    assertEquals(0, events.size());
  }

  @Test
  public void testCarriageReturnLineSeparator() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data: test\r\r");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals("test", events.get(0).data());
  }

  @Test
  public void testWriteQueueMethods() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    stream.setWriteQueueMaxSize(100);
    assertFalse(stream.writeQueueFull());

    AtomicReference<Void> drainCalled = new AtomicReference<>();
    stream.drainHandler(v -> drainCalled.set(v));
  }

  @Test
  public void testExceptionHandler() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    AtomicReference<Throwable> exception = new AtomicReference<>();
    stream.exceptionHandler(exception::set);

    // The exceptionHandler returns null in the current implementation
    // This test just verifies it doesn't crash
  }

  @Test
  public void testResultFuture() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Future<Void> result = stream.result();
    assertTrue(result.isComplete()); // Returns succeeded future immediately
    assertTrue(result.succeeded());
  }

  @Test
  public void testPauseResume() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.pause();
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data: test1\n\ndata: test2\n\n");
    stream.write(data).await();
    // Events should not be delivered while paused
    assertEquals(0, events.size());
  }

  @Test
  public void testFetch() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.pause();
      stream.handler(events::add);
      stream.fetch(1);
    });

    BodyStream<Void> stream = codec.stream();

    Buffer data = Buffer.buffer("data: test1\n\ndata: test2\n\n");
    stream.write(data).await();
    // Only one event should be delivered
    assertEquals(1, events.size());
    assertEquals("test1", events.get(0).data());
  }

  @Test
  public void testMultipleDataFieldsWithTrailingNewline() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    // According to SSE spec, when concatenating multiple data fields:
    // 1. Append each field value with a newline
    // 2. Remove the final trailing newline before dispatching
    Buffer data = Buffer.buffer("data: first\ndata: second\ndata: third\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    // The trailing newline after "third" should be stripped
    assertEquals("first\nsecond\nthird", events.get(0).data());
  }

  @Test
  public void testSingleDataFieldNoTrailingNewline() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    // Single data field - no trailing newline to strip
    Buffer data = Buffer.buffer("data: single line\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals("single line", events.get(0).data());
  }

  @Test
  public void testMultipleDataFieldsWithEmptyLines() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    // Test with empty data fields (should still add newlines)
    Buffer data = Buffer.buffer("data: line1\ndata:\ndata: line3\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    // Empty data field still contributes a newline
    assertEquals("line1\n\nline3", events.get(0).data());
  }

  @Test
  public void testDataFieldWithActualNewlines() throws Exception {
    List<SseEvent> events = new ArrayList<>();

    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
    });

    BodyStream<Void> stream = codec.stream();

    // SSE spec allows multiline data by using multiple data: fields
    Buffer data = Buffer.buffer("data: This is\ndata: a multiline\ndata: message\n\n");
    stream.write(data).await();
    assertEquals(1, events.size());
    assertEquals("This is\na multiline\nmessage", events.get(0).data());
  }

  @Test
  public void testEventParsingAcrossResponseBuffers() throws Exception {
    List<SseEvent> events = new ArrayList<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> stream.handler(events::add));
    BodyStream<Void> stream = codec.stream();

    stream.write(Buffer.buffer("event: greeting\ndata: hello")).await();
    assertTrue(events.isEmpty());
    stream.write(Buffer.buffer(" world\nid: 1\n\n")).await();

    assertEquals(1, events.size());
    assertEquals("greeting", events.get(0).event());
    assertEquals("hello world", events.get(0).data());
    assertEquals("1", events.get(0).id());
  }

  @Test
  public void testDataLineLimitDiscardsEventAndContinues() throws Exception {
    List<SseEvent> events = new ArrayList<>();
    List<Throwable> errors = new ArrayList<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.exceptionHandler(errors::add);
    }, 2, -1);
    BodyStream<Void> stream = codec.stream();

    stream.write(Buffer.buffer("data: one\ndata: two\n")).await();
    stream.write(Buffer.buffer("data: three\n\ndata: accepted\n\n")).await();

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains("maximum number of data lines"));
    assertEquals(1, events.size());
    assertEquals("accepted", events.get(0).data());
  }

  @Test
  public void testEventSizeLimitDiscardsEventAndContinuesAcrossBuffers() throws Exception {
    List<SseEvent> events = new ArrayList<>();
    List<Throwable> errors = new ArrayList<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.exceptionHandler(errors::add);
    }, -1, 5_006);
    BodyStream<Void> stream = codec.stream();

    // The partial line is exactly 5,006 UTF-8 bytes, above the queue's high watermark.
    stream.write(Buffer.buffer("data: " + "é".repeat(2_500))).await();
    assertFalse(stream.writeQueueFull());
    assertTrue(errors.isEmpty());

    stream.write(Buffer.buffer("x")).await();
    assertEquals(1, errors.size());
    assertInstanceOf(IllegalStateException.class, errors.get(0));
    assertTrue(errors.get(0).getMessage().contains("maximum size of 5006 bytes"));

    stream.write(Buffer.buffer("\n\ndata: ok\n\n")).await();
    assertEquals(1, errors.size());
    assertEquals(1, events.size());
    assertEquals("ok", events.get(0).data());
  }

  @Test
  public void testDataLineLimitDiscardsEntireEventWhenMoreLinesFollow() throws Exception {
    List<SseEvent> events = new ArrayList<>();
    List<Throwable> errors = new ArrayList<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.handler(events::add);
      stream.exceptionHandler(errors::add);
    }, 2, -1);
    BodyStream<Void> stream = codec.stream();

    // Event 1: 4 data lines, limit is 2 — lines 3 and 4 appear before the event terminator
    stream.write(Buffer.buffer("data: one\ndata: two\ndata: three\ndata: four\n\n")).await();
    // Event 2: within limit
    stream.write(Buffer.buffer("data: accepted\n\n")).await();

    // Exactly one error for the oversized event
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains("maximum number of data lines"));
    // No spurious empty event; only the second, valid event must be delivered
    assertEquals(1, events.size());
    assertEquals("accepted", events.get(0).data());
  }

  @Test
  public void testErrorDeliveredWhilePaused() throws Exception {
    List<Throwable> errors = new ArrayList<>();
    List<SseEvent> events = new ArrayList<>();
    BodyCodec<Void> codec = BodyCodec.sseStream(stream -> {
      stream.pause();
      stream.handler(events::add);
      stream.exceptionHandler(errors::add);
    }, 1, -1);
    BodyStream<Void> stream = codec.stream();

    stream.write(Buffer.buffer("data: one\ndata: two\n\n")).await();

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains("maximum number of data lines"));
    assertEquals(0, events.size());
  }
}
