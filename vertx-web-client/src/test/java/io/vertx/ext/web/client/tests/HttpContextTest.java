package io.vertx.ext.web.client.tests;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
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

    AtomicReference<Throwable> capturedFailure = new AtomicReference<>();

    webClientInternal.addInterceptor(ctx -> {
      if (Objects.requireNonNull(ctx.phase()) == ClientPhase.FAILURE) {
        capturedFailure.set(ctx.failure());
        ctx.next();
      } else {
        ctx.next();
      }
    });

    RuntimeException err = assertThrows(RuntimeException.class, () -> webClientInternal.get("/some illegal path").send().await());
    assertNotNull(capturedFailure.get(), "The exception should have been captured in the FAILURE phase.");
    assertSame(err, capturedFailure.get());

  }

}
