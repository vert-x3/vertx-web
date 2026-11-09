package io.vertx.ext.web.client.tests;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class HttpContextTest extends WebClientTestBase {

  private WebClientInternal webClientInternal;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx) {
    super.setUp(vertx);
    webClientInternal = (WebClientInternal)webClient;
  }

  @Test
  public void testFailReleaseResources() {
    server.requestHandler(req -> req.response().end());
    startServer();
    Throwable cause = new Throwable();
    AtomicReference<HttpContext> ref = new AtomicReference<>();
    webClientInternal.addInterceptor(ctx -> {
      switch (ctx.phase()) {
        case SEND_REQUEST:
          ctx.fail(cause);
          break;
        case FAILURE:
          assertNotNull(ctx.clientRequest());
          ref.set(ctx);
          ctx.next();
          break;
        default:
          ctx.next();
          break;
      }
    });
    HttpRequest<Buffer> builder = webClientInternal.get("/somepath");
    try {
      builder.send().await();
      fail("Should have failed");
    } catch (Throwable err) {
      HttpContext ctx = ref.get();
      assertNotNull(ctx);
      assertNull(ctx.clientRequest());
      assertSame(cause, err);
    }
  }

  @Test
  public void testSynchronousExceptionInDoSendRequestFailsTheContext() {
    server.requestHandler(req -> req.response().end());
    startServer();

    RuntimeException failure = new RuntimeException("boom");
    AtomicReference<Throwable> capturedFailure = new AtomicReference<>();

    webClientInternal.addInterceptor(ctx -> {
      if (ctx.phase() == ClientPhase.FAILURE) {
        capturedFailure.set(ctx.failure());
      }
      ctx.next();
    });

    // A body stream whose pipe throws synchronously when HttpContext#doSendRequest writes the body
    Pipe<Buffer> pipe = new Pipe<Buffer>() {
      @Override
      public Pipe<Buffer> endOnFailure(boolean end) {
        return this;
      }
      @Override
      public Pipe<Buffer> endOnSuccess(boolean end) {
        return this;
      }
      @Override
      public Pipe<Buffer> endOnComplete(boolean end) {
        return this;
      }
      @Override
      public Future<Void> to(WriteStream<Buffer> dst) {
        throw failure;
      }
      @Override
      public void close() {
      }
    };
    ReadStream<Buffer> stream = new ReadStream<Buffer>() {
      @Override
      public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        return this;
      }
      @Override
      public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        return this;
      }
      @Override
      public ReadStream<Buffer> pause() {
        return this;
      }
      @Override
      public ReadStream<Buffer> resume() {
        return this;
      }
      @Override
      public ReadStream<Buffer> fetch(long amount) {
        return this;
      }
      @Override
      public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        return this;
      }
      @Override
      public Pipe<Buffer> pipe() {
        return pipe;
      }
    };

    // Without the try/catch around HttpContext#doSendRequest, the exception is swallowed by the
    // event loop and the response future never completes, hence the bounded await
    RuntimeException err = assertThrows(RuntimeException.class, () -> webClientInternal.post("/somepath").sendStream(stream).await(5, TimeUnit.SECONDS));
    assertSame(failure, err);
    assertNotNull(capturedFailure.get(), "The exception should have been captured in the FAILURE phase.");
    assertSame(failure, capturedFailure.get());
  }

  @Test
  public void testPipeClosedWhenDoSendRequestThrows() {
    server.requestHandler(req -> req.response().end());
    startServer();

    AtomicBoolean closed = new AtomicBoolean();

    webClientInternal.addInterceptor(ctx -> {
      if (ctx.phase() == ClientPhase.SEND_REQUEST) {
        // Make the request complete before the body is piped, so that setChunked() throws
        ctx.clientRequest().end();
      }
      ctx.next();
    });

    ReadStream<Buffer> src = new FakeStream(closed);

    assertThrows(Exception.class, () -> webClientInternal.post("/somepath").sendStream(src).await(5, TimeUnit.SECONDS));
    assertTrue(closed.get(), "The pipe should have been closed");
  }

  @Test
  public void testPipeClosedWhenContextFailsBeforeSendingBody() {
    server.requestHandler(req -> req.response().end());
    startServer();

    AtomicBoolean closed = new AtomicBoolean();
    Throwable cause = new Throwable();

    webClientInternal.addInterceptor(ctx -> {
      if (ctx.phase() == ClientPhase.SEND_REQUEST) {
        ctx.fail(cause);
      } else {
        ctx.next();
      }
    });

    ReadStream<Buffer> src = new FakeStream(closed);

    Throwable err = assertThrows(Throwable.class, () -> webClientInternal.post("/somepath").sendStream(src).await(5, TimeUnit.SECONDS));
    assertSame(cause, err);
    assertTrue(closed.get(), "The pipe should have been closed");
  }

  private static class FakeStream implements ReadStream<Buffer> {
    final AtomicBoolean closed;
    FakeStream(AtomicBoolean closed) {
      this.closed = closed;
    }
    @Override public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) { return this; }
    @Override public ReadStream<Buffer> handler(Handler<Buffer> handler) { return this; }
    @Override public ReadStream<Buffer> pause() { return this; }
    @Override public ReadStream<Buffer> resume() { return this; }
    @Override public ReadStream<Buffer> fetch(long amount) { return this; }
    @Override public ReadStream<Buffer> endHandler(Handler<Void> endHandler) { return this; }
    @Override public Pipe<Buffer> pipe() {
      Pipe<Buffer> delegate = ReadStream.super.pipe();
      return new Pipe<Buffer>() {
        @Override public Pipe<Buffer> endOnFailure(boolean end) { delegate.endOnFailure(end); return this; }
        @Override public Pipe<Buffer> endOnSuccess(boolean end) { delegate.endOnSuccess(end); return this; }
        @Override public Pipe<Buffer> endOnComplete(boolean end) { delegate.endOnComplete(end); return this; }
        @Override public Future<Void> to(WriteStream<Buffer> dst) { return delegate.to(dst); }
        @Override public void close() { closed.set(true); delegate.close(); }
      };
    }
  }
}
