package io.vertx.ext.web.client.tests;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
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
    RuntimeException boom = new RuntimeException("synchronous exception in doSendRequest");
    Promise<HttpClientResponse> responsePromise = Promise.promise();
    AtomicReference<Throwable> capturedFailure = new AtomicReference<>();

    HttpClientRequest fakeRequest = mockHttpClientRequest(responsePromise, boom);

    webClientInternal.addInterceptor(ctx -> {
      switch (ctx.phase()) {
        case CREATE_REQUEST:
          ctx.sendRequest(fakeRequest);
          break;
        case FAILURE:
          capturedFailure.set(ctx.failure());
          ctx.next();
          break;
        default:
          ctx.next();
          break;
      }
    });

    RuntimeException e = assertThrows(RuntimeException.class, () -> webClientInternal.get("/somepath").send().await());
    assertSame(boom, e);
    assertSame(boom, capturedFailure.get(), "The exception should have been captured in the FAILURE phase.");

  }

  private static HttpClientRequest mockHttpClientRequest(Promise<HttpClientResponse> responsePromise, RuntimeException boom) {
    return (HttpClientRequest) Proxy.newProxyInstance(
            HttpClientRequest.class.getClassLoader(),
            new Class[]{HttpClientRequest.class},
            (proxy, method, args) -> {
              switch (method.getName()) {
                case "response":
                  return responsePromise.future();
                case "send":
                  throw boom;
                default:
                  return null;
              }
            });
  }

}
