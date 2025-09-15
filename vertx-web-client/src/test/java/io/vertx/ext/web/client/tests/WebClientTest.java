package io.vertx.ext.web.client.tests;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.AsyncFileLock;
import io.vertx.core.http.*;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.http.HttpClientInternal;
import io.vertx.core.internal.net.NetClientInternal;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.htdigest.HtdigestCredentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.tests.jackson.WineAndCheese;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.test.core.Repeat;
import io.vertx.test.core.TestUtils;
import io.vertx.test.fakeresolver.FakeAddress;
import io.vertx.test.fakeresolver.FakeEndpointResolver;
import io.vertx.test.tls.Cert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientTest extends WebClientTestBase {

  @Test
  public void testDefaultHostAndPort() throws Exception {
    testRequest(client -> client.get("somepath"), req -> assertEquals("localhost:8080", req.authority().toString()));
  }

  @Test
  public void testDefaultPort() throws Exception {
    testRequest(client -> client.get("somehost", "somepath"), req -> assertEquals("somehost:8080", req.authority().toString()));
  }

  @Test
  public void testDefaultUserAgent() throws Exception {
    testRequest(client -> client.get("somehost", "somepath"), req -> {
      String ua = req.headers().get(HttpHeaders.USER_AGENT);
      assertTrue("Was expecting use agent header " + ua + " to start with Vert.x-WebClient/", ua.startsWith("Vert.x-WebClient/"));
    });
  }

  @Test
  public void testBasicAuthentication() throws Exception {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials("ém$¨=!$€", "&@#§$*éà#\"'");
    testRequest(
      client -> client.get("somehost", "somepath").authentication(creds),
      req -> {
        String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
        assertEquals("Was expecting authorization header to contain a basic authentication string", creds.toHttpAuthorization(), auth);
      }
    );
  }

  @Test
  public void testBasicAuthenticationEmptyUsername() throws Exception {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials("", "&@#§$*éà#\"'");
    testRequest(
      client -> client.get("somehost", "somepath").authentication(creds),
      req -> {
        String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
        assertEquals("Was expecting authorization header to contain a basic authentication string", creds.toHttpAuthorization(), auth);
      }
    );
  }

  @Test
  public void testBasicAuthenticationNullUsername() throws Exception {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(null, "&@#§$*éà#\"'");
    testRequest(
      client -> client.get("somehost", "somepath").authentication(creds),
      req -> {
        String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
        assertEquals("Was expecting authorization header to contain a basic authentication string", creds.toHttpAuthorization(), auth);
      }
    );
  }

  @Test
  public void testBasicAuthenticationNullUsername2() throws Exception {
    testRequest(
      client -> client.get("somehost", "somepath").basicAuthentication("", "password"),
      req -> {
        String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
        assertEquals("Was expecting authorization header to contain a basic authentication string", "Basic OnBhc3N3b3Jk", auth);
      }
    );
  }

  @Test
  public void testBearerTokenAuthentication() throws Exception {
    testRequest(
      client -> client.get("somehost", "somepath").authentication(new TokenCredentials("sometoken")),
      req -> {
        String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
        assertEquals("Was expecting authorization header to contain a bearer token authentication string", "Bearer sometoken", auth);
      }
    );
  }

  @Test
  public void testDigestAuthentication() throws Exception {
    testRequest(
      client -> client.get("somehost", "/dir/index.html")
        .authentication(
          // like on wikipedia
          new HtdigestCredentials("Mufasa", "Circle Of Life")
          .applyHttpChallenge(
            "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"",
            HttpMethod.GET,
            "/dir/index.html",
            1,
            "0a4f113b"
          )
        ),
      req -> {
        String auth = req.headers().get(HttpHeaders.AUTHORIZATION);
        String expected = "Digest username=\"Mufasa\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/dir/index.html\", qop=auth, nc=1, cnonce=\"0a4f113b\", response=\"95c727b8ed724ea2be8e9318e0e4f619\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

        assertEquals("Was expecting authorization header to contain a digest authentication string", expected, auth);
      }
    );
  }

  @Test
  public void testCustomUserAgent() throws Exception {
    webClient = WebClient.wrap(client, new WebClientOptions().setUserAgent("smith"));
    testRequest(client -> client.get("somehost", "somepath"), req -> assertEquals(Collections.singletonList("smith"), req.headers().getAll(HttpHeaders.USER_AGENT)));
  }

  @Test
  public void testUserAgentDisabled() throws Exception {
    webClient = WebClient.wrap(client, new WebClientOptions().setUserAgentEnabled(false));
    testRequest(client -> client.get("somehost", "somepath"), req -> assertEquals(Collections.emptyList(), req.headers().getAll(HttpHeaders.USER_AGENT)));
  }

  @Test
  public void testSendingJsonWithUserAgentDisabled() throws Exception {
    waitFor(2);
    server.requestHandler(req -> {
      assertEquals(Collections.emptyList(), req.headers().getAll(HttpHeaders.USER_AGENT));
      try {
        complete();
      } finally {
        req.response().end();
      }
    });
    startServer();

    JsonObject payload = new JsonObject().put("meaning", 42);

    WebClientOptions clientOptions = new WebClientOptions()
      .setDefaultHost(DEFAULT_HTTP_HOST)
      .setDefaultPort(DEFAULT_HTTP_PORT)
      .setUserAgentEnabled(false);

    WebClient agentFreeClient = WebClient.create(vertx, clientOptions);
    HttpRequest<Buffer> builder = agentFreeClient.post("somehost", "somepath");
    builder.sendJson(payload).onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testUserAgentHeaderOverride() throws Exception {
    testRequest(client -> client.get("somehost", "somepath").putHeader(HttpHeaders.USER_AGENT.toString(), "smith"), req -> assertEquals(Collections.singletonList("smith"), req.headers().getAll(HttpHeaders.USER_AGENT)));
  }

  @Test
  public void testPutHeaders() throws Exception {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("foo","bar");
    headers.add("ping","pong");
    testRequest(client -> client.get("somehost", "somepath").putHeaders(headers), req -> {
      assertEquals("bar", req.headers().get("foo"));
      assertEquals("pong", req.headers().get("ping"));
    });
  }

  @Test
  public void testUserAgentHeaderRemoved() throws Exception {
    testRequest(client -> {
      HttpRequest<Buffer> request = client.get("somehost", "somepath");
      request.headers().remove(HttpHeaders.USER_AGENT);
      return request;
    }, req -> assertEquals(Collections.emptyList(), req.headers().getAll(HttpHeaders.USER_AGENT)));
  }

  @Test
  public void testGet() throws Exception {
    testRequest(HttpMethod.GET);
  }

  @Test
  public void testHead() throws Exception {
    testRequest(HttpMethod.HEAD);
  }

  @Test
  public void testDelete() throws Exception {
    testRequest(HttpMethod.DELETE);
  }

  private void testRequest(HttpMethod method) throws Exception {
    testRequest(client -> {
      switch (method.name()) {
        case "GET":
          return client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
        case "HEAD":
          return client.head(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
        case "DELETE":
          return client.delete(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
        default:
          fail("Invalid HTTP method");
          return null;
      }
    }, req -> assertEquals(method, req.method()));
  }

  @Test
  public void testPost() throws Exception {
    testRequestWithBody(HttpMethod.POST, false);
  }

  @Test
  public void testPostChunked() throws Exception {
    testRequestWithBody(HttpMethod.POST, true);
  }

  @Test
  public void testPut() throws Exception {
    testRequestWithBody(HttpMethod.PUT, false);
  }

  @Test
  public void testPutChunked() throws Exception {
    testRequestWithBody(HttpMethod.PUT, true);
  }

  @Test
  public void testPatch() throws Exception {
    testRequestWithBody(HttpMethod.PATCH, false);
  }

  @Test
  public void testProxyPerRequest() throws Exception {
    startProxy(null, ProxyType.HTTP);
    proxy.setForceUri("http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT);
    server.requestHandler(req -> req.response().setStatusCode(200).end());
    startServer();

    webClient
      .get("http://checkip.amazonaws.com/")
      .proxy(new ProxyOptions().setPort(proxy.port()))
      .send().onComplete(ar -> {
        if (ar.succeeded()) {
          // Obtain response
          HttpResponse<Buffer> response = ar.result();
          assertEquals(200, response.statusCode());
          assertEquals("http://checkip.amazonaws.com/", proxy.getLastUri());
          testComplete();
        } else {
          fail(ar.cause());
        }
      });
    await();
  }

  @Test
  public void testSendJsonObjectBody() throws Exception {
    JsonObject body = new JsonObject().put("wine", "Chateauneuf Du Pape").put("cheese", "roquefort");
    testSendBody(body, (contentType, buff) -> {
      assertEquals("application/json", contentType);
      assertEquals(body, buff.toJsonObject());
    });
  }

  @Test
  public void testSendJsonArrayBody() throws Exception {
    JsonArray body = new JsonArray().add(0).add(1).add(2);
    testSendBody(body, (contentType, buff) -> {
      assertEquals("application/json", contentType);
      assertEquals(body, buff.toJsonArray());
    });
  }

  @Test
  public void testSendBufferBody() throws Exception {
    Buffer body = TestUtils.randomBuffer(2048);
    testSendBody(body, (contentType, buff) -> assertEquals(body, buff));
  }

  @Test
  public void testSendJsonNullBody() throws Exception {
    testSendBody(null, (contentType, buff) -> {
      assertEquals("application/json", contentType);
//      assertEquals(body, buff.toJsonObject());
    });
  }

  @Test
  public void testConnectError() throws Exception {
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get.send().onComplete(onFailure(err -> {
      assertTrue(err instanceof ConnectException);
      complete();
    }));
    await();
  }

  @Repeat(times = 100)
  @Test
  public void testTimeoutRequestBeforeSending() throws Exception {
    NetServer server = vertx.createNetServer();
    server.connectHandler(so -> {
    });
    CountDownLatch latch = new CountDownLatch(1);
    server.listen(8080, "localhost").onComplete(onSuccess(v -> {
      latch.countDown();
    }));
    awaitLatch(latch);
    webClient
      .get(8080, "localhost", "/")
      .timeout(1)
      .send().onComplete(onFailure(err -> {
        testComplete();
      }));
    await();
  }

  @Test
  public void testRequestConnectError() throws Exception {
    HttpRequest<Buffer> post = webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    AtomicInteger closed = new AtomicInteger();
    Future<?> resp = post.sendStream(new ReadStream<>() {
      @Override
      public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        throw new UnsupportedOperationException();
      }
      @Override
      public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        throw new UnsupportedOperationException();
      }
      @Override
      public ReadStream<Buffer> pause() {
        throw new UnsupportedOperationException();
      }
      @Override
      public ReadStream<Buffer> resume() {
        throw new UnsupportedOperationException();
      }
      @Override
      public ReadStream<Buffer> fetch(long amount) {
        throw new UnsupportedOperationException();
      }
      @Override
      public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        throw new UnsupportedOperationException();
      }
      @Override
      public Pipe<Buffer> pipe() {
        return new Pipe<>() {
          @Override
          public Pipe<Buffer> endOnFailure(boolean end) {
            throw new UnsupportedOperationException();
          }
          @Override
          public Pipe<Buffer> endOnSuccess(boolean end) {
            throw new UnsupportedOperationException();
          }
          @Override
          public Pipe<Buffer> endOnComplete(boolean end) {
            throw new UnsupportedOperationException();
          }
          @Override
          public Future<Void> to(WriteStream<Buffer> dst) {
            throw new UnsupportedOperationException();
          }
          @Override
          public void close() {
            closed.incrementAndGet();
          }
        };
      }
    });
    assertWaitUntil(resp::isComplete);
    assertTrue(resp.failed());
    assertWaitUntil(() -> closed.get() == 1);
  }

  @Test
  public void testRequestSendError() throws Exception {
    HttpRequest<Buffer> post = webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<HttpConnection> conn = new AtomicReference<>();
    server.requestHandler(req -> {
      conn.set(req.connection());
      req.pause();
      latch.countDown();
    });
    startServer();
    AtomicReference<Handler<Buffer>> dataHandler = new AtomicReference<>();
    AtomicReference<Handler<Void>> endHandler = new AtomicReference<>();
    AtomicBoolean paused = new AtomicBoolean();
    post.sendStream(new ReadStream<Buffer>() {
          @Override
          public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
            return this;
          }
          @Override
          public ReadStream<Buffer> handler(Handler<Buffer> handler) {
            dataHandler.set(handler);
            return this;
          }
          @Override
          public ReadStream<Buffer> pause() {
            paused.set(true);
            return this;
          }
          @Override
          public ReadStream<Buffer> fetch(long amount) {
            throw new UnsupportedOperationException();
          }
          @Override
          public ReadStream<Buffer> resume() {
            paused.set(false);
            return this;
          }
          @Override
          public ReadStream<Buffer> endHandler(Handler<Void> handler) {
            endHandler.set(handler);
            return this;
          }
        }).onComplete(onFailure(err -> {
          // Should be a connection reset by peer or closed
//          assertNotNull(endHandler.get());
//          assertNotNull(dataHandler.get());
//          assertEquals("Connection was closed", err.getMessage());
          complete();
        }));
    assertWaitUntil(() -> dataHandler.get() != null);
    dataHandler.get().handle(TestUtils.randomBuffer(1024));
    awaitLatch(latch);
    while (!paused.get()) {
      dataHandler.get().handle(TestUtils.randomBuffer(1024));
    }
    conn.get().close();
    await();
  }

  @Test
  public void testRequestPumpError() throws Exception {
    waitFor(2);
    HttpRequest<Buffer> post = webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    CompletableFuture<Void> done = new CompletableFuture<>();
    server.requestHandler(req -> {
      req.response().closeHandler(v -> complete());
      req.handler(buff -> done.complete(null));
    });
    Throwable cause = new Throwable();
    startServer();
    post.sendStream(new ReadStream<Buffer>() {
          @Override
          public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
            if (handler != null) {
              done.thenAccept(v -> handler.handle(cause));
            }
            return this;
          }
          @Override
          public ReadStream<Buffer> handler(Handler<Buffer> handler) {
            if (handler != null) {
              handler.handle(TestUtils.randomBuffer(1024));
            }
            return this;
          }
          @Override
          public ReadStream<Buffer> fetch(long amount) {
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
          public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
            return this;
          }
        }).onComplete(onFailure(err -> {
          if (err instanceof StreamResetException && cause == err.getCause()) {
            complete();
          } else {
            fail(new Exception("Unexpected failure", err));
          }
        }));
    await();
  }

  @Test
  public void testRequestPumpErrorInStream() throws Exception {
    waitFor(2);
    CompletableFuture<Void> failSignal = new CompletableFuture<>();
    HttpRequest<Buffer> post = webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    server.requestHandler(req -> {
      req.response().closeHandler(v -> complete());
      failSignal.complete(null);
    });
    Throwable cause = new Throwable();
    startServer();
    post.sendStream(new ReadStream<Buffer>() {
      Handler<Throwable> exceptionHandler;
      @Override
      public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        exceptionHandler = handler;
        return this;
      }
      @Override
      public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        if (handler != null) {
          failSignal.whenComplete((v1, e) -> {
            vertx.runOnContext(v2 -> exceptionHandler.handle(cause));
          });
          handler.handle(Buffer.buffer("hello world"));
        }
        return this;
      }
      @Override
      public ReadStream<Buffer> fetch(long amount) {
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
      public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        return this;
      }
    }).onComplete(onFailure(err -> {
      assertEquals(StreamResetException.class, err.getClass());
      assertSame(cause, err.getCause());
      complete();
    }));
    await();
  }

  @Test
  public void testResponseBodyAsBuffer() throws Exception {
    Buffer expected = TestUtils.randomBuffer(2000);
    server.requestHandler(req -> req.response().end(expected));
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get.send().onComplete(onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(expected, resp.body());
      testComplete();
    }));
    await();
  }

  @Test
  public void testNullLiteralResponseBodyAsJsonObject() throws Exception {
    this.testResponseBodyAs(BodyCodec.jsonObject(), "null", this.onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(null, resp.body());
      testComplete();
    }));
  }

  @Test
  public void testAnotherJsonResponseBodyAsJsonObject() throws Exception {
    this.testResponseBodyAs(BodyCodec.jsonObject(), "1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  @Test
  public void testInvalidJsonResponseBodyAsJsonObject() throws Exception {
    this.testResponseBodyAs(BodyCodec.jsonObject(), "\"1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  @Test
  public void testNullLiteralResponseBodyAsJsonMapped() throws Exception {
    this.testResponseBodyAs(BodyCodec.json(WineAndCheese.class), "null", this.onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(null, resp.body());
      testComplete();
    }));
  }

  @Test
  public void testAnotherJsonResponseBodyAsJsonMapped() throws Exception {
    this.testResponseBodyAs(BodyCodec.json(WineAndCheese.class), "1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  @Test
  public void testInvalidJsonResponseBodyAsJsonMapped() throws Exception {
    this.testResponseBodyAs(BodyCodec.json(WineAndCheese.class), "\"1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  @Test
  public void testResponseBodyAsJsonArray() throws Exception {
    JsonArray expected = new JsonArray().add("cheese").add("wine");
    server.requestHandler(req -> req.response().end(expected.encode()));
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(BodyCodec.jsonArray())
      .send().onComplete(onSuccess(resp -> {
        assertEquals(200, resp.statusCode());
        assertEquals(expected, resp.body());
        testComplete();
      }));
    await();
  }

  @Test
  public void testNullLiteralResponseBodyAsJsonArray() throws Exception {
    this.testResponseBodyAs(BodyCodec.jsonArray(), "null", this.onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(null, resp.body());
      testComplete();
    }));
  }

  @Test
  public void testAnotherJsonResponseBodyAsJsonArray() throws Exception {
    this.testResponseBodyAs(BodyCodec.jsonArray(), "1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  @Test
  public void testInvalidJsonResponseBodyAsJsonArray() throws Exception {
    this.testResponseBodyAs(BodyCodec.jsonArray(), "\"1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  @Test
  public void testResponseBodyAsJsonArrayMapped() throws Exception {
    JsonArray expected = new JsonArray().add("cheese").add("wine");
    server.requestHandler(req -> req.response().end(expected.encode()));
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(BodyCodec.json(List.class))
      .send().onComplete(onSuccess(resp -> {
        assertEquals(200, resp.statusCode());
        assertEquals(expected.getList(), resp.body());
        testComplete();
      }));
    await();
  }

  @Test
  public void testNullLiteralResponseBodyAsJsonArrayMapped() throws Exception {
    this.testResponseBodyAs(BodyCodec.json(List.class), "null", this.onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(null, resp.body());
      testComplete();
    }));
  }

  @Test
  public void testAnotherJsonResponseBodyAsJsonArrayMapped() throws Exception {
    this.testResponseBodyAs(BodyCodec.json(List.class), "1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  @Test
  public void testInvalidJsonResponseBodyAsJsonArrayMapped() throws Exception {
    this.testResponseBodyAs(BodyCodec.json(List.class), "\"1234", this.onFailure(err -> {
      assertEquals(DecodeException.class, err.getClass());
      testComplete();
    }));
  }

  private <T> void testResponseBodyAs(BodyCodec<T> bodyCodec, String body, Handler<AsyncResult<HttpResponse<T>>> checker) throws Exception {
    server.requestHandler(req -> req.response().end(body));
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(bodyCodec)
      .send().onComplete(checker);
    await();
  }

  @Test
  public void testResponseBodyDiscarded() throws Exception {
    testResponseBodyAs(BodyCodec.none(), TestUtils.randomAlphaString(1024), onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(null, resp.body());
      testComplete();
    }));
  }

  @Test
  public void testResponseUnknownContentTypeBodyAsJsonObject() throws Exception {
    JsonObject expected = new JsonObject().put("cheese", "Goat Cheese").put("wine", "Condrieu");
    testResponseBody(expected.encode(), onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(expected, resp.bodyAsJsonObject());
      testComplete();
    }));
  }

  @Test
  public void testResponseUnknownContentTypeBodyAsJsonArray() throws Exception {
    JsonArray expected = new JsonArray().add("cheese").add("wine");
    testResponseBody(expected.encode(), onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(expected, resp.bodyAsJsonArray());
      testComplete();
    }));
  }

  @Test
  public void testResponseInvalidContentTypeBodyAs() throws Exception {
    testResponseBody("\"1234", onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      try {
        resp.bodyAsJsonObject();
        fail();
      } catch (DecodeException ignore) {
      }
      try {
        resp.bodyAsJsonArray();
        fail();
      } catch (DecodeException ignore) {
      }
      try {
        resp.bodyAsJson(WineAndCheese.class);
        fail();
      } catch (DecodeException ignore) {
      }
      testComplete();
    }));
  }
  @Test
  public void testResponseAnotherContentTypeBodyAs() throws Exception {
    testResponseBody("1234", onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      try {
        resp.bodyAsJsonObject();
        fail();
      } catch (DecodeException ignore) {
      }
      try {
        resp.bodyAsJsonArray();
        fail();
      } catch (DecodeException ignore) {
      }
      try {
        resp.bodyAsJson(WineAndCheese.class);
        fail();
      } catch (DecodeException ignore) {
      }
      testComplete();
    }));
  }

  @Test
  public void testResponseNullContentTypeBodyAs() throws Exception {
    testResponseBody("null", onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals(null, resp.bodyAsJsonObject());
      assertEquals(null, resp.bodyAsJsonArray());
      assertEquals(null, resp.bodyAsJson(WineAndCheese.class));
      testComplete();
    }));
  }

  @Test
  public void testResponseBodyStreamNoClose() throws Exception {
	  testResponseBodyStream(false);
  }

  @Test
  public void testResponseBodyStream() throws Exception {
	  testResponseBodyStream(true);
  }

  public void testResponseBodyStream(boolean close) throws Exception {
    AtomicBoolean paused = new AtomicBoolean();
    server.requestHandler(req -> {
      HttpServerResponse resp = req.response();
      resp.setChunked(true);
      vertx.setPeriodic(1, id -> {
        if (!resp.writeQueueFull()) {
          resp.write(TestUtils.randomAlphaString(1024));
        } else {
          resp.drainHandler(v -> resp.end());
          paused.set(true);
          vertx.cancelTimer(id);
        }
      });
    });
    startServer();
    CompletableFuture<Void> resume = new CompletableFuture<>();
    AtomicInteger size = new AtomicInteger();
    AtomicBoolean ended = new AtomicBoolean();
    WriteStream<Buffer> stream = new WriteStreamBase() {
      boolean paused = true;
      {
        resume.thenAccept(v -> {
          paused = false;
          if (drainHandler != null) {
            drainHandler.handle(null);
          }
        });
      }
      @Override
      public Future<Void> write(Buffer data) {
        size.addAndGet(data.length());
        return super.write(data);
      }
      @Override
      public Future<Void> end() {
        ended.set(true);
        return super.end();
      }

      @Override
      public boolean writeQueueFull() {
        return paused;
      }
    };
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(BodyCodec.pipe(stream, close))
      .send().onComplete(onSuccess(resp -> {
      assertEquals(close, ended.get());
      assertEquals(200, resp.statusCode());
      assertEquals(null, resp.body());
      testComplete();
    }));
    assertWaitUntil(paused::get);
    resume.complete(null);
    await();
  }

  @Test
  public void testResponseBodyStreamError() throws Exception {
    CompletableFuture<Void> fail = new CompletableFuture<>();
    server.requestHandler(req -> {
      HttpServerResponse resp = req.response();
      resp.setChunked(true);
      resp.write(TestUtils.randomBuffer(2048));
      fail.thenAccept(v -> req.connection().close());
    });
    startServer();
    AtomicInteger received = new AtomicInteger();
    WriteStream<Buffer> stream = new WriteStreamBase() {
      @Override
      public Future<Void> write(Buffer data) {
        received.addAndGet(data.length());
        return super.write(data);
      }
    };
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(BodyCodec.pipe(stream))
      .send().onComplete(onFailure(err -> testComplete()));
    assertWaitUntil(() -> received.get() == 2048);
    fail.complete(null);
    await();
  }

  @Test
  public void testResponseBodyCodecError() throws Exception {
    server.requestHandler(req -> {
      HttpServerResponse resp = req.response();
      resp.setChunked(true);
      resp.end(TestUtils.randomBuffer(2048));
    });
    startServer();
    RuntimeException cause = new RuntimeException();
    WriteStream<Buffer> stream = new WriteStreamBase() {
      Handler<Throwable> exceptionHandler;
      @Override
      public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        exceptionHandler = handler;
        return this;
      }
      @Override
      public Future<Void> write(Buffer data) {
        exceptionHandler.handle(cause);
        return Future.failedFuture(cause);
      }
    };
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(BodyCodec.pipe(stream))
      .send().onComplete(onFailure(err -> {
      assertSame(cause, err);
      testComplete();
    }));
    await();
  }

  @Test
  public void testResponseBodyCodecErrorBeforeResponseIsReceived() throws Exception {
    server.requestHandler(req -> {
      HttpServerResponse resp = req.response();
      resp.setChunked(true);
      resp.end(TestUtils.randomBuffer(2048));
    });
    startServer();
    RuntimeException cause = new RuntimeException();
    WriteStreamBase stream = new WriteStreamBase() {
    };
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    HttpRequest<Void> request = get.as(BodyCodec.pipe(stream));
    assertNotNull(stream.exceptionHandler);
    stream.exceptionHandler.handle(cause);
    request.send().onComplete(onFailure(err -> {
      assertSame(cause, err);
      testComplete();
    }));
    await();
  }

  @Test
  public void testAsyncFileResponseBodyStream() throws Exception {
    server.requestHandler(req -> {
      HttpServerResponse resp = req.response();
      resp.end(TestUtils.randomBuffer(1024 * 1024));
    });
    startServer();
    AtomicLong received = new AtomicLong();
    AtomicBoolean closed = new AtomicBoolean();
    AsyncFile file = new AsyncFile() {
      public Future<Void> write(Buffer buffer, long position) {
        throw new UnsupportedOperationException();
      }
      public Future<Buffer> read(Buffer buffer, int offset, long position, int length) {
        throw new UnsupportedOperationException();
      }
      public AsyncFile handler(Handler<Buffer> handler) { throw new UnsupportedOperationException(); }
      public AsyncFile pause() { throw new UnsupportedOperationException(); }
      public AsyncFile resume() { throw new UnsupportedOperationException(); }
      public AsyncFile endHandler(Handler<Void> handler) { throw new UnsupportedOperationException(); }
      public AsyncFile setWriteQueueMaxSize(int i) { throw new UnsupportedOperationException(); }
      public AsyncFile drainHandler(Handler<Void> handler) { throw new UnsupportedOperationException(); }
      public AsyncFile fetch(long l) { throw new UnsupportedOperationException(); }
      public Future<Void> end() { return close(); }
      public void end(Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
      public void close(Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
      public void write(Buffer buffer, long l, Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
      public AsyncFile read(Buffer buffer, int i, long l, int i1, Handler<AsyncResult<Buffer>> handler) { throw new UnsupportedOperationException(); }
      public Future<Void> flush() { throw new UnsupportedOperationException(); }
      public AsyncFile flush(Handler<AsyncResult<Void>> handler) { throw new UnsupportedOperationException(); }
      public AsyncFile setReadPos(long l) { throw new UnsupportedOperationException(); }
      public AsyncFile setReadLength(long l) { throw new UnsupportedOperationException(); }
      public long getReadLength() { throw new UnsupportedOperationException(); }
      public AsyncFile setWritePos(long l) { throw new UnsupportedOperationException(); }
      public AsyncFile setReadBufferSize(int i) { throw new UnsupportedOperationException(); }
      public long sizeBlocking() { return 0; }
      public Future<Long> size() { return Future.succeededFuture(0L); }
      public AsyncFile exceptionHandler(Handler<Throwable> handler) {
        return this;
      }
      public boolean writeQueueFull() {
        return false;
      }
      public long getWritePos() { throw new UnsupportedOperationException(); }
      public AsyncFileLock tryLock() { throw new UnsupportedOperationException(); }
      public AsyncFileLock tryLock(long l, long l1, boolean b) { throw new UnsupportedOperationException(); }
      public Future<AsyncFileLock> lock() { throw new UnsupportedOperationException(); }
      public void lock(Handler<AsyncResult<AsyncFileLock>> handler) { throw new UnsupportedOperationException(); }
      public Future<AsyncFileLock> lock(long l, long l1, boolean b) { throw new UnsupportedOperationException(); }
      public void lock(long l, long l1, boolean b, Handler<AsyncResult<AsyncFileLock>> handler) { throw new UnsupportedOperationException(); }

      public Future<Void> write(Buffer buffer) {
        Promise<Void> promise = Promise.promise();
        write(buffer, promise);
        return promise.future();
      }
      public void write(Buffer buffer, Completable<Void> handler) {
        received.addAndGet(buffer.length());
        if (handler != null) {
          handler.succeed();
        }
      }
      public Future<Void> close() {
        return Future.future(p -> {
          vertx.setTimer(500, id -> {
            closed.set(true);
            p.complete();
          });
        });
      }
    };
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(BodyCodec.pipe(file))
      .send().onComplete(onSuccess(v -> {
        assertEquals(1024 * 1024, received.get());
        assertTrue(closed.get());
        testComplete();
      }));
    await();
  }

  @Test
  public void testResponseJsonObjectMissingBody() throws Exception {
    testResponseMissingBody(BodyCodec.jsonObject());
  }

  @Test
  public void testResponseJsonMissingBody() throws Exception {
    testResponseMissingBody(BodyCodec.json(WineAndCheese.class));
  }

  @Test
  public void testResponseWriteStreamMissingBody() throws Exception {
    AtomicInteger length = new AtomicInteger();
    AtomicBoolean ended = new AtomicBoolean();
    WriteStream<Buffer> stream = new WriteStreamBase() {
      @Override
      public Future<Void> write(Buffer data) {
        length.addAndGet(data.length());
        return super.write(data);
      }
      @Override
      public Future<Void> end() {
        ended.set(true);
        return super.end();
      }
    };
    testResponseMissingBody(BodyCodec.pipe(stream));
    assertTrue(ended.get());
    assertEquals(0, length.get());
  }

  private <R> void testResponseMissingBody(BodyCodec<R> codec) throws Exception {
    server.requestHandler(req -> req.response().setStatusCode(403).end());
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(codec)
      .send().onComplete(onSuccess(resp -> {
      assertEquals(403, resp.statusCode());
      assertNull(resp.body());
      testComplete();
    }));
    await();
  }

  @Test
  public void testHttpResponseError() throws Exception {
    server.requestHandler(req -> {
      HttpServerResponse resp = req.response();
      resp.setChunked(true).write(Buffer.buffer("some-data"));
      req.connection().close();
    });
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get
      .as(BodyCodec.jsonObject())
      .send().onComplete(onFailure(err -> {
      assertTrue(err instanceof VertxException);
      testComplete();
    }));
    await();
  }

  @Test
  public void testTimeout() throws Exception {
    AtomicInteger count = new AtomicInteger();
    server.requestHandler(req -> count.incrementAndGet());
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    get.timeout(50).send().onComplete(onFailure(err -> {
      assertTrue(err instanceof TimeoutException);
      testComplete();
    }));
    await();
  }

  @Test
  public void testQueryParam() throws Exception {
    testRequest(client -> client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/").addQueryParam("param", "param_value"), req -> {
      assertEquals("param=param_value", req.query());
      assertEquals("param_value", req.getParam("param"));
    });
  }

  @Test
  public void testQueryParamMulti() throws Exception {
    testRequest(client -> client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/").addQueryParam("param", "param_value1").addQueryParam("param", "param_value2"), req -> {
      assertEquals("param=param_value1&param=param_value2", req.query());
      assertEquals(Arrays.asList("param_value1", "param_value2"), req.params().getAll("param"));
    });
  }

  @Test
  public void testQueryParamAppend() throws Exception {
    testRequest(client -> client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/?param1=param1_value1").addQueryParam("param1", "param1_value2").addQueryParam("param2", "param2_value"), req -> {
      assertEquals("param1=param1_value1&param1=param1_value2&param2=param2_value", req.query());
      assertEquals("param1_value1", req.getParam("param1"));
      assertEquals("param2_value", req.getParam("param2"));
    });
  }

  @Test
  public void testOverwriteQueryParams() throws Exception {
    testRequest(client -> client.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/?param=param_value1").setQueryParam("param", "param_value2"), req -> {
      assertEquals("param=param_value2", req.query());
      assertEquals("param_value2", req.getParam("param"));
    });
  }

  @Test
  public void testQueryParamEncoding() throws Exception {
    testRequest(client -> client
      .get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/")
      .addQueryParam("param1", " ")
      .addQueryParam("param2", "\u20AC"), req -> {
      assertEquals("param1=%20&param2=%E2%82%AC", req.query());
      assertEquals(" ", req.getParam("param1"));
      assertEquals("\u20AC", req.getParam("param2"));
    });
  }

  @Test
  public void testFormUrlEncoded() throws Exception {
    server.requestHandler(req -> {
      req.setExpectMultipart(true);
      req.endHandler(v -> {
        assertEquals("param1_value", req.getFormAttribute("param1"));
        req.response().end();
      });
    });
    startServer();
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("param1", "param1_value");
    HttpRequest<Buffer> builder = webClient.post("/somepath");
    builder.sendForm(form).onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testFormUrlEncodedWithCharset() throws Exception {
    String str = "ø";
    String expected = URLDecoder.decode(URLEncoder.encode(str, StandardCharsets.ISO_8859_1.name()), StandardCharsets.UTF_8.name());
    server.requestHandler(req -> {
      req.setExpectMultipart(true);
      req.endHandler(v -> {
        String val = req.getFormAttribute("param1");
        assertEquals(expected, val);
        req.response().end();
      });
    });
    startServer();
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("param1", str);
    HttpRequest<Buffer> builder = webClient.post("/somepath");
    builder.sendForm(form, StandardCharsets.ISO_8859_1.name()).onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testFormUrlEncodedUnescaped() throws Exception {
    server.requestHandler(req -> {
      req.setExpectMultipart(true);
      req.bodyHandler(body -> {
        assertEquals("grant_type=client_credentials&resource=https%3A%2F%2Fmanagement.core.windows.net%2F", body.toString());
        req.response().end();
      });
    });
    startServer();
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form
      .set("grant_type", "client_credentials")
      .set("resource", "https://management.core.windows.net/");
    HttpRequest<Buffer> builder = webClient.post("/somepath");
    builder.sendForm(form).onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testFormUrlEncodedMultipleHeaders() throws Exception {
    server.requestHandler(req -> {
      req.setExpectMultipart(true);
      req.endHandler(v -> {
        assertEquals(Arrays.asList("1", "2"), req.headers().getAll("bla"));
        req.response().end();
      });
    });
    startServer();
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    HttpRequest<Buffer> builder = webClient.post("/somepath");
    builder.putHeader((CharSequence) "bla", Arrays.asList("1", "2"));
    builder.sendForm(form).onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testFormMultipart() throws Exception {
    server.requestHandler(req -> {
      assertTrue(req.getHeader(HttpHeaders.CONTENT_TYPE).startsWith("multipart/form-data"));
      req.setExpectMultipart(true);
      req.endHandler(v -> {
        assertEquals("param1_value", req.getFormAttribute("param1"));
        req.response().end();
      });
    });
    startServer();
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("param1", "param1_value");
    HttpRequest<Buffer> builder = webClient.post("/somepath");
    builder.putHeader("content-type", "multipart/form-data");
    builder.sendForm(form).onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testFormMultipartWithCharset() throws Exception {
    server.requestHandler(req -> {
      req.body().onComplete(onSuccess(body -> {
        System.out.println("body = " + body);
        assertTrue(body.toString().contains("content-type: text/plain; charset=ISO-8859-1"));
        req.response().end();
      }));
    });
    startServer();
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("param1", "param1_value");
    HttpRequest<Buffer> builder = webClient.post("/somepath");
    builder.putHeader("content-type", "multipart/form-data");
    builder.sendForm(form, "ISO-8859-1").onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testFileUploadFormMultipart32B() throws Exception {
    testFileUploadFormMultipart(32, false);
  }

  @Test
  public void testFileUploadFormMultipart32K() throws Exception {
    testFileUploadFormMultipart(32 * 1024, false);
  }

  @Test
  public void testFileUploadFormMultipart32M() throws Exception {
    testFileUploadFormMultipart(32 * 1024 * 1024, false);
  }

  @Test
  public void testMemoryFileUploadFormMultipart() throws Exception {
    testFileUploadFormMultipart(32 * 1024, true);
  }

  private void testFileUploadFormMultipart(int size, boolean memory) throws Exception {
    Buffer content = Buffer.buffer(TestUtils.randomAlphaString(size));
    Upload upload;
    if (memory) {
      upload = Upload.memoryUpload("test", "test.txt", content);
    } else {
      upload = Upload.fileUpload("test", "test.txt", content);
    }
    MultipartForm form = MultipartForm.create()
      .attribute("toolkit", "vert.x")
      .attribute("runtime", "jvm");
    testFileUploadFormMultipart(form, Collections.singletonList(upload), true, (req, uploads) -> {
      assertEquals("vert.x", req.getFormAttribute("toolkit"));
      assertEquals("jvm", req.getFormAttribute("runtime"));
      assertEquals(1, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test.txt", uploads.get(0).filename);
      assertEquals(content, uploads.get(0).data);
    });
  }

  @Test
  public void testFileUploadsFormMultipart() throws Exception {
    Buffer content1 = Buffer.buffer(TestUtils.randomAlphaString(16));
    Buffer content2 = Buffer.buffer(TestUtils.randomAlphaString(16));
    List<Upload> toUpload = Arrays.asList(
      Upload.fileUpload("test1", "test1.txt", content1),
      Upload.fileUpload("test2", "test2.txt", content2)
    );
    testFileUploadFormMultipart(MultipartForm.create(), toUpload, true, (req, uploads) -> {
      assertEquals(2, uploads.size());
      assertEquals("test1", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals("UTF-8", uploads.get(0).charset);
      assertEquals(content1, uploads.get(0).data);
      assertEquals("test2", uploads.get(1).name);
      assertEquals("test2.txt", uploads.get(1).filename);
      assertEquals("UTF-8", uploads.get(1).charset);
      assertEquals(content2, uploads.get(1).data);
    });
  }

  @Test
  public void testFileUploadsFormMultipartWithCharset() throws Exception {
    Buffer content = Buffer.buffer(TestUtils.randomAlphaString(16));
    List<Upload> toUpload = Collections.singletonList(Upload.fileUpload("test1", "test1.txt", content));
    testFileUploadFormMultipart(MultipartForm.create().setCharset(StandardCharsets.ISO_8859_1), toUpload, true, (req, uploads) -> {
      assertEquals(1, uploads.size());
      assertEquals("test1", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals("ISO-8859-1", uploads.get(0).charset);
    });
  }

  @Test
  public void testFileUploadsSameNameFormMultipart() throws Exception {
    Buffer content1 = Buffer.buffer(TestUtils.randomAlphaString(16));
    Buffer content2 = Buffer.buffer(TestUtils.randomAlphaString(16));
    List<Upload> toUpload = Arrays.asList(
      Upload.fileUpload("test", "test1.txt", content1),
      Upload.fileUpload("test", "test2.txt", content2)
    );
    testFileUploadFormMultipart(MultipartForm.create(), toUpload, true, (req, uploads) -> {
      assertEquals(2, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals(content1, uploads.get(0).data);
      assertEquals("test", uploads.get(1).name);
      assertEquals("test2.txt", uploads.get(1).filename);
      assertEquals(content2, uploads.get(1).data);
    });
  }

  @Test
  public void testFileUploadsSameNameFormMultipartDisableMultipartMixed() throws Exception {
    Buffer content1 = Buffer.buffer(TestUtils.randomAlphaString(16));
    Buffer content2 = Buffer.buffer(TestUtils.randomAlphaString(16));
    List<Upload> toUpload = Arrays.asList(
      Upload.fileUpload("test", "test1.txt", content1),
      Upload.fileUpload("test", "test2.txt", content2)
    );
    testFileUploadFormMultipart(MultipartForm.create(), toUpload, false, (req, uploads) -> {
      assertEquals(2, uploads.size());
      assertEquals("test", uploads.get(0).name);
      assertEquals("test1.txt", uploads.get(0).filename);
      assertEquals(content1, uploads.get(0).data);
      assertEquals("test", uploads.get(1).name);
      assertEquals("test2.txt", uploads.get(1).filename);
      assertEquals(content2, uploads.get(1).data);
    });
  }

  private void testFileUploadFormMultipart(
      MultipartForm form,
      List<Upload> toUpload,
      boolean multipartMixed,
      BiConsumer<HttpServerRequest,
      List<Upload>> checker) throws Exception {
    File[] testFiles = new File[toUpload.size()];
    for (int i = 0;i < testFiles.length;i++) {
      Upload upload = toUpload.get(i);
      if (upload.file) {
        String name = upload.filename;
        testFiles[i] = testFolder.newFile(name);
        vertx.fileSystem().writeFileBlocking(testFiles[i].getPath(), upload.data);
        form.textFileUpload(toUpload.get(i).name, toUpload.get(i).filename, testFiles[i].getPath(), "text/plain");
      } else {
        form.textFileUpload(toUpload.get(i).name, toUpload.get(i).filename, upload.data, "text/plain");
      }
    }

    server.requestHandler(req -> {
      req.setExpectMultipart(true);
      List<Upload> uploads = new ArrayList<>();
      req.uploadHandler(upload -> {
        Buffer fileBuffer = Buffer.buffer();
        assertEquals("text/plain", upload.contentType());
        upload.handler(fileBuffer::appendBuffer);
        upload.endHandler(v -> {
          uploads.add(new Upload(upload.name(), upload.filename(), true, upload.charset(), fileBuffer));
        });
      });
      req.endHandler(v -> {
        checker.accept(req, uploads);
        req.response().end();
      });
    });
    startServer();

    HttpRequest<Buffer> builder = webClient.post("somepath");
    builder.multipartMixed(multipartMixed);
    builder.sendMultipartForm(form).onComplete(onSuccess(resp -> complete()));
    await();
  }

  static class Upload {
    final String name;
    final String filename;
    final String charset;
    final Buffer data;
    final boolean file;
    private Upload(String name, String filename, boolean file, String charset, Buffer data) {
      this.name = name;
      this.filename = filename;
      this.charset = charset;
      this.data = data;
      this.file = file;
    }
    static Upload fileUpload(String name, String filename, Buffer data) {
      return new Upload(name, filename, true, null, data);
    }
    static Upload memoryUpload(String name, String filename, Buffer data) {
      return new Upload(name, filename, false, null, data);
    }
  }

  @Test
  public void testMultipartFormMultipleHeaders() throws Exception {
    server.requestHandler(req -> {
      req.setExpectMultipart(true);
      req.endHandler(v -> {
        assertEquals(Arrays.asList("1", "2"), req.headers().getAll("bla"));
        req.response().end();
      });
    });
    startServer();
    HttpRequest<Buffer> builder = webClient.post("somepath");
    MultipartForm form = MultipartForm.create();
    builder.putHeader((CharSequence) "bla", Arrays.asList("1", "2"));
    builder.sendMultipartForm(form).onComplete(onSuccess(resp -> complete()));
    await();
  }

  @Test
  public void testFileUploadWhenFileDoesNotExist() {
    HttpRequest<Buffer> builder = webClient.post("somepath");
    MultipartForm form = MultipartForm.create()
      .textFileUpload("file", "nonexistentFilename", "nonexistentPathname", "text/plain");

    builder.sendMultipartForm(form).onComplete(onFailure(err -> {
      complete();
    }));
    await();
  }

  @Test
  public void testFileUploads() throws Exception {
    server.requestHandler(req -> {
      fail("Should not be called");
    });
    startServer();
    HttpRequest<Buffer> builder = webClient.post("somepath");
    MultipartForm form = MultipartForm.create()
      .textFileUpload("file", "nonexistentFilename", "nonexistentPathname", "text/plain");
    builder.sendMultipartForm(form).onComplete(onFailure(err -> {
      complete();
    }));
    await();
  }

  @Test
  public void testDefaultFollowRedirects() throws Exception {
    testFollowRedirects(null, true);
  }

  @Test
  public void testFollowRedirects() throws Exception {
    testFollowRedirects(true, true);
  }

  @Test
  public void testDoNotFollowRedirects() throws Exception {
    testFollowRedirects(false, false);
  }

  private void testFollowRedirects(Boolean set, boolean expect) throws Exception {
    waitFor(2);
    String location = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/ok";
    server.requestHandler(req -> {
      if (!req.headers().contains("foo", "bar", true)) {
        fail("Missing expected header");
        return;
      }
      assertEquals(Collections.singletonList("bar"), req.headers().getAll("foo"));
      if (req.path().equals("/redirect")) {
        req.response().setStatusCode(301).putHeader("Location", location).end();
        if (!expect) {
          complete();
        }
      } else {
        req.response().end(req.path());
        if (expect) {
          complete();
        }
      }
    });
    startServer();
    HttpRequest<Buffer> builder = webClient.get("/redirect")
      .putHeader("foo", "bar");
    if (set != null) {
      builder = builder.followRedirects(set);
    }
    builder.send().onComplete(onSuccess(resp -> {
      if (expect) {
        assertEquals(200, resp.statusCode());
        assertEquals("/ok", resp.body().toString());
        assertEquals(1, resp.followedRedirects().size());
        assertEquals(location, resp.followedRedirects().get(0));
      } else {
        assertEquals(301, resp.statusCode());
        assertEquals(location, resp.getHeader("location"));
      }
      complete();
    }));
    await();
  }

  @Test
  public void testMultipleRedirect() throws Exception {
    waitFor(2);
    String middleRedirect = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/middle";
    String endRedirect = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/ok";
    server.requestHandler(req -> {
      if (!req.headers().contains("foo", "bar", true)) {
        fail("Missing expected header");
        return;
      }
      if (req.path().equals("/redirect")) {
        req.response().setStatusCode(301).putHeader("Location", middleRedirect).end();
      } else if (req.path().equals("/middle")) {
        req.response().setStatusCode(301).putHeader("Location", endRedirect).end();
      } else {
        req.response().end(req.path());
        complete();
      }
    });
    startServer();
    webClient.get("/redirect")
      .putHeader("foo", "bar")
      .followRedirects(true)
      .send().onComplete(onSuccess(resp -> {
        assertEquals(200, resp.statusCode());
        assertEquals("/ok", resp.body().toString());
        assertEquals(2, resp.followedRedirects().size());
        assertEquals(middleRedirect, resp.followedRedirects().get(0));
        assertEquals(endRedirect, resp.followedRedirects().get(1));
        complete();
    }));
    await();
  }

  @Test
  public void testInvalidRedirection() throws Exception {
    server.requestHandler(req -> {
      assertEquals(HttpMethod.POST, req.method());
      assertEquals("/redirect", req.path());
      req.response().setStatusCode(302).putHeader("Location", "http://www.google.com").end();
    });
    startServer();
    HttpRequest<Buffer> builder = webClient
      .post("/redirect")
      .followRedirects(true);
    builder.send().onComplete(onSuccess(resp -> {
      assertEquals(302, resp.statusCode());
      assertEquals("http://www.google.com", resp.getHeader("Location"));
      assertNull(resp.body());
      complete();
    }));
    await();
  }

  @Test
  public void testRedirectLimit() throws Exception {
    String location = "http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT + "/redirect";
    server.requestHandler(req -> {
      assertEquals(HttpMethod.GET, req.method());
      assertEquals("/redirect", req.path());
      req.response().setStatusCode(302).putHeader("Location", location).end();
    });
    startServer();
    HttpRequest<Buffer> builder = webClient
      .get("/redirect")
      .followRedirects(true);
    builder.send().onComplete(onSuccess(resp -> {
      assertEquals(302, resp.statusCode());
      assertEquals(location, resp.getHeader("Location"));
      assertNull(resp.body());
      complete();
    }));
    await();
  }

  @Test
  public void testVirtualHost() throws Exception {
    server.requestHandler(req -> {
      assertEquals("another-host:8080", req.authority().toString());
      req.response().end();
    });
    startServer();
    HttpRequest<Buffer> req = webClient.get("/test").virtualHost("another-host");
    req.send().onComplete(onSuccess(resp -> testComplete()));
    await();
  }

  @Test
  public void testSocketAddress() throws Exception {
    server.requestHandler(req -> {
      assertEquals("another-host:8080", req.authority().toString());
      req.response().end();
    });
    startServer();
    SocketAddress addr = SocketAddress.inetSocketAddress(8080, "localhost");
    HttpRequest<Buffer> req = webClient.request(HttpMethod.GET, addr, 8080, "another-host", "/test");
    req.send().onComplete(onSuccess(resp -> testComplete()));
    await();
  }

  @Test
  public void testRequest() throws Exception {
    String headerKey = "header1", headerValue = "value1";
    RequestOptions options = new RequestOptions().addHeader(headerKey, headerValue);

    HttpRequest<Buffer> request = webClient.request(HttpMethod.GET, options);
    assertThat(request.headers().get(headerKey), equalTo(headerValue));

    request = webClient.request(HttpMethod.GET, SocketAddress.inetSocketAddress(8080, "localhost"), options);
    assertThat(request.headers().get(headerKey), equalTo(headerValue));

    request = webClient.request(HttpMethod.GET, new RequestOptions());
    assertThat(request.headers().get(headerKey), nullValue());
  }

  @Test
  public void testRequestOptions() throws Exception {
    ProxyOptions proxyOptions = new ProxyOptions().setHost("proxy-host");
    RequestOptions options = new RequestOptions().setHost("another-host").setPort(8080).setSsl(true)
      .setURI("/test").setTimeout(500).setProxyOptions(proxyOptions).setFollowRedirects(true);
    HttpRequest<Buffer> request = webClient.request(HttpMethod.GET, options);

    assertThat(request.host(), equalTo("another-host"));
    assertThat(request.port(), equalTo(8080));
    assertThat(request.ssl(), equalTo(true));
    assertThat(request.uri(), equalTo("/test"));
    assertThat(request.timeout(), equalTo(500l));
    assertThat(request.followRedirects(), equalTo(true));
    assertThat(request.proxy(), is(not(equalTo(proxyOptions))));
    assertThat(request.proxy().getHost(), equalTo("proxy-host"));
  }

  @Test
  public void testTLSEnabled() throws Exception {
    testTLS(true, true, client -> client.get("/"));
  }

  @Test
  public void testTLSEnabledDisableRequestTLS() throws Exception {
    testTLS(true, false, client -> client.get("/").ssl(false));
  }

  @Test
  public void testTLSEnabledEnableRequestTLS() throws Exception {
    testTLS(true, true, client -> client.get("/").ssl(true));
  }

  @Test
  public void testTLSDisabledDisableRequestTLS() throws Exception {
    testTLS(false, false, client -> client.get("/").ssl(false));
  }

  @Test
  public void testTLSDisabledEnableRequestTLS() throws Exception {
    testTLS(false, true, client -> client.get("/").ssl(true));
  }

  @Test
  public void testTLSEnabledDisableRequestTLSAbsURI() throws Exception {
    testTLS(true, false, client -> client.getAbs("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
  }

  @Test
  public void testTLSEnabledEnableRequestTLSAbsURI() throws Exception {
    testTLS(true, true, client -> client.getAbs("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
  }

  @Test
  public void testTLSDisabledDisableRequestTLSAbsURI() throws Exception {
    testTLS(false, false, client -> client.getAbs("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
  }

  @Test
  public void testTLSDisabledEnableRequestTLSAbsURI() throws Exception {
    testTLS(false, true, client -> client.getAbs("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT));
  }

  @Test
  public void testTLSEnabledDisableRequestTLSAbsURIWithOptions() throws Exception {
    testTLS(true, false, client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT)));
  }

  @Test
  public void testTLSEnabledEnableRequestTLSAbsURIWithOptions() throws Exception {
    testTLS(true, true, client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT)));
  }

  @Test
  public void testTLSDisabledDisableRequestTLSAbsURIWithOptions() throws Exception {
    testTLS(false, false, client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("http://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT)));
  }

  @Test
  public void testTLSDisabledEnableRequestTLSAbsURIWithOptions() throws Exception {
    testTLS(false, true, client -> client.request(HttpMethod.GET, new RequestOptions().setAbsoluteURI("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT)));
  }

  /**
   * Regression test for issue #563 (https://github.com/vert-x3/vertx-web/issues/563)
   * <p>
   * Only occurred when {@link WebClientOptions#isSsl()} was false for an SSL request.
   */
  @Test
  public void testTLSQueryParametersIssue563() throws Exception {
    testTLS(false, true,
      client -> client.getAbs("https://" + DEFAULT_HTTPS_HOST + ":" + DEFAULT_HTTPS_PORT)
        .addQueryParam("query1", "value1")
        .addQueryParam("query2", "value2"),
      serverRequest -> assertEquals("query1=value1&query2=value2", serverRequest.query()));
  }

  private void testTLS(boolean clientSSL, boolean serverSSL, Function<WebClient, HttpRequest<Buffer>> requestProvider) throws Exception {
    testTLS(clientSSL, serverSSL, requestProvider, null);
  }

  private void testTLS(boolean clientSSL, boolean serverSSL, Function<WebClient, HttpRequest<Buffer>> requestProvider, Consumer<HttpServerRequest> serverAssertions) throws Exception {
    WebClientOptions clientOptions = new WebClientOptions()
      .setSsl(clientSSL)
      .setTrustAll(true)
      .setDefaultHost(DEFAULT_HTTPS_HOST)
      .setDefaultPort(DEFAULT_HTTPS_PORT);
    HttpServerOptions serverOptions = new HttpServerOptions()
      .setSsl(serverSSL)
      .setKeyCertOptions(Cert.SERVER_JKS.get())
      .setPort(DEFAULT_HTTPS_PORT)
      .setHost(DEFAULT_HTTPS_HOST);
    testTLS(clientOptions, serverOptions, requestProvider, serverAssertions);
  }

  @Test
  public void testVirtualHostSNI() throws Exception {
    WebClientOptions clientOptions = new WebClientOptions()
      .setTrustAll(true)
      .setDefaultHost(DEFAULT_HTTPS_HOST)
      .setDefaultPort(DEFAULT_HTTPS_PORT);
    HttpServerOptions serverOptions = new HttpServerOptions()
      .setSsl(true)
      .setSni(true)
      .setKeyCertOptions(Cert.SNI_JKS.get())
      .setPort(DEFAULT_HTTPS_PORT)
      .setHost(DEFAULT_HTTPS_HOST);
     testTLS(clientOptions, serverOptions, req -> req.get("/").virtualHost("host2.com").ssl(true), req -> {
       assertEquals("host2.com", req.connection().indicatedServerName());
    });
  }

  private void testTLS(WebClientOptions clientOptions, HttpServerOptions serverOptions, Function<WebClient, HttpRequest<Buffer>> requestProvider, Consumer<HttpServerRequest> serverAssertions) throws Exception {
    WebClient sslClient = WebClient.create(vertx, clientOptions);
    HttpServer sslServer = vertx.createHttpServer(serverOptions);
    sslServer.requestHandler(req -> {
      assertEquals(serverOptions.isSsl(), req.isSSL());
      if (serverAssertions != null) {
        serverAssertions.accept(req);
      }
      req.response().end();
    });
    try {
      startServer(sslServer);
      HttpRequest<Buffer> builder = requestProvider.apply(sslClient);
      builder.send().onComplete(onSuccess(resp -> testComplete()));
      await();
    } finally {
      sslClient.close();
      sslServer.close();
    }
  }

  @Test
  public void testHttpProxyFtpRequest() throws Exception {
    startProxy(null, ProxyType.HTTP);
    proxy.setForceUri("http://" + DEFAULT_HTTP_HOST + ":" + DEFAULT_HTTP_PORT);
    server.requestHandler(req -> req.response().setStatusCode(200).end());
    startServer();

    WebClientOptions options = new WebClientOptions();
    options.setProxyOptions(new ProxyOptions().setPort(proxy.port()));
    WebClient client = WebClient.create(vertx, options);
    client
    .get("ftp://ftp.gnu.org/gnu/")
    .send().onComplete(ar -> {
      if (ar.succeeded()) {
        // Obtain response
        HttpResponse<Buffer> response = ar.result();
        assertEquals(200, response.statusCode());
        assertEquals("ftp://ftp.gnu.org/gnu/", proxy.getLastUri());
        testComplete();
      } else {
        fail(ar.cause());
      }
    });
    await();
  }

  @Test
  public void testStreamHttpServerRequest() throws Exception {
    Buffer expected = TestUtils.randomBuffer(10000);
    HttpServer server2 = vertx.createHttpServer(new HttpServerOptions().setPort(8081)).requestHandler(req -> req.bodyHandler(body -> {
      assertEquals(body, expected);
      req.response().end();
    }));
    startServer(server2);
    WebClient webClient = WebClient.create(vertx);
    try {
      server.requestHandler(req -> webClient.postAbs("http://localhost:8081/")
        .sendStream(req).onComplete(onSuccess(resp -> req.response().end("ok"))));
      startServer();
      webClient.post(8080, "localhost", "/").sendBuffer(expected).onComplete(onSuccess(resp -> {
        assertEquals("ok", resp.bodyAsString());
        complete();
      }));
      await();
    } finally {
      server2.close();
    }
  }

  private void testExpectation(boolean shouldFail,
                               Consumer<HttpRequest<?>> modifier,
                               Consumer<HttpServerResponse> bilto) throws Exception {
    testExpectation(shouldFail, modifier, bilto, null);
  }

  private void testExpectation(boolean shouldFail,
                               Consumer<HttpRequest<?>> modifier,
                               Consumer<HttpServerResponse> bilto,
                               Consumer<AsyncResult<?>> resultTest) throws Exception {
    server.requestHandler(request -> bilto.accept(request.response()));
    startServer();
    HttpRequest<Buffer> request = webClient
      .get("/test");
    modifier.accept(request);
    request.send().onComplete(ar -> {
      if (ar.succeeded()) {
        assertFalse("Expected response success", shouldFail);
      } else {
        assertTrue("Expected response failure", shouldFail);
      }
      if (resultTest != null) resultTest.accept(ar);
      testComplete();
    });
    await();
  }

  // New expectation API

  @Test
  public void testExpectFail_2() throws Exception {
    testExpectation_2(true,
      value -> false,
      HttpServerResponse::end);
  }

  @Test
  public void testExpectPass_2() throws Exception {
    testExpectation_2(false,
      value -> true,
      HttpServerResponse::end);
  }

  @Test
  public void testExpectStatusFail_2() throws Exception {
    testExpectation_2(true,
      HttpResponseExpectation.status(200),
      resp -> resp.setStatusCode(201).end());
  }

  @Test
  public void testExpectStatusPass_2() throws Exception {
    testExpectation_2(false,
      HttpResponseExpectation.status(200),
      resp -> resp.setStatusCode(200).end());
  }

  @Test
  public void testExpectStatusRangeFail_2() throws Exception {
    testExpectation_2(true,
      HttpResponseExpectation.SC_SUCCESS,
      resp -> resp.setStatusCode(500).end());
  }

  @Test
  public void testExpectStatusRangePass1_2() throws Exception {
    testExpectation_2(false,
      HttpResponseExpectation.SC_SUCCESS,
      resp -> resp.setStatusCode(200).end());
  }

  @Test
  public void testExpectStatusRangePass2_2() throws Exception {
    testExpectation_2(false,
      HttpResponseExpectation.SC_SUCCESS,
      resp -> resp.setStatusCode(299).end());
  }

  @Test
  public void testExpectContentTypeFail_2() throws Exception {
    testExpectation_2(true,
      HttpResponseExpectation.JSON,
      HttpServerResponse::end);
  }

  @Test
  public void testExpectOneOfContentTypesFail_2() throws Exception {
    testExpectation_2(true,
      HttpResponseExpectation.contentType(Arrays.asList("text/plain", "text/csv")),
      httpServerResponse -> httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML).end());
  }

  @Test
  public void testExpectContentTypePass_2() throws Exception {
    testExpectation_2(false,
      HttpResponseExpectation.JSON,
      resp -> resp.putHeader("content-type", "application/JSON").end());
  }

  @Test
  public void testExpectContentTypeWithEncodingPass_2() throws Exception {
    testExpectation_2(false,
      HttpResponseExpectation.JSON,
      resp -> resp.putHeader("content-type", "application/JSON;charset=UTF-8").end());
  }

  @Test
  public void testExpectOneOfContentTypesPass_2() throws Exception {
    testExpectation_2(false,
      HttpResponseExpectation.contentType(Arrays.asList("text/plain", "text/HTML")),
      httpServerResponse -> httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML).end());
  }

  @Test
  public void testExpectCustomException_2() throws Exception {
    Expectation<HttpResponseHead> expectation = ((Expectation<HttpResponseHead>) value -> false)
      .wrappingFailure((head, err) -> new CustomException("boom"));
    testExpectation_2(true, expectation, HttpServerResponse::end, ar -> {
      Throwable cause = ar.cause();
      assertThat(cause, instanceOf(CustomException.class));
      CustomException customException = (CustomException) cause;
      assertEquals("boom", customException.getMessage());
    });
  }

  @Test
  public void testExpectCustomExceptionWithResponseBody_2() throws Exception {
    UUID uuid = UUID.randomUUID();
    Expectation<HttpResponseHead> expectation = HttpResponseExpectation.SC_SUCCESS.wrappingFailure((head, err) -> {
      JsonObject body = ((HttpResponse<?>) head).bodyAsJsonObject();
      return new CustomException(UUID.fromString(body.getString("tag")), body.getString("message"));
    });
    testExpectation_2(true, expectation, httpServerResponse -> {
      httpServerResponse
        .setStatusCode(400)
        .end(new JsonObject().put("tag", uuid.toString()).put("message", "tilt").toBuffer());
    }, ar -> {
      Throwable cause = ar.cause();
      assertThat(cause, instanceOf(CustomException.class));
      CustomException customException = (CustomException) cause;
      assertEquals("tilt", customException.getMessage());
      assertEquals(uuid, customException.tag);
    });
  }

  @Test
  public void testExpectCustomExceptionWithStatusCode_2() throws Exception {
    UUID uuid = UUID.randomUUID();
    int statusCode = 400;

    Expectation<HttpResponseHead> expectation = HttpResponseExpectation.SC_SUCCESS
      .wrappingFailure((head, err) -> new CustomException(uuid, String.valueOf(head.statusCode())));

    testExpectation_2(true, expectation, httpServerResponse -> {
      httpServerResponse
        .setStatusCode(statusCode)
        .end(TestUtils.randomBuffer(2048));
    }, ar -> {
      Throwable cause = ar.cause();
      assertThat(cause, instanceOf(CustomException.class));
      CustomException customException = (CustomException) cause;
      assertEquals(String.valueOf(statusCode), customException.getMessage());
      assertEquals(uuid, customException.tag);
    });
  }

  @Test
  public void testExpectFunctionThrowsException_2() throws Exception {
    Expectation<HttpResponseHead> expectation = value -> {
      throw new IndexOutOfBoundsException("boom");
    };

    testExpectation_2(true, expectation, HttpServerResponse::end, ar -> {
      assertThat(ar.cause(), instanceOf(IndexOutOfBoundsException.class));
    });
  }

  @Test
  public void testErrorConverterThrowsException_2() throws Exception {
    Expectation<HttpResponseHead> expectation = ((Expectation<HttpResponseHead>) value -> false).wrappingFailure((head, err) -> {
      throw new IndexOutOfBoundsException();
    });

    testExpectation_2(true, expectation, HttpServerResponse::end, ar -> {
      assertThat(ar.cause(), instanceOf(IndexOutOfBoundsException.class));
    });
  }

  @Test
  public void testErrorConverterReturnsNull_2() throws Exception {
    Expectation<HttpResponseHead> expectation = ((Expectation<HttpResponseHead>) value -> false)
      .wrappingFailure((head, err) -> null);

    testExpectation_2(true, expectation, HttpServerResponse::end, ar -> {
      assertThat(ar.cause(), not(instanceOf(NullPointerException.class)));
    });
  }

  private void testExpectation_2(boolean shouldFail,
                                 Expectation<HttpResponseHead> expectation,
                                 Consumer<HttpServerResponse> bilto) throws Exception {
    testExpectation_2(shouldFail, expectation, bilto, null);
  }

  private void testExpectation_2(boolean shouldFail,
                                 Expectation<HttpResponseHead> expectation,
                                 Consumer<HttpServerResponse> bilto,
                                 Consumer<AsyncResult<?>> resultTest) throws Exception {
    server.requestHandler(request -> bilto.accept(request.response()));
    startServer();
    HttpRequest<Buffer> request = webClient
      .get("/test");
    request.send().expecting(expectation).onComplete(ar -> {
      if (ar.succeeded()) {
        assertFalse("Expected response success", shouldFail);
      } else {
        assertTrue("Expected response failure", shouldFail);
      }
      if (resultTest != null) resultTest.accept(ar);
      testComplete();
    });
    await();
  }

  private static class CustomException extends Exception {

    UUID tag;

    CustomException(String message) {
      super(message);
    }

    CustomException(UUID tag, String message) {
      super(message);
      this.tag = tag;
    }
  }

  @Test
  public void testDontModifyRequestURIQueryParams() throws Exception {
    testRequest(
      client -> client.get("/remote-server?jREJBBB5x2AaiSSDO0/OskoCztDZBAAAAAADV1A4"),
      req -> assertEquals("/remote-server?jREJBBB5x2AaiSSDO0/OskoCztDZBAAAAAADV1A4", req.uri())
    );
  }

  @Test
  public void testRawMethod() throws Exception {
    testRequest(
      client -> client.request(HttpMethod.valueOf("MY_METHOD"), "/"),
      req -> {
        assertEquals(HttpMethod.valueOf("MY_METHOD"), req.method());
        assertEquals("MY_METHOD", req.method().name());
    });
  }

  @Test
  public void testHeaderOverwrite() throws Exception {
    testRequest(
      client -> client
        .get("somepath")
        .putHeader("bla", "1")
        .putHeader("bla", "2"),
      req -> assertEquals(Collections.singletonList("2"), req.headers().getAll("bla")));
  }

  @Test
  public void testMultipleHeaders() throws Exception {
    testRequest(
      client -> client
        .get("somepath")
        .putHeader((CharSequence) "bla", Arrays.asList("1", "2")),
      req -> assertEquals(Arrays.asList("1", "2"), req.headers().getAll("bla")));
  }

  private abstract static class WriteStreamBase implements WriteStream<Buffer> {

    protected Handler<Throwable> exceptionHandler;
    protected Handler<Void> drainHandler;

    @Override
    public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
      exceptionHandler = handler;
      return this;
    }

    @Override
    public Future<Void> write(Buffer data) {
      return Future.succeededFuture();
    }

    @Override
    public Future<Void> end() {
      return Future.succeededFuture();
    }

    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
      return this;
    }

    @Override
    public boolean writeQueueFull() {
      return false;
    }

    @Override
    public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
      drainHandler = handler;
      return this;
    }
  }

  @Test
  public void testMalformedURLExceptionNotSwallowed() {
    try {
      webClient.requestAbs(HttpMethod.POST, "blah://foo@bar");
    } catch (VertxException e) {
      assertThat(e.getCause(), instanceOf(MalformedURLException.class));
    }
  }

  @Test
  public void testQueryParamSpecialChars() throws Exception {
    server.requestHandler(req -> {
      assertEquals("c%3Ad=e", req.query());
      req.response().end();
    });
    startServer();
    HttpRequest<Buffer> req = webClient.get("/test").addQueryParam("c:d", "e");
    req.send().onComplete(onSuccess(resp -> testComplete()));
    await();
  }

  @Test
  public void testQueryParamSpecialCharsDirect() throws Exception {
    server.requestHandler(req -> {
      assertEquals("c:d=e", req.query());
      req.response().end();
    });
    startServer();
    HttpRequest<Buffer> req = webClient.get("/test/?c:d=e");
    req.send().onComplete(onSuccess(resp -> testComplete()));
    await();
  }

  @Test
  public void testCannotResolveAddress() throws Exception {
    awaitFuture(client.close());
    FakeEndpointResolver<?> resolver = new FakeEndpointResolver<>();
    client = vertx.httpClientBuilder().with(createBaseClientOptions()).withAddressResolver(resolver).build();
    webClient = WebClient.wrap(client);

    // This test verifies the address resolver is actually used by the WebClient
    webClient.request(HttpMethod.GET, new RequestOptions().setServer(new FakeAddress("mars")))
      .send()
      .onComplete(onFailure(t -> assertThat(t.getMessage(), allOf(containsString("resolve"), containsString("mars")))));
  }

  @Test
  public void testUseResolvedAddress() throws Exception {
    awaitFuture(client.close());
    FakeEndpointResolver<?> resolver = new FakeEndpointResolver<>();
    resolver.registerAddress("mars", Collections.singletonList(testAddress));
    client = vertx.httpClientBuilder().with(createBaseClientOptions()).withAddressResolver(resolver).build();
    webClient = WebClient.wrap(client);

    testRequest(
      client -> client.request(HttpMethod.GET, new RequestOptions().setServer(new FakeAddress("mars"))),
      req -> assertEquals("localhost:8080", req.authority().toString()));
  }

  @Test
  public void testUpdateSSLOptionsProxiesInternalClient() {
    InterceptingHttpClass proxy = new InterceptingHttpClass();
    WebClient webClient = WebClient.wrap(proxy);
    webClient.updateSSLOptions(new ClientSSLOptions(), false);
    assertEquals(1, proxy.proxiedCallsToUpdateSSLOptions.get());
    webClient.updateSSLOptions(
      new ClientSSLOptions(), true);
    assertEquals(2, proxy.proxiedCallsToUpdateSSLOptions.get());
    webClient.updateSSLOptions(new ClientSSLOptions());
    assertEquals(3, proxy.proxiedCallsToUpdateSSLOptions.get());
  }

  // Intercepts calls to updateSSLOptions and keeps track of the number of calls made to the method.
  static class InterceptingHttpClass implements HttpClientInternal {
    AtomicInteger proxiedCallsToUpdateSSLOptions = new AtomicInteger(0);

    @Override
    public Future<Boolean> updateSSLOptions(ClientSSLOptions options, boolean force) {
      proxiedCallsToUpdateSSLOptions.incrementAndGet();
      return Future.succeededFuture(true);
    }

    @Override public Future<HttpClientConnection> connect(HttpConnectOptions options){return null;}
    @Override public VertxInternal vertx(){return null;}
    @Override public Function<HttpClientResponse, Future<RequestOptions>> redirectHandler(){return null;}
    @Override public HttpClientOptions options(){return new HttpClientOptions();}
    @Override public NetClientInternal netClient(){return null;}
    @Override public Future<Void> closeFuture(){return null;}
    @Override public void close(Completable<Void> completion){}
    @Override public Future<HttpClientRequest> request(RequestOptions options){return null;}
    @Override public Future<Void> shutdown(long timeout, TimeUnit unit){return null;}
    @Override public Metrics getMetrics(){return null;}
  }
}
