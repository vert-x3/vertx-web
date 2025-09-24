package io.vertx.ext.web.client.sse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.codec.spi.BodyStream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class SseBodyCodecTest {

  @Test
  public void testCreateCodec() {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);
    assertNotNull(codec);
  }

  @Test
  public void testSimpleEvent() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      assertTrue(result.succeeded());
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send simple SSE event
    String sseData = "data: Hello World\n\n";
    CountDownLatch writeLatch = new CountDownLatch(1);
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
      writeLatch.countDown();
    });

    assertTrue(writeLatch.await(5, TimeUnit.SECONDS));
    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertNull(event.id());
    assertNull(event.event());
    assertEquals("Hello World", event.data());
    assertEquals(0, event.retry());
  }

  @Test
  public void testCompleteEvent() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send complete SSE event with all fields
    String sseData = "id: 123\nevent: message\ndata: Hello World\nretry: 5000\n\n";
    CountDownLatch writeLatch = new CountDownLatch(1);
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
      writeLatch.countDown();
    });

    assertTrue(writeLatch.await(5, TimeUnit.SECONDS));
    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertEquals("123", event.id());
    assertEquals("message", event.event());
    assertEquals("Hello World", event.data());
    assertEquals(5000, event.retry());
  }

  @Test
  public void testMultilineData() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send event with multiple data lines
    String sseData = "data: Line 1\ndata: Line 2\ndata: Line 3\n\n";
    CountDownLatch writeLatch = new CountDownLatch(1);
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
      writeLatch.countDown();
    });

    assertTrue(writeLatch.await(5, TimeUnit.SECONDS));
    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertEquals("Line 1Line 2Line 3", event.data());
  }

  @Test
  public void testMultipleEvents() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send multiple events
    String sseData = "data: Event 1\n\ndata: Event 2\n\ndata: Event 3\n\n";
    CountDownLatch writeLatch = new CountDownLatch(1);
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
      writeLatch.countDown();
    });

    assertTrue(writeLatch.await(5, TimeUnit.SECONDS));
    assertEquals(3, handler.events.size());
    assertEquals("Event 1", handler.events.get(0).data());
    assertEquals("Event 2", handler.events.get(1).data());
    assertEquals("Event 3", handler.events.get(2).data());
  }

  @Test
  public void testCommentLines() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send event with comment lines (starting with :)
    String sseData = ": This is a comment\ndata: Hello World\n: Another comment\n\n";
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
    });

    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertEquals("Hello World", event.data());
  }

  @Test
  public void testFieldWithoutColon() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send field without colon (should be treated as field with empty value)
    String sseData = "data\nevent: test\n\n";
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
    });

    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertEquals("", event.data());
    assertEquals("test", event.event());
  }

  @Test
  public void testFieldWithLeadingSpace() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Test leading space removal according to SSE spec
    String sseData = "data: Hello World\nevent: message\n\n";
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
    });

    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertEquals("Hello World", event.data());
    assertEquals("message", event.event());
  }

  @Test
  public void testUnknownFields() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send event with unknown fields (should be ignored)
    String sseData = "unknown: value\ndata: Hello World\ncustom: ignored\n\n";
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
    });

    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertEquals("Hello World", event.data());
    assertNull(event.event());
    assertNull(event.id());
    assertEquals(0, event.retry());
  }

  @Test
  public void testInvalidRetryValue() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Set exception handler
    stream.exceptionHandler(errorRef::set);

    // Send event with invalid retry value
    String sseData = "data: Hello World\nretry: invalid\n\n";
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.failed());
    });

    assertNotNull(errorRef.get());
    assertTrue(errorRef.get() instanceof RuntimeException);
    assertTrue(errorRef.get().getMessage().contains("Invalid \"retry\" value"));
  }

  @Test
  public void testCarriageReturnLineFeed() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Test CRLF line endings
    String sseData = "data: Hello World\r\n\r\n";
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
    });

    assertEquals(1, handler.events.size());
    SseEvent event = handler.events.get(0);
    assertEquals("Hello World", event.data());
  }

  @Test
  public void testStreamEnd() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Send partial data without final newlines
    String sseData = "data: Hello World";
    stream.write(Buffer.buffer(sseData), writeResult -> {
      assertTrue(writeResult.succeeded());
    });

    // End the stream - should process remaining buffer
    stream.end(endResult -> {
      assertTrue(endResult.succeeded());
    });

    assertEquals(1, handler.endEvents.size());
    SseEvent event = handler.endEvents.get(0);
    assertEquals("Hello World", event.data());
  }

  @Test
  public void testWriteAfterEnd() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // End the stream first
    stream.end(endResult -> {
      assertTrue(endResult.succeeded());
    });

    // Try to write after end - should fail
    stream.write(Buffer.buffer("data: test\n\n"), writeResult -> {
      assertTrue(writeResult.failed());
      assertEquals("Stream is ended", writeResult.cause().getMessage());
    });
  }

  @Test
  public void testWriteQueueFunctions() throws Exception {
    TestEventHandler handler = new TestEventHandler();
    SseBodyCodec codec = (SseBodyCodec) SseBodyCodec.sseStream(handler);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<BodyStream<Void>> streamRef = new AtomicReference<>();

    codec.create(result -> {
      streamRef.set(result.result());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    BodyStream<Void> stream = streamRef.get();

    // Test write queue functions
    assertFalse(stream.writeQueueFull());

    stream.setWriteQueueMaxSize(100);
    assertFalse(stream.writeQueueFull());

    stream.drainHandler(v -> {
    });
  }

  private static class TestEventHandler implements WriteStream<SseEvent> {

    final List<SseEvent> events = new ArrayList<>();
    final List<SseEvent> endEvents = new ArrayList<>();
    private Handler<Throwable> exceptionHandler;

    @Override
    public WriteStream<SseEvent> exceptionHandler(Handler<Throwable> handler) {
      this.exceptionHandler = handler;
      return this;
    }

    @Override
    public Future<Void> write(SseEvent data) {
      events.add(data);
      return Future.succeededFuture();
    }

    @Override
    public void write(SseEvent data, Handler<AsyncResult<Void>> handler) {
      events.add(data);
      handler.handle(Future.succeededFuture());
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
      handler.handle(Future.succeededFuture());
    }

    @Override
    public Future<Void> end() {
      return Future.succeededFuture();
    }

    @Override
    public Future<Void> end(SseEvent data) {
      endEvents.add(data);
      return Future.succeededFuture();
    }

    @Override
    public void end(SseEvent data, Handler<AsyncResult<Void>> handler) {
      endEvents.add(data);
      handler.handle(Future.succeededFuture());
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
    public WriteStream<SseEvent> drainHandler(Handler<Void> handler) {
      return this;
    }
  }
}
