package io.vertx.ext.web.client.tests;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class HttpContextTest extends WebClientTestBase {

  private WebClientInternal webClientInternal;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx) {
    super.setUp(vertx);
    webClientInternal = webClient;
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
}
