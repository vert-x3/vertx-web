package io.vertx.ext.web.client;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpTestBase;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class HttpContextTest extends HttpTestBase {

  private WebClientInternal client;

  @Override
  protected VertxOptions getOptions() {
    return super.getOptions().setAddressResolverOptions(new AddressResolverOptions().
      setHostsValue(Buffer.buffer(
        "127.0.0.1 somehost\n" +
          "127.0.0.1 localhost")));
  }

  private void setUpClient() {
    super.client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost"));
    client = (WebClientInternal) WebClient.wrap(super.client);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    setUpClient();
    server.close();
    server = vertx.createHttpServer(new HttpServerOptions().setPort(DEFAULT_HTTP_PORT).setHost(DEFAULT_HTTP_HOST));
  }

  @Test
  public void testFailReleaseResources() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    Throwable cause = new Throwable();
    AtomicReference<HttpContext> ref =  new AtomicReference<>();
    client.addInterceptor(ctx -> {
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
    HttpRequest<Buffer> builder = client.get("/somepath");
    builder.send(onFailure(err -> {
      HttpContext ctx = ref.get();
      assertNotNull(ctx);
      assertNull(ctx.clientRequest());
      assertSame(cause, err);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSynchronousExceptionInDoSendRequestFailsTheContext() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();

    AtomicReference<Throwable> capturedFailure = new AtomicReference<>();

    client.addInterceptor(ctx -> {
      if (Objects.requireNonNull(ctx.phase()) == ClientPhase.FAILURE) {
        capturedFailure.set(ctx.failure());
        ctx.next();
      } else {
        ctx.next();
      }
    });

    RuntimeException err = Assert.assertThrows(RuntimeException.class, () -> Future.await(client.get("/some illegal path").send()));
    assertNotNull("The exception should have been captured in the FAILURE phase.", capturedFailure.get());
    assertSame(err, capturedFailure.get());

  }

  @Test
  public void testResponseFailedBeforeSendRequest() throws Exception {
    server.requestHandler(req -> req.response().end());
    startServer();
    // simulates a pooled connection closed just before the request is sent: the response future fails
    // synchronously when the context registers its handler
    vertx.exceptionHandler(this::fail);
    client.addInterceptor(ctx -> {
      if (ctx.phase() == ClientPhase.SEND_REQUEST) {
        ctx.clientRequest().reset();
      }
      ctx.next();
    });
    HttpRequest<Buffer> builder = client.post("/somepath").host("localhost").port(8080);
    builder.sendBuffer(Buffer.buffer("body"), onFailure(err -> {
      // let the current event loop task end so that an exception thrown after the failure is reported
      vertx.runOnContext(v -> testComplete());
    }));
    await();
  }

}
