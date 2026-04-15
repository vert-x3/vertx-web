package io.vertx.ext.web.client.tests;

import io.netty.handler.codec.DateFormatter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.junit5.VertxTest;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
@VertxTest
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

  private void startMockServer(Consumer<HttpServerRequest> reqHandler) {
    server.requestHandler(req -> {
      try {
        reqHandler.accept(req);
      } finally {
        if (!req.response().ended())
          req.response().end(UUID.randomUUID().toString());
      }
    });
    server
      .listen()
      .await();
  }

  private void startMockServer(String cacheControl) {
    startMockServer(req -> {
      req.response().headers().set("Cache-Control", cacheControl);
    });
  }

  private String executeRequestBlocking(WebClient client, Consumer<HttpRequest<Buffer>> reqConsumer) {
    HttpRequest<Buffer> request = client.get("localhost", "/");

    reqConsumer.accept(request);

    String body = request
      .send()
      .await()
      .bodyAsString();

    assertNotNull(body);

    return body;
  }

  private String executeGetBlocking(Consumer<HttpRequest<Buffer>> reqConsumer) {
    return executeRequestBlocking(defaultClient, reqConsumer);
  }

  private String executeGetBlocking() {
    return executeRequestBlocking(defaultClient, req -> {});
  }

  private String executeGetBlocking(String uri) {
    return executeGetBlocking(req -> req.uri(uri));
  }

  private String executeGetBlocking(WebClient client) {
    return executeRequestBlocking(client, req -> {});
  }

  private String executeGetBlocking(WebClient client, Consumer<HttpRequest<Buffer>> reqConsumer) {
    return executeRequestBlocking(client, reqConsumer);
  }

  private void assertCacheUse(HttpMethod method, WebClient client, boolean shouldCacheBeUsed) {
    List<HttpResponse<Buffer>> responses = new ArrayList<>(2);

    // Wait for request 1 to finish first to make sure the cache stored a value if necessary
    responses.add(client.request(method, "localhost", "/").send().await());
    responses.add(client.request(method, "localhost", "/").send().await());

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

  private void assertCached(WebClient client) {
    assertCacheUse(HttpMethod.GET, client, true);
  }

  private void assertCached() {
    assertCached(defaultClient);
  }

  private void assertNotCached(WebClient client) {
    assertCacheUse(HttpMethod.GET, client, false);
  }

  private void assertNotCached() {
    assertNotCached(defaultClient);
  }

  // Non-GET methods that we shouldn't cache

  @Test
  public void testPOSTNotCached() throws Exception {
    startMockServer("public, max-age=600");
    assertCacheUse(HttpMethod.POST, defaultClient, false);

  }

  @Test
  public void testPUTNotCached() throws Exception {
    startMockServer("public, max-age=600");
    assertCacheUse(HttpMethod.PUT, defaultClient, false);

  }

  @Test
  public void testPATCHNotCached() throws Exception {
    startMockServer("public, max-age=600");
    assertCacheUse(HttpMethod.PATCH, defaultClient, false);

  }

  @Test
  public void testDELETENotCached() throws Exception {
    startMockServer("public, max-age=600");
    assertCacheUse(HttpMethod.DELETE, defaultClient, false);

  }

  // Cache-Control: no-store || no-cache

  @Test
  public void testNoStore() throws Exception {
    startMockServer("no-store");
    assertNotCached();

  }

  @Test
  public void testNoCache() throws Exception {
    final AtomicBoolean replyWith304 = new AtomicBoolean(false);

    startMockServer(req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "no-cache");

      if (replyWith304.get()) {
        req.response().setStatusCode(304);
        req.response().end();
      } else {
        req.response().end(UUID.randomUUID().toString());
      }
    });

    String body1 = executeGetBlocking(); // Initial request
    String body2 = executeGetBlocking(); // Another request, reply with new value
    replyWith304.compareAndSet(false, true);
    String body3 = executeGetBlocking(); // Another request, server says cache is valid
    replyWith304.compareAndSet(true, false);
    String body4 = executeGetBlocking(); // Another request, reply with new value

    assertNotEquals(body1, body2);
    assertNotEquals(body1, body3);
    assertNotEquals(body1, body4);
    assertEquals(body2, body3);
    assertNotEquals(body2, body4);
    assertNotEquals(body3, body4);

  }

  // Cache-Control: public

  @Test
  public void testPublicWithMaxAge() throws Exception {
    startMockServer("public, max-age=600");
    assertCached();

  }

  @Test
  public void testPublicWithMaxAgeMultiHeader() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public");
      req.response().headers().add("Cache-Control", "max-age=600");
    });

    assertCached();

  }

  @Test
  public void testPublicWithoutMaxAge() throws Exception {
    startMockServer("public");
    assertCached();

  }

  @Test
  public void testPublicMaxAgeZero() throws Exception {
    startMockServer("public,max-age=0");
    assertNotCached();

  }

  @Test
  public void testPublicSharedMaxAge() throws Exception {
    startMockServer("public, s-maxage=600");
    assertCached();

  }

  @Test
  public void testPublicSharedMaxAgeZero() throws Exception {
    startMockServer("public, s-maxage=0");
    assertNotCached();

  }

  @Test
  public void testPublicWithExpiresNow() throws Exception {
    startMockServer(req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", DateFormatter.format(new Date()));
    });

    assertNotCached();

  }

  @Test
  public void testPublicWithExpiresPast() throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached();

  }

  @Test
  public void testPublicWithExpiresFuture() throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", expires);
    });

    assertCached();

  }

  @Test
  public void testPublicWithMaxAgeFutureAndExpiresPast() throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(req -> {
      req.response().headers().set("Cache-Control", "public, max-age=300");
      req.response().headers().set("Expires", expires);
    });

    assertCached();

  }

  @Test
  public void testPublicWithMaxAgeFutureAndExpiresFuture() throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));

    AtomicBoolean req1Completed = new AtomicBoolean(false);

    startMockServer(req -> {
      String maxAge = req1Completed.get() ? "0" : "1";
      req.response().headers().set("Cache-Control", "public, max-age=" + maxAge);
      req.response().headers().set("Expires", expires);
    });

    String body1 = defaultClient
      .get("localhost", "/")
      .send()
      .await()
      .bodyAsString();

    String body2 = defaultClient
      .get("localhost", "/")
      .send()
      .await().bodyAsString();

    // HTTP cache only has 1 second resolution, so this must be 1+ seconds past than the max-age
    Thread.sleep(2000);

    String body3 = defaultClient
      .get("localhost", "/")
      .send()
      .await()
      .bodyAsString();

    assertNotNull(body1);
    assertNotNull(body2);
    assertNotNull(body3);
    assertEquals(body1, body2);
    assertNotEquals(body1, body3);

  }

  @Test
  public void testPublicWithMaxAgeZeroAndExpiresFuture() throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(req -> {
      req.response().headers().set("Cache-Control", "public, max-age=0");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached();

  }

  @Test
  public void testPublicWithMaxAgeZeroAndExpiresZero() throws Exception {
    String expires = DateFormatter.format(new Date());
    startMockServer(req -> {
      req.response().headers().set("Cache-Control", "public, max-age=0");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached();

  }

  @Test
  public void testPublicAndPrivate() throws Exception {
    // This is a silly case because it is invalid, but it validates that we err on the side of not
    // caching responses.
    startMockServer("public, private, max-age=300");
    assertNotCached();

  }

  @Test
  public void testUpdateStaleResponse() throws Exception {
    startMockServer("public, max-age=1");

    String body1 = executeGetBlocking();

    Thread.sleep(2000);

    String body2 = executeGetBlocking();
    String body3 = executeGetBlocking();

    assertNotEquals(body1, body2);
    assertEquals(body2, body3);

  }

  @Test
  public void testCacheHitWontAllocateRequest() throws Exception {

    CountDownLatch busyLatch = new CountDownLatch(5);
    server.requestHandler(req -> {
      switch (req.path()) {
        case "/cached":
          req.response().putHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=1").end(UUID.randomUUID().toString());
          break;
        case "/blocked":
          busyLatch.countDown();
          break;
      }
    });
    server.listen().await();

    String expected = executeGetBlocking("/cached");
    for (int i = 0; i < PoolOptions.DEFAULT_MAX_POOL_SIZE; i++) {
      HttpRequest<Buffer> request = defaultClient.get("localhost", "/blocked");
      request.send();
    }

    busyLatch.await();
    assertEquals(executeGetBlocking("/cached"), expected);

  }

  @Test
  public void test304NotModifiedResponse() throws Exception {
    AtomicBoolean primerDone = new AtomicBoolean();

    startMockServer(req -> {
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
          .onComplete(v -> primerDone.set(true));
      }
    });

    String body1 = executeGetBlocking();
    Thread.sleep(100);

    Thread.sleep(2000);

    String body2 = executeGetBlocking();

    assertEquals(body1, body2);

  }

  @Test
  public void testStaleWhileRevalidate() throws Exception {
    startMockServer("public, max-age=1, stale-while-revalidate=2");

    String body1 = executeGetBlocking();

    String key = defaultCacheStore.db.keySet().iterator().next();
    assertEquals(defaultCacheStore.db.get(key).getBody().toString(), body1);

    // Wait > max-age but < stale-while-revalidate
    Thread.sleep(2000);

    String body2 = executeGetBlocking();

    // Wait > max-age + stale-while-revalidate but account for already waited
    Thread.sleep(2000);
    assertNotEquals(defaultCacheStore.db.get(key).getBody().toString(), body1);

  }

  @Test
  public void testStaleWhileRevalidateExpired() throws Exception {
    startMockServer("public, max-age=1, stale-while-revalidate=1");

    String body1 = executeGetBlocking();

    // max-age 1 + stale-while-revalidate 1 + leeway 1 => 3s
    Thread.sleep(3000);

    String body2 = executeGetBlocking();

    // max-age 1 + stale-while-revalidate 1 + leeway 1 => 3s
    Thread.sleep(3000);

    String body3 = executeGetBlocking();

    assertNotEquals(body1, body2);
    assertNotEquals(body1, body3);
    assertNotEquals(body2, body3);

  }

  @Test
  public void testStaleIfError() throws Exception {
    AtomicBoolean stale = new AtomicBoolean();

    startMockServer(req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1, stale-if-error=2");
      if (stale.get()) {
        req.response().setStatusCode(503);
        req.response().end();
      }
    });

    String body1 = executeGetBlocking();
    Thread.sleep(2000);
    stale.set(true);
    String body2 = executeGetBlocking();

    assertEquals(body1, body2);

  }

  @Test
  public void testStaleIfErrorExpired() throws Exception {
    AtomicBoolean stale = new AtomicBoolean();

    startMockServer(req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1, stale-if-error=2");
      if (stale.get()) {
        req.response().setStatusCode(503);
        req.response().end();
      }
    });

    String body1 = executeGetBlocking();
    Thread.sleep(2000);
    stale.set(true);

    String body2 = executeGetBlocking();
    Thread.sleep(3000);

    HttpResponse<Buffer> response = defaultClient
      .get("localhost", "/")
      .send()
      .await();

    assertEquals(body1, body2);
    assertNull(response.bodyAsString());
    assertEquals(503, response.statusCode());

  }

  @Test
  public void testMatchingPaths() throws Exception {
    startMockServer("public, max-age=300");

    String body1 = executeGetBlocking("/path/to/resource");
    String body2 = executeGetBlocking("/path/to/resource");

    assertEquals(body1, body2);

  }

  @Test
  public void testDifferentPaths() throws Exception {
    startMockServer("public, max-age=300");

    String body1 = executeGetBlocking("/path/to/resource");
    String body2 = executeGetBlocking("/other/path");

    assertNotEquals(body1, body2);

  }

  @Test
  public void testWithMatchingQueryParams() throws Exception {
    startMockServer("public, max-age=300");

    String body1 = executeGetBlocking(req -> {
      req.setQueryParam("q", "search");
    });

    String body2 = executeGetBlocking(req -> {
      req.setQueryParam("q", "search");
    });

    assertEquals(body1, body2);

  }

  @Test
  public void testWithDifferentQueryParams() throws Exception {
    startMockServer("public, max-age=300");

    String body1 = executeGetBlocking(req -> {
      req.setQueryParam("q", "search");
    });

    String body2 = executeGetBlocking(req -> {
      req.setQueryParam("q", "other");
    });

    assertNotEquals(body1, body2);

  }

  @Test
  public void testWithDifferentQueryParamOrdering() throws Exception {
    startMockServer("public, max-age=300");

    String body1 = executeGetBlocking(req -> {
      req
        .setQueryParam("q", "search")
        .setQueryParam("param", "value");
    });

    String body2 = executeGetBlocking(req -> {
      req
        .setQueryParam("param", "value")
        .setQueryParam("q", "search");
    });

    assertEquals(body1, body2);

  }

  // Cache-Control: private with client NOT enabled private caching

  @Test
  public void testPrivate() throws Exception {
    startMockServer("private");
    assertNotCached();

  }

  @Test
  public void testPrivateMaxAge() throws Exception {
    startMockServer("private, max-age=300");
    assertNotCached();

  }

  @Test
  public void testPrivateMaxAgeZero() throws Exception {
    startMockServer("private, max-age=0");
    assertNotCached();

  }

  @Test
  public void testPrivateExpires() throws Exception {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "private");
      req.response().headers().add("Expires", expires);
    });

    assertNotCached();

  }

  // Cache-Control: private with client enabled private caching

  @Test
  public void testPrivateEnabled() throws Exception {
    startMockServer("private");
    assertCached(sessionClient);

  }

  @Test
  public void testPrivateEnabledMaxAge() throws Exception {
    startMockServer("private, max-age=300");
    assertCached(sessionClient);

  }

  @Test
  public void testPrivateEnabledMaxAgeZero() throws Exception {
    startMockServer("private, max-age=0");
    assertNotCached(sessionClient);

  }

  @Test
  public void testPrivateSharedMaxAgeAndMaxAgeZero() throws Exception {
    startMockServer("private, s-maxage=300, max-age=0");
    assertNotCached(sessionClient);

  }

  @Test
  public void testPrivateSharedMaxAgeAndMaxAge() throws Exception {
    startMockServer("private, s-maxage=300, max-age=1");

    String body1 = executeGetBlocking(sessionClient);
    String body2 = executeGetBlocking(sessionClient);

    // Wait for the max-age time to pass, but not long enough for s-maxage
    // HTTP cache only has 1 second resolution, so this must be 1+ seconds past than the max-age
    Thread.sleep(2000);

    String body3 = executeGetBlocking(sessionClient);

    assertEquals(body1, body2);
    assertNotEquals(body2, body3);

  }

  // Cache-Control: public; Vary: User-Agent

  @Test
  public void testPublicVaryMaxAgeZero() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public, max-age=0");
      req.response().headers().add("Vary", "User-Agent");
    });

    assertNotCached(varyClient);

  }

  @Test
  public void testVaryUserAgentTwoDesktops() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent");
    });

    // Chrome Desktop
    String body1 = executeGetBlocking(varyClient,req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // Firefox Desktop
    String body2 = executeGetBlocking(varyClient,req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0");
    });

    // Desktop user agents are normalized so two desktop clients should hit the same cache
    assertEquals(body1, body2);

  }

  @Test
  public void testVaryUserAgentDesktopVsMobile() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent");
    });

    // Chrome Desktop
    String body1 = executeGetBlocking(varyClient,req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // iPhone Mobile
    String body2 = executeGetBlocking(varyClient,req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    // Desktop and Mobile may receive different content and should not share a cache
    assertNotEquals(body1, body2);

  }

  // Cache-Control: public; Vary: Content-Encoding

  @Test
  public void testVaryEncodingTransformedToIdentityAlwaysSoWeIgnoreIt() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Content-Encoding", "gzip");
      req.response().headers().add("Vary", "Accept-Encoding");
    });

    String body1 = executeGetBlocking(varyClient,req -> {
      req.putHeader("Accept-Encoding", "gzip,deflate");
    });

    String body2 = executeGetBlocking(varyClient,req -> {
      req.putHeader("Accept-Encoding", "br");
    });

    assertEquals(body1, body2);

  }

  @Test
  public void testVaryCustomHeader() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "X-Custom-Header");
    });

    String body1 = executeGetBlocking(varyClient,req -> {
      req.putHeader("X-Custom-Header", "0x00000000");
    });

    String body2 = executeGetBlocking(varyClient,req -> {
      req.putHeader("X-Custom-Header", "0xDEADBEEF");
    });

    String body3 = executeGetBlocking(varyClient,req -> {
      req.putHeader("X-Custom-Header", "0x00000000");
    });

    assertNotEquals(body1, body2);
    assertNotEquals(body2, body3);
    assertEquals(body1, body3);

  }

  @Test
  public void testVaryUserAgentAndCustomHeader() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent, X-Custom-Header");
    });

    // 1. Chrome desktop, custom header 0
    String body1 = executeGetBlocking(varyClient,req -> {
      req
        .putHeader("X-Custom-Header", "0x00000000")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 2. Chrome desktop, custom header deadbeef, should not be cached
    String body2 = executeGetBlocking(varyClient,req -> {
      req
        .putHeader("X-Custom-Header", "0xDEADBEEF")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 3. Chrome desktop, custom header 0, should be cached from req1
    String body3 = executeGetBlocking(varyClient,req -> {
      req
        .putHeader("X-Custom-Header", "0x00000000")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 4. iPhone mobile, custom header 0, should not be cached
    String body4 = executeGetBlocking(varyClient,req -> {
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

  }

  @Test
  public void testVaryWithStaleResponse() throws Exception {
    startMockServer(req -> {
      req.response().headers().add("Cache-Control", "public, max-age=2");
      req.response().headers().add("Vary", "User-Agent");
    });

    String body1 = executeGetBlocking(varyClient,req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    String body2 = executeGetBlocking(varyClient,req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    Thread.sleep(3000);

    String body3 = executeGetBlocking(varyClient,req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    assertEquals(body1, body2);
    assertNotEquals(body1, body3);
    assertNotEquals(body2, body3);

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
