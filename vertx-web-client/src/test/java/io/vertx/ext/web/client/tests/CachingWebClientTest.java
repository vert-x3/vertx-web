package io.vertx.ext.web.client.tests;

import io.netty.handler.codec.DateFormatter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.ReportHandlerFailures;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.ext.web.client.*;
import io.vertx.ext.web.client.impl.cache.CacheKey;
import io.vertx.ext.web.client.impl.cache.CachedHttpResponse;
import io.vertx.ext.web.client.spi.CacheStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
@ExtendWith(VertxExtension.class)
@ReportHandlerFailures
public class CachingWebClientTest {

  private static final int PORT = 8778;

  private WebClient defaultClient;
  private WebClient varyClient;
  private WebClient sessionClient;
  private Vertx vertx;
  private HttpServer server;
  private TestCacheStore defaultCacheStore;

  private WebClient buildBaseWebClient() {
    HttpClientOptions opts = new HttpClientOptions().setDefaultPort(PORT).setDefaultHost("localhost");
    return WebClient.wrap(vertx.createHttpClient(opts));
  }

  private HttpServer buildHttpServer() {
    HttpServerOptions opts = new HttpServerOptions().setPort(PORT).setHost("0.0.0.0");
    return vertx.createHttpServer(opts);
  }

  @BeforeEach
  public void setUp(Vertx vertx) {
    this.vertx = vertx;
    WebClient baseClient = buildBaseWebClient();
    defaultCacheStore = new TestCacheStore();
    defaultClient = CachingWebClient.create(baseClient, defaultCacheStore);
    varyClient = CachingWebClient.create(baseClient, new TestCacheStore(), new CachingWebClientOptions(true));
    sessionClient = WebClientSession.create(CachingWebClient.create(baseClient, new TestCacheStore()));
    server = buildHttpServer();
  }

  private void startMockServer(VertxTestContext testContext, Consumer<HttpServerRequest> reqHandler) {
    Checkpoint started = testContext.checkpoint();
    server.requestHandler(req -> {
      try {
        reqHandler.accept(req);
      } finally {
        if (!req.response().ended())
          req.response().end(UUID.randomUUID().toString());
      }
    });
    server.listen().onComplete(testContext.succeeding(s -> started.flag()));
    started.await();
  }

  private void startMockServer(VertxTestContext testContext, String cacheControl) {
    startMockServer(testContext, req -> {
      req.response().headers().set("Cache-Control", cacheControl);
    });
  }

  private String executeRequestBlocking(VertxTestContext testContext, WebClient client, Consumer<HttpRequest<Buffer>> reqConsumer) {
    Checkpoint cp = testContext.checkpoint();
    AtomicReference<String> body = new AtomicReference<>();
    HttpRequest<Buffer> request = client.get("localhost", "/");

    reqConsumer.accept(request);

    request.send().onComplete(testContext.succeeding(response -> {
      body.set(response.bodyAsString());
      cp.flag();
    }));
    cp.await();

    assertNotNull(body.get());

    return body.get();
  }

  private String executeGetBlocking(VertxTestContext testContext, Consumer<HttpRequest<Buffer>> reqConsumer) {
    return executeRequestBlocking(testContext, defaultClient, reqConsumer);
  }

  private String executeGetBlocking(VertxTestContext testContext) {
    return executeRequestBlocking(testContext, defaultClient, req -> {});
  }

  private String executeGetBlocking(VertxTestContext testContext, String uri) {
    return executeGetBlocking(testContext, req -> req.uri(uri));
  }

  private String executeGetBlocking(VertxTestContext testContext, WebClient client) {
    return executeRequestBlocking(testContext, client, req -> {});
  }

  private String executeGetBlocking(VertxTestContext testContext, WebClient client, Consumer<HttpRequest<Buffer>> reqConsumer) {
    return executeRequestBlocking(testContext, client, reqConsumer);
  }

  private void assertCacheUse(VertxTestContext testContext, HttpMethod method, WebClient client, boolean shouldCacheBeUsed) {
    Checkpoint cp1 = testContext.checkpoint();
    Checkpoint cp2 = testContext.checkpoint();
    List<HttpResponse<Buffer>> responses = new ArrayList<>(2);

    client.request(method, "localhost", "/").send().onComplete(testContext.succeeding(resp -> {
      responses.add(resp);
      cp1.flag();
    }));

    // Wait for request 1 to finish first to make sure the cache stored a value if necessary
    cp1.await();

    client.request(method, "localhost", "/").send().onComplete(testContext.succeeding(resp -> {
      responses.add(resp);
      cp2.flag();
    }));

    cp2.await();

    assertTrue(responses.size() == 2);

    HttpResponse<Buffer> resp1 = responses.get(0);
    HttpResponse<Buffer> resp2 = responses.get(1);

    if (shouldCacheBeUsed) {
      assertEquals(resp1.bodyAsString(), resp2.bodyAsString());
      assertNotNull(resp1.headers().get(HttpHeaders.AGE));
      assertNotNull(resp2.headers().get(HttpHeaders.AGE));
    } else {
      assertNotEquals(resp1.bodyAsString(), resp2.bodyAsString());
      assertNull(resp1.headers().get(HttpHeaders.AGE));
      assertNull(resp2.headers().get(HttpHeaders.AGE));
    }
  }

  private void assertCached(VertxTestContext testContext, WebClient client) {
    assertCacheUse(testContext, HttpMethod.GET, client, true);
  }

  private void assertCached(VertxTestContext testContext) {
    assertCached(testContext, defaultClient);
  }

  private void assertNotCached(VertxTestContext testContext, WebClient client) {
    assertCacheUse(testContext, HttpMethod.GET, client, false);
  }

  private void assertNotCached(VertxTestContext testContext) {
    assertNotCached(testContext, defaultClient);
  }

  // Non-GET methods that we shouldn't cache

  @Test
  public void testPOSTNotCached(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=600");
    assertCacheUse(testContext, HttpMethod.POST, defaultClient, false);
    testContext.completeNow();
  }

  @Test
  public void testPUTNotCached(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=600");
    assertCacheUse(testContext, HttpMethod.PUT, defaultClient, false);
    testContext.completeNow();
  }

  @Test
  public void testPATCHNotCached(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=600");
    assertCacheUse(testContext, HttpMethod.PATCH, defaultClient, false);
    testContext.completeNow();
  }

  @Test
  public void testDELETENotCached(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=600");
    assertCacheUse(testContext, HttpMethod.DELETE, defaultClient, false);
    testContext.completeNow();
  }

  // Cache-Control: no-store || no-cache

  @Test
  public void testNoStore(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "no-store");
    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testNoCache(VertxTestContext testContext) throws Exception {
    final AtomicBoolean replyWith304 = new AtomicBoolean(false);

    startMockServer(testContext, req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "no-cache");

      if (replyWith304.get()) {
        req.response().setStatusCode(304);
        req.response().end();
      } else {
        req.response().end(UUID.randomUUID().toString());
      }
    });

    String body1 = executeGetBlocking(testContext); // Initial request
    String body2 = executeGetBlocking(testContext); // Another request, reply with new value
    replyWith304.compareAndSet(false, true);
    String body3 = executeGetBlocking(testContext); // Another request, server says cache is valid
    replyWith304.compareAndSet(true, false);
    String body4 = executeGetBlocking(testContext); // Another request, reply with new value

    assertNotEquals(body1, body2);
    assertNotEquals(body1, body3);
    assertNotEquals(body1, body4);
    assertEquals(body2, body3);
    assertNotEquals(body2, body4);
    assertNotEquals(body3, body4);
    testContext.completeNow();
  }

  // Cache-Control: public

  @Test
  public void testPublicWithMaxAge(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=600");
    assertCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithMaxAgeMultiHeader(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public");
      req.response().headers().add("Cache-Control", "max-age=600");
    });

    assertCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithoutMaxAge(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public");
    assertCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicMaxAgeZero(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public,max-age=0");
    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicSharedMaxAge(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, s-maxage=600");
    assertCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicSharedMaxAgeZero(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, s-maxage=0");
    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithExpiresNow(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", DateFormatter.format(new Date()));
    });

    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithExpiresPast(VertxTestContext testContext) throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(testContext, req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithExpiresFuture(VertxTestContext testContext) throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(testContext, req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", expires);
    });

    assertCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithMaxAgeFutureAndExpiresPast(VertxTestContext testContext) throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(testContext, req -> {
      req.response().headers().set("Cache-Control", "public, max-age=300");
      req.response().headers().set("Expires", expires);
    });

    assertCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithMaxAgeFutureAndExpiresFuture(VertxTestContext testContext) throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));

    Checkpoint latch1 = testContext.checkpoint();
    Checkpoint latch2 = testContext.checkpoint();
    Checkpoint latch3 = testContext.checkpoint();
    Checkpoint waiter = testContext.checkpoint();
    AtomicReference<String> body1 = new AtomicReference<>();
    AtomicReference<String> body2 = new AtomicReference<>();
    AtomicReference<String> body3 = new AtomicReference<>();
    AtomicBoolean req1Completed = new AtomicBoolean(false);

    startMockServer(testContext, req -> {
      String maxAge = req1Completed.get() ? "0" : "1";
      req.response().headers().set("Cache-Control", "public, max-age=" + maxAge);
      req.response().headers().set("Expires", expires);
    });

    defaultClient.get("localhost", "/").send().onComplete(testContext.succeeding(resp -> {
      body1.set(resp.bodyAsString());
      req1Completed.set(true);
      latch1.flag();
    }));
    latch1.await();

    defaultClient.get("localhost", "/").send().onComplete(testContext.succeeding(resp -> {
      body2.set(resp.bodyAsString());
      latch2.flag();
    }));
    latch2.await();

    // HTTP cache only has 1 second resolution, so this must be 1+ seconds past than the max-age
    vertx.setTimer(2000, l -> waiter.flag());
    waiter.await();

    defaultClient.get("localhost", "/").send().onComplete(testContext.succeeding(resp -> {
      body3.set(resp.bodyAsString());
      latch3.flag();
    }));
    latch3.await();

    assertNotNull(body1.get());
    assertNotNull(body2.get());
    assertNotNull(body3.get());
    assertEquals(body1.get(), body2.get());
    assertNotEquals(body1.get(), body3.get());
    testContext.completeNow();
  }

  @Test
  public void testPublicWithMaxAgeZeroAndExpiresFuture(VertxTestContext testContext) throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(testContext, req -> {
      req.response().headers().set("Cache-Control", "public, max-age=0");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicWithMaxAgeZeroAndExpiresZero(VertxTestContext testContext) throws Exception {
    String expires = DateFormatter.format(new Date());
    startMockServer(testContext, req -> {
      req.response().headers().set("Cache-Control", "public, max-age=0");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPublicAndPrivate(VertxTestContext testContext) throws Exception {
    // This is a silly case because it is invalid, but it validates that we err on the side of not
    // caching responses.
    startMockServer(testContext, "public, private, max-age=300");
    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testUpdateStaleResponse(VertxTestContext testContext) throws Exception {
    Checkpoint waiter = testContext.checkpoint();

    startMockServer(testContext, "public, max-age=1");

    String body1 = executeGetBlocking(testContext);

    vertx.setTimer(2000, l -> waiter.flag());
    waiter.await();

    String body2 = executeGetBlocking(testContext);
    String body3 = executeGetBlocking(testContext);

    assertNotEquals(body1, body2);
    assertEquals(body2, body3);
    testContext.completeNow();
  }

  @Test
  public void testCacheHitWontAllocateRequest(VertxTestContext testContext) throws Exception {

    Checkpoint listenLatch = testContext.checkpoint();
    Checkpoint busyLatch = testContext.checkpoint(5);
    server.requestHandler(req -> {
      switch (req.path()) {
        case "/cached":
          req.response().putHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=1").end(UUID.randomUUID().toString());
          break;
        case "/blocked":
          busyLatch.flag();
          break;
      }
    });
    server.listen().onComplete(testContext.succeeding(s -> listenLatch.flag()));
    listenLatch.await();

    String expected = executeGetBlocking(testContext, "/cached");
    for (int i = 0; i < PoolOptions.DEFAULT_MAX_POOL_SIZE; i++) {
      HttpRequest<Buffer> request = defaultClient.get("localhost", "/blocked");
      request.send();
    }

    busyLatch.await();
    assertEquals(executeGetBlocking(testContext, "/cached"), expected);
    testContext.completeNow();
  }

  @Test
  public void test304NotModifiedResponse(VertxTestContext testContext) throws Exception {
    AtomicBoolean primerDone = new AtomicBoolean();
    Checkpoint primer = testContext.checkpoint();
    Checkpoint waiter = testContext.checkpoint();

    startMockServer(testContext, req -> {
      HttpServerResponse resp = req.response();
      resp.headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1");
      if (primerDone.get()) {
        assertEquals("etag_value", req.headers().get("if-none-match"));
        resp.setStatusCode(304);
        resp.end();
      } else {
        resp
          .setStatusCode(200)
          .putHeader("etag", "etag_value")
          .end(UUID.randomUUID().toString())
          .onComplete(v -> { primerDone.set(true); primer.flag(); });
      }
    });

    String body1 = executeGetBlocking(testContext);
    primer.await();

    vertx.setTimer(2000L, l -> waiter.flag());
    waiter.await();

    String body2 = executeGetBlocking(testContext);

    assertEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testStaleWhileRevalidate(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=1, stale-while-revalidate=2");

    String body1 = executeGetBlocking(testContext);

    String key = defaultCacheStore.db.keySet().iterator().next();
    assertEquals(defaultCacheStore.db.get(key).getBody().toString(), body1);

    // Wait > max-age but < stale-while-revalidate
    Thread.sleep(2000);

    String body2 = executeGetBlocking(testContext);

    // Wait > max-age + stale-while-revalidate but account for already waited
    Thread.sleep(2000);
    assertNotEquals(defaultCacheStore.db.get(key).getBody().toString(), body1);
    testContext.completeNow();
  }

  @Test
  public void testStaleWhileRevalidateExpired(VertxTestContext testContext) throws Exception {
    Checkpoint waiter1 = testContext.checkpoint();
    Checkpoint waiter2 = testContext.checkpoint();

    startMockServer(testContext, "public, max-age=1, stale-while-revalidate=1");

    String body1 = executeGetBlocking(testContext);

    // max-age 1 + stale-while-revalidate 1 + leeway 1 => 3s
    vertx.setTimer(3000, l -> waiter1.flag());
    waiter1.await();

    String body2 = executeGetBlocking(testContext);

    // max-age 1 + stale-while-revalidate 1 + leeway 1 => 3s
    vertx.setTimer(3000, l -> waiter2.flag());
    waiter2.await();

    String body3 = executeGetBlocking(testContext);

    assertNotEquals(body1, body2);
    assertNotEquals(body1, body3);
    assertNotEquals(body2, body3);
    testContext.completeNow();
  }

  @Test
  public void testStaleIfError(VertxTestContext testContext) throws Exception {
    AtomicBoolean waiterDone = new AtomicBoolean();
    Checkpoint waiter = testContext.checkpoint();

    startMockServer(testContext, req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1, stale-if-error=2");
      if (waiterDone.get()) {
        req.response().setStatusCode(503);
        req.response().end();
      }
    });

    String body1 = executeGetBlocking(testContext);
    vertx.setTimer(2000L, l -> { waiterDone.set(true); waiter.flag(); });
    waiter.await();
    String body2 = executeGetBlocking(testContext);

    assertEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testStaleIfErrorExpired(VertxTestContext testContext) throws Exception {
    AtomicBoolean waiter1Done = new AtomicBoolean();
    Checkpoint waiter1 = testContext.checkpoint();
    Checkpoint waiter2 = testContext.checkpoint();
    Checkpoint request = testContext.checkpoint();
    AtomicReference<HttpResponse<Buffer>> response = new AtomicReference<>();

    startMockServer(testContext, req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1, stale-if-error=2");
      if (waiter1Done.get()) {
        req.response().setStatusCode(503);
        req.response().end();
      }
    });

    String body1 = executeGetBlocking(testContext);
    vertx.setTimer(2000L, l -> { waiter1Done.set(true); waiter1.flag(); });
    waiter1.await();

    String body2 = executeGetBlocking(testContext);
    vertx.setTimer(3000L, l -> waiter2.flag());
    waiter2.await();

    defaultClient.get("localhost", "/").send().onComplete(testContext.succeeding(resp -> {
      response.set(resp);
      request.flag();
    }));
    request.await();

    assertEquals(body1, body2);
    assertNull(response.get().bodyAsString());
    assertEquals(response.get().statusCode(), 503);
    testContext.completeNow();
  }

  @Test
  public void testMatchingPaths(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=300");

    String body1 = executeGetBlocking(testContext, "/path/to/resource");
    String body2 = executeGetBlocking(testContext, "/path/to/resource");

    assertEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testDifferentPaths(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=300");

    String body1 = executeGetBlocking(testContext, "/path/to/resource");
    String body2 = executeGetBlocking(testContext, "/other/path");

    assertNotEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testWithMatchingQueryParams(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=300");

    String body1 = executeGetBlocking(testContext, req -> {
      req.setQueryParam("q", "search");
    });

    String body2 = executeGetBlocking(testContext, req -> {
      req.setQueryParam("q", "search");
    });

    assertEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testWithDifferentQueryParams(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=300");

    String body1 = executeGetBlocking(testContext, req -> {
      req.setQueryParam("q", "search");
    });

    String body2 = executeGetBlocking(testContext, req -> {
      req.setQueryParam("q", "other");
    });

    assertNotEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testWithDifferentQueryParamOrdering(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "public, max-age=300");

    String body1 = executeGetBlocking(testContext, req -> {
      req
        .setQueryParam("q", "search")
        .setQueryParam("param", "value");
    });

    String body2 = executeGetBlocking(testContext, req -> {
      req
        .setQueryParam("param", "value")
        .setQueryParam("q", "search");
    });

    assertEquals(body1, body2);
    testContext.completeNow();
  }

  // Cache-Control: private with client NOT enabled private caching

  @Test
  public void testPrivate(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "private");
    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPrivateMaxAge(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "private, max-age=300");
    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPrivateMaxAgeZero(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "private, max-age=0");
    assertNotCached(testContext);
    testContext.completeNow();
  }

  @Test
  public void testPrivateExpires(VertxTestContext testContext) throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "private");
      req.response().headers().add("Expires", expires);
    });

    assertNotCached(testContext);
    testContext.completeNow();
  }

  // Cache-Control: private with client enabled private caching

  @Test
  public void testPrivateEnabled(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "private");
    assertCached(testContext, sessionClient);
    testContext.completeNow();
  }

  @Test
  public void testPrivateEnabledMaxAge(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "private, max-age=300");
    assertCached(testContext, sessionClient);
    testContext.completeNow();
  }

  @Test
  public void testPrivateEnabledMaxAgeZero(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "private, max-age=0");
    assertNotCached(testContext, sessionClient);
    testContext.completeNow();
  }

  @Test
  public void testPrivateSharedMaxAgeAndMaxAgeZero(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, "private, s-maxage=300, max-age=0");
    assertNotCached(testContext, sessionClient);
    testContext.completeNow();
  }

  @Test
  public void testPrivateSharedMaxAgeAndMaxAge(VertxTestContext testContext) throws Exception {
    Checkpoint waiter = testContext.checkpoint();

    startMockServer(testContext, "private, s-maxage=300, max-age=1");

    String body1 = executeGetBlocking(testContext, sessionClient);
    String body2 = executeGetBlocking(testContext, sessionClient);

    // Wait for the max-age time to pass, but not long enough for s-maxage
    // HTTP cache only has 1 second resolution, so this must be 1+ seconds past than the max-age
    vertx.setTimer(2000, l -> waiter.flag());
    waiter.await();

    String body3 = executeGetBlocking(testContext, sessionClient);

    assertEquals(body1, body2);
    assertNotEquals(body2, body3);
    testContext.completeNow();
  }

  // Cache-Control: public; Vary: User-Agent

  @Test
  public void testPublicVaryMaxAgeZero(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=0");
      req.response().headers().add("Vary", "User-Agent");
    });

    assertNotCached(testContext, varyClient);
    testContext.completeNow();
  }

  @Test
  public void testVaryUserAgentTwoDesktops(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent");
    });

    // Chrome Desktop
    String body1 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // Firefox Desktop
    String body2 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0");
    });

    // Desktop user agents are normalized so two desktop clients should hit the same cache
    assertEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testVaryUserAgentDesktopVsMobile(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent");
    });

    // Chrome Desktop
    String body1 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // iPhone Mobile
    String body2 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    // Desktop and Mobile may receive different content and should not share a cache
    assertNotEquals(body1, body2);
    testContext.completeNow();
  }

  // Cache-Control: public; Vary: Content-Encoding

  @Test
  public void testVaryEncodingTransformedToIdentityAlwaysSoWeIgnoreIt(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Content-Encoding", "gzip");
      req.response().headers().add("Vary", "Accept-Encoding");
    });

    String body1 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("Accept-Encoding", "gzip,deflate");
    });

    String body2 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("Accept-Encoding", "br");
    });

    assertEquals(body1, body2);
    testContext.completeNow();
  }

  @Test
  public void testVaryCustomHeader(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "X-Custom-Header");
    });

    String body1 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("X-Custom-Header", "0x00000000");
    });

    String body2 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("X-Custom-Header", "0xDEADBEEF");
    });

    String body3 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("X-Custom-Header", "0x00000000");
    });

    assertNotEquals(body1, body2);
    assertNotEquals(body2, body3);
    assertEquals(body1, body3);
    testContext.completeNow();
  }

  @Test
  public void testVaryUserAgentAndCustomHeader(VertxTestContext testContext) throws Exception {
    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent, X-Custom-Header");
    });

    // 1. Chrome desktop, custom header 0
    String body1 = executeGetBlocking(testContext, varyClient, req -> {
      req
        .putHeader("X-Custom-Header", "0x00000000")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 2. Chrome desktop, custom header deadbeef, should not be cached
    String body2 = executeGetBlocking(testContext, varyClient, req -> {
      req
        .putHeader("X-Custom-Header", "0xDEADBEEF")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 3. Chrome desktop, custom header 0, should be cached from req1
    String body3 = executeGetBlocking(testContext, varyClient, req -> {
      req
        .putHeader("X-Custom-Header", "0x00000000")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 4. iPhone mobile, custom header 0, should not be cached
    String body4 = executeGetBlocking(testContext, varyClient, req -> {
      req
        .putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1")
        .putHeader("X-Custom-Header", "0x00000000");
    });

    assertNotEquals(body1, body2);
    assertEquals(body1, body3);
    assertNotEquals(body1, body4);
    assertNotEquals(body2, body3);
    assertNotEquals(body2, body4);
    assertNotEquals(body3, body4);
    testContext.completeNow();
  }

  @Test
  public void testVaryWithStaleResponse(VertxTestContext testContext) throws Exception {
    Checkpoint waiter = testContext.checkpoint();

    startMockServer(testContext, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=2");
      req.response().headers().add("Vary", "User-Agent");
    });

    String body1 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    String body2 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    vertx.setTimer(3000, l -> waiter.flag());
    waiter.await();

    String body3 = executeGetBlocking(testContext, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    assertEquals(body1, body2);
    assertNotEquals(body1, body3);
    assertNotEquals(body2, body3);
    testContext.completeNow();
  }

  static class TestCacheStore implements CacheStore {
    public final Map<String, CachedHttpResponse> db = new ConcurrentHashMap<>();

    @Override
    public Future<CachedHttpResponse> get(CacheKey key) {
      CachedHttpResponse res = db.get(key.toString());
      return Future.succeededFuture(res);
    }

    @Override
    public Future<CachedHttpResponse> set(CacheKey key, CachedHttpResponse response) {
      db.put(key.toString(), response);
      return Future.succeededFuture(response);
    }

    @Override
    public Future<Void> delete(CacheKey key) {
      db.remove(key.toString());
      return Future.succeededFuture();
    }

    @Override
    public Future<Void> flush() {
      db.clear();
      return Future.succeededFuture();
    }
  }
}
