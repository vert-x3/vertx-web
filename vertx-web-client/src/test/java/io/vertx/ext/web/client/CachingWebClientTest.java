package io.vertx.ext.web.client;

import io.netty.handler.codec.DateFormatter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
@RunWith(VertxUnitRunner.class)
public class CachingWebClientTest {

  private static final int PORT = 8888;

  private WebClient defaultClient;
  private WebClient privateClient;
  private WebClient varyClient;
  private Vertx vertx;
  private HttpServer server;

  private WebClient buildBaseWebClient() {
    HttpClientOptions opts = new HttpClientOptions().setDefaultPort(PORT).setDefaultHost("localhost");
    return WebClient.wrap(vertx.createHttpClient(opts));
  }

  private HttpServer buildHttpServer() {
    HttpServerOptions opts = new HttpServerOptions().setPort(PORT).setHost("0.0.0.0");
    return vertx.createHttpServer(opts);
  }

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    WebClient base = buildBaseWebClient();
    defaultClient = CachingWebClient.create(base, new TestCacheStore(), new CachingWebClientOptions(true, false, false));
    privateClient = CachingWebClient.create(base, new TestCacheStore(), new CachingWebClientOptions(true, true, false));
    varyClient = CachingWebClient.create(base, new TestCacheStore(), new CachingWebClientOptions(true, false, true));
    server = buildHttpServer();
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  private void startMockServer(TestContext context, Consumer<HttpServerRequest> reqHandler) {
    Async async = context.async();
    server.requestHandler(req -> {
      try {
        reqHandler.accept(req);
      } finally {
        if (!req.response().ended())
          req.response().end(UUID.randomUUID().toString());
      }
    });
    server.listen(context.asyncAssertSuccess(s -> async.complete()));
    async.awaitSuccess(15000);
  }

  private void startMockServer(TestContext context, String cacheControl) {
    startMockServer(context, req -> {
      req.response().headers().set("Cache-Control", cacheControl);
    });
  }

  private String executeRequestBlocking(TestContext context, WebClient client, Consumer<HttpRequest<Buffer>> reqConsumer) {
    Async waiter = context.async();
    AtomicReference<String> body = new AtomicReference<>();
    HttpRequest<Buffer> request = client.get("localhost", "/");

    reqConsumer.accept(request);

    request.send(context.asyncAssertSuccess(response -> {
      body.set(response.bodyAsString());
      waiter.complete();
    }));
    waiter.await();

    context.assertNotNull(body.get());

    return body.get();
  }

  private String executeGetBlocking(TestContext context, Consumer<HttpRequest<Buffer>> reqConsumer) {
    return executeRequestBlocking(context, defaultClient, reqConsumer);
  }

  private String executeGetBlocking(TestContext context) {
    return executeRequestBlocking(context, defaultClient, req -> {});
  }

  private String executeGetBlocking(TestContext context, String uri) {
    return executeGetBlocking(context, req -> req.uri(uri));
  }

  private String executeGetBlocking(TestContext context, WebClient client) {
    return executeRequestBlocking(context, client, req -> {});
  }

  private String executeGetBlocking(TestContext context, WebClient client, Consumer<HttpRequest<Buffer>> reqConsumer) {
    return executeRequestBlocking(context, client, reqConsumer);
  }

  private void assertCacheUse(TestContext context, HttpMethod method, WebClient client, boolean shouldCacheBeUsed) {
    Async request1 = context.async();
    Async request2 = context.async();
    List<HttpResponse<Buffer>> responses = new ArrayList<>(2);

    client.request(method, "localhost", "/").send(context.asyncAssertSuccess(resp -> {
      responses.add(resp);
      request1.complete();
    }));

    // Wait for request 1 to finish first to make sure the cache stored a value if necessary
    request1.await();

    client.request(method, "localhost", "/").send(context.asyncAssertSuccess(resp -> {
      responses.add(resp);
      request2.complete();
    }));

    request2.await();

    context.assertTrue(responses.size() == 2);

    HttpResponse<Buffer> resp1 = responses.get(0);
    HttpResponse<Buffer> resp2 = responses.get(1);

    if (shouldCacheBeUsed) {
      context.assertEquals(resp1.bodyAsString(), resp2.bodyAsString());
      context.assertNotNull(resp1.headers().get(HttpHeaders.AGE));
      context.assertNotNull(resp2.headers().get(HttpHeaders.AGE));
    } else {
      context.assertNotEquals(resp1.bodyAsString(), resp2.bodyAsString());
      context.assertNull(resp1.headers().get(HttpHeaders.AGE));
      context.assertNull(resp2.headers().get(HttpHeaders.AGE));
    }
  }

  private void assertCached(TestContext context, WebClient client) {
    assertCacheUse(context, HttpMethod.GET, client, true);
  }

  private void assertCached(TestContext context) {
    assertCached(context, defaultClient);
  }

  private void assertNotCached(TestContext context, WebClient client) {
    assertCacheUse(context, HttpMethod.GET, client, false);
  }

  private void assertNotCached(TestContext context) {
    assertNotCached(context, defaultClient);
  }

  // Test cache disabled via config

  @Test
  public void testCacheConfigDisable(TestContext context) {
    WebClient client = CachingWebClient.create(defaultClient, new TestCacheStore(),
      new CachingWebClientOptions(false, false, false));

    startMockServer(context, "public, max-age=600");
    assertNotCached(context, client);
  }

  // Non-GET methods that we shouldn't cache

  @Test
  public void testPOSTNotCached(TestContext context) {
    startMockServer(context, "public, max-age=600");
    assertCacheUse(context, HttpMethod.POST, defaultClient, false);
  }

  @Test
  public void testPOSTNotPrivatelyCached(TestContext context) {
    startMockServer(context, "private, max-age=600");
    assertCacheUse(context, HttpMethod.POST, privateClient, false);
  }

  @Test
  public void testPUTNotCached(TestContext context) {
    startMockServer(context, "public, max-age=600");
    assertCacheUse(context, HttpMethod.PUT, defaultClient, false);
  }

  @Test
  public void testPUTNotPrivatelyCached(TestContext context) {
    startMockServer(context, "private, max-age=600");
    assertCacheUse(context, HttpMethod.PUT, privateClient, false);
  }

  @Test
  public void testPATCHNotCached(TestContext context) {
    startMockServer(context, "public, max-age=600");
    assertCacheUse(context, HttpMethod.PATCH, defaultClient, false);
  }

  @Test
  public void testPATCHNotPrivatelyCached(TestContext context) {
    startMockServer(context, "private, max-age=600");
    assertCacheUse(context, HttpMethod.PATCH, privateClient, false);
  }

  @Test
  public void testDELETENotCached(TestContext context) {
    startMockServer(context, "public, max-age=600");
    assertCacheUse(context, HttpMethod.DELETE, defaultClient, false);
  }

  @Test
  public void testDELETENotPrivatelyCached(TestContext context) {
    startMockServer(context, "private, max-age=600");
    assertCacheUse(context, HttpMethod.DELETE, privateClient, false);
  }

  // Cache-Control: no-store || no-cache

  @Test
  public void testNoStore(TestContext context) {
    startMockServer(context, "no-store");
    assertNotCached(context);
  }

  @Test
  public void testNoCache(TestContext context) {
    final AtomicBoolean replyWith304 = new AtomicBoolean(false);

    startMockServer(context, req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "no-cache");

      if (replyWith304.get()) {
        req.response().setStatusCode(304);
        req.response().end();
      } else {
        req.response().end(UUID.randomUUID().toString());
      }
    });

    String body1 = executeGetBlocking(context); // Initial request
    String body2 = executeGetBlocking(context); // Another request, reply with new value
    replyWith304.compareAndSet(false, true);
    String body3 = executeGetBlocking(context); // Another request, server says cache is valid
    replyWith304.compareAndSet(true, false);
    String body4 = executeGetBlocking(context); // Another request, reply with new value

    context.assertNotEquals(body1, body2);
    context.assertNotEquals(body1, body3);
    context.assertNotEquals(body1, body4);
    context.assertEquals(body2, body3);
    context.assertNotEquals(body2, body4);
    context.assertNotEquals(body3, body4);
  }

  // Cache-Control: public

  @Test
  public void testPublicWithMaxAge(TestContext context) {
    startMockServer(context, "public, max-age=600");
    assertCached(context);
  }

  @Test
  public void testPublicWithMaxAgeMultiHeader(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public");
      req.response().headers().add("Cache-Control", "max-age=600");
    });

    assertCached(context);
  }

  @Test
  public void testPublicWithoutMaxAge(TestContext context) {
    startMockServer(context, "public");
    assertCached(context);
  }

  @Test
  public void testPublicMaxAgeZero(TestContext context) {
    startMockServer(context, "public,max-age=0");
    assertNotCached(context);
  }

  @Test
  public void testPublicSharedMaxAge(TestContext context) {
    startMockServer(context, "public, s-maxage=600");
    assertCached(context);
  }

  @Test
  public void testPublicSharedMaxAgeZero(TestContext context) {
    startMockServer(context, "public, s-maxage=0");
    assertNotCached(context);
  }

  @Test
  public void testPublicWithExpiresNow(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", DateFormatter.format(new Date()));
    });

    assertNotCached(context);
  }

  @Test
  public void testPublicWithExpiresPast(TestContext context) {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(context, req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached(context);
  }

  @Test
  public void testPublicWithExpiresFuture(TestContext context) {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(context, req -> {
      req.response().headers().set("Cache-Control", "public");
      req.response().headers().set("Expires", expires);
    });

    assertCached(context);
  }

  @Test
  public void testPublicWithMaxAgeFutureAndExpiresPast(TestContext context) {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(context, req -> {
      req.response().headers().set("Cache-Control", "public, max-age=300");
      req.response().headers().set("Expires", expires);
    });

    assertCached(context);
  }

  @Test
  public void testPublicWithMaxAgeFutureAndExpiresFuture(TestContext context) {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));

    Async req1 = context.async();
    Async req2 = context.async();
    Async req3 = context.async();
    Async waiter = context.async();
    AtomicReference<String> body1 = new AtomicReference<>();
    AtomicReference<String> body2 = new AtomicReference<>();
    AtomicReference<String> body3 = new AtomicReference<>();

    startMockServer(context, req -> {
      String maxAge = req1.isCompleted() ? "0" : "1";
      req.response().headers().set("Cache-Control", "public, max-age=" + maxAge);
      req.response().headers().set("Expires", expires);
    });

    defaultClient.get("localhost", "/").send(context.asyncAssertSuccess(resp -> {
      body1.set(resp.bodyAsString());
      req1.complete();
    }));
    req1.await();

    defaultClient.get("localhost", "/").send(context.asyncAssertSuccess(resp -> {
      body2.set(resp.bodyAsString());
      req2.complete();
    }));
    req2.await();

    // HTTP cache only has 1 second resolution, so this must be 1+ seconds past than the max-age
    vertx.setTimer(2000, l -> waiter.complete());
    waiter.await();

    defaultClient.get("localhost", "/").send(context.asyncAssertSuccess(resp -> {
      body3.set(resp.bodyAsString());
      req3.complete();
    }));
    req3.await();

    context.assertNotNull(body1.get());
    context.assertNotNull(body2.get());
    context.assertNotNull(body3.get());
    context.assertEquals(body1.get(), body2.get());
    context.assertNotEquals(body1.get(), body3.get());
  }

  @Test
  public void testPublicWithMaxAgeZeroAndExpiresFuture(TestContext context) {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(context, req -> {
      req.response().headers().set("Cache-Control", "public, max-age=0");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached(context);
  }

  @Test
  public void testPublicWithMaxAgeZeroAndExpiresZero(TestContext context) {
    String expires = DateFormatter.format(new Date());
    startMockServer(context, req -> {
      req.response().headers().set("Cache-Control", "public, max-age=0");
      req.response().headers().set("Expires", expires);
    });

    assertNotCached(context);
  }

  @Test
  public void testPublicAndPrivate(TestContext context) {
    // This is a silly case because it is invalid, but it validates that we err on the side of not
    // caching responses.
    startMockServer(context, "public, private, max-age=300");
    assertNotCached(context);
  }

  @Test
  public void testUpdateStaleResponse(TestContext context) {
    Async waiter = context.async();

    startMockServer(context, "public, max-age=1");

    String body1 = executeGetBlocking(context);

    vertx.setTimer(2000, l -> waiter.complete());
    waiter.await();

    String body2 = executeGetBlocking(context);
    String body3 = executeGetBlocking(context);

    context.assertNotEquals(body1, body2);
    context.assertEquals(body2, body3);
  }

  @Test
  public void test304NotModifiedResponse(TestContext context) {
    Async primer = context.async();
    Async waiter = context.async();

    startMockServer(context, req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1");

      if (primer.isCompleted()) {
        req.response().setStatusCode(304);
        req.response().end();
      } else {
        req.response().setStatusCode(200);
        req.response().end(UUID.randomUUID().toString(), v -> primer.complete());
      }
    });

    String body1 = executeGetBlocking(context);
    primer.await();

    vertx.setTimer(2000L, l -> waiter.complete());
    waiter.await();

    String body2 = executeGetBlocking(context);

    context.assertEquals(body1, body2);
  }

  @Test
  public void testStaleWhileRevalidate(TestContext context) {
    startMockServer(context, "public, max-age=1, stale-while-revalidate=2");

    Async waiter1 = context.async();
    Async waiter2 = context.async();

    String body1 = executeGetBlocking(context);

    vertx.setTimer(2000, l -> waiter1.complete());
    waiter1.await();

    String body2 = executeGetBlocking(context);

    vertx.setTimer(1000, l -> waiter2.complete());
    waiter2.await();

    String body3 = executeGetBlocking(context);

    context.assertEquals(body1, body2);
    context.assertNotEquals(body1, body3);
  }

  @Test
  public void testStaleWhileRevalidateExpired(TestContext context) {
    startMockServer(context, "public, max-age=1, stale-while-revalidate=1");

    Async waiter1 = context.async();
    Async waiter2 = context.async();

    String body1 = executeGetBlocking(context);

    // max-age 1 + stale-while-revalidate 1 + leeway 1 => 3s
    vertx.setTimer(3000, l -> waiter1.complete());
    waiter1.await();

    String body2 = executeGetBlocking(context);

    // max-age 1 + stale-while-revalidate 1 + leeway 1 => 3s
    vertx.setTimer(3000, l -> waiter2.complete());
    waiter2.await();

    String body3 = executeGetBlocking(context);

    context.assertNotEquals(body1, body2);
    context.assertNotEquals(body1, body3);
    context.assertNotEquals(body2, body3);
  }

  @Test
  public void testStaleIfError(TestContext context) {
    Async waiter = context.async();

    startMockServer(context, req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1, stale-if-error=2");
      if (waiter.isCompleted()) {
        req.response().setStatusCode(503);
        req.response().end();
      }
    });

    String body1 = executeGetBlocking(context);
    vertx.setTimer(2000L, l -> waiter.complete());
    waiter.await();
    String body2 = executeGetBlocking(context);

    context.assertEquals(body1, body2);
  }

  @Test
  public void testStaleIfErrorExpired(TestContext context) {
    Async waiter1 = context.async();
    Async waiter2 = context.async();
    Async request = context.async();
    AtomicReference<HttpResponse<Buffer>> response = new AtomicReference<>();

    startMockServer(context, req -> {
      req.response().headers().set(HttpHeaders.CACHE_CONTROL, "public, max-age=1, stale-if-error=2");
      if (waiter1.isCompleted()) {
        req.response().setStatusCode(503);
        req.response().end();
      }
    });

    String body1 = executeGetBlocking(context);
    vertx.setTimer(2000L, l -> waiter1.complete());
    waiter1.await();

    String body2 = executeGetBlocking(context);
    vertx.setTimer(3000L, l -> waiter2.complete());
    waiter2.await();

    defaultClient.get("localhost", "/").send(context.asyncAssertSuccess(resp -> {
      response.set(resp);
      request.complete();
    }));
    request.await();

    context.assertEquals(body1, body2);
    context.assertNull(response.get().bodyAsString());
    context.assertEquals(response.get().statusCode(), 503);
  }

  @Test
  public void testMatchingPaths(TestContext context) {
    startMockServer(context, "public, max-age=300");

    String body1 = executeGetBlocking(context, "/path/to/resource");
    String body2 = executeGetBlocking(context, "/path/to/resource");

    context.assertEquals(body1, body2);
  }

  @Test
  public void testDifferentPaths(TestContext context) {
    startMockServer(context, "public, max-age=300");

    String body1 = executeGetBlocking(context, "/path/to/resource");
    String body2 = executeGetBlocking(context, "/other/path");

    context.assertNotEquals(body1, body2);
  }

  @Test
  public void testWithMatchingQueryParams(TestContext context) {
    startMockServer(context, "public, max-age=300");

    String body1 = executeGetBlocking(context, req -> {
      req.setQueryParam("q", "search");
    });

    String body2 = executeGetBlocking(context, req -> {
      req.setQueryParam("q", "search");
    });

    context.assertEquals(body1, body2);
  }

  @Test
  public void testWithDifferentQueryParams(TestContext context) {
    startMockServer(context, "public, max-age=300");

    String body1 = executeGetBlocking(context, req -> {
      req.setQueryParam("q", "search");
    });

    String body2 = executeGetBlocking(context, req -> {
      req.setQueryParam("q", "other");
    });

    context.assertNotEquals(body1, body2);
  }

  @Test
  public void testWithDifferentQueryParamOrdering(TestContext context) {
    startMockServer(context, "public, max-age=300");

    String body1 = executeGetBlocking(context, req -> {
      req
        .setQueryParam("q", "search")
        .setQueryParam("param", "value");
    });

    String body2 = executeGetBlocking(context, req -> {
      req
        .setQueryParam("param", "value")
        .setQueryParam("q", "search");
    });

    context.assertEquals(body1, body2);
  }

  // Cache-Control: private with client NOT enabled private caching

  @Test
  public void testPrivate(TestContext context) {
    startMockServer(context, "private");
    assertNotCached(context);
  }

  @Test
  public void testPrivateMaxAge(TestContext context) {
    startMockServer(context, "private, max-age=300");
    assertNotCached(context);
  }

  @Test
  public void testPrivateMaxAgeZero(TestContext context) {
    startMockServer(context, "private, max-age=0");
    assertNotCached(context);
  }

  @Test
  public void testPrivateExpires(TestContext context) {
    String expires = DateFormatter.format(new Date(
      System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()
    ));
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "private");
      req.response().headers().add("Expires", expires);
    });

    assertNotCached(context);
  }

  // Cache-Control: private with client enabled private caching

  @Test
  public void testPrivateEnabled(TestContext context) {
    startMockServer(context, "private");
    assertCached(context, privateClient);
  }

  @Test
  public void testPrivateEnabledMaxAge(TestContext context) {
    startMockServer(context, "private, max-age=300");
    assertCached(context, privateClient);
  }

  @Test
  public void testPrivateEnabledMaxAgeZero(TestContext context) {
    startMockServer(context, "private, max-age=0");
    assertNotCached(context, privateClient);
  }

  @Test
  public void testPrivateSharedMaxAgeAndMaxAgeZero(TestContext context) {
    startMockServer(context, "private, s-maxage=300, max-age=0");
    assertNotCached(context, privateClient);
  }

  @Test
  public void testPrivateSharedMaxAgeAndMaxAge(TestContext context) {
    startMockServer(context, "private, s-maxage=300, max-age=1");

    Async waiter = context.async();

    String body1 = executeGetBlocking(context, privateClient);
    String body2 = executeGetBlocking(context, privateClient);

    // Wait for the max-age time to pass, but not long enough for s-maxage
    // HTTP cache only has 1 second resolution, so this must be 1+ seconds past than the max-age
    vertx.setTimer(2000, l -> waiter.complete());
    waiter.await();

    String body3 = executeGetBlocking(context, privateClient);

    context.assertEquals(body1, body2);
    context.assertNotEquals(body2, body3);
  }

  // Cache-Control: public; Vary: User-Agent

  @Test
  public void testPublicVaryMaxAgeZero(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=0");
      req.response().headers().add("Vary", "User-Agent");
    });

    assertNotCached(context, varyClient);
  }

  @Test
  public void testVaryUserAgentTwoDesktops(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent");
    });

    // Chrome Desktop
    String body1 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // Firefox Desktop
    String body2 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0");
    });

    // Desktop user agents are normalized so two desktop clients should hit the same cache
    context.assertEquals(body1, body2);
  }

  @Test
  public void testVaryUserAgentDesktopVsMobile(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent");
    });

    // Chrome Desktop
    String body1 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // iPhone Mobile
    String body2 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    // Desktop and Mobile may receive different content and should not share a cache
    context.assertNotEquals(body1, body2);
  }

  // Cache-Control: public; Vary: Content-Encoding

  @Test
  public void testVaryEncodingOverlap(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Content-Encoding", "gzip");
      req.response().headers().add("Vary", "Accept-Encoding");
    });

    String body1 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("Accept-Encoding", "gzip,deflate,sdch");
    });

    String body2 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("Accept-Encoding", "gzip,deflate");
    });

    // Both accept gzip, so cache should be used
    context.assertEquals(body1, body2);
  }

  @Test
  public void testVaryEncodingDifferent(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Content-Encoding", "gzip");
      req.response().headers().add("Vary", "Accept-Encoding");
    });

    String body1 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("Accept-Encoding", "gzip,deflate");
    });

    String body2 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("Accept-Encoding", "br");
    });

    context.assertNotEquals(body1, body2);
  }

  @Test
  public void testVaryCustomHeader(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "X-Custom-Header");
    });

    String body1 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("X-Custom-Header", "0x00000000");
    });

    String body2 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("X-Custom-Header", "0xDEADBEEF");
    });

    String body3 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("X-Custom-Header", "0x00000000");
    });

    context.assertNotEquals(body1, body2);
    context.assertNotEquals(body2, body3);
    context.assertEquals(body1, body3);
  }

  @Test
  public void testVaryUserAgentAndCustomHeader(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=300");
      req.response().headers().add("Vary", "User-Agent, X-Custom-Header");
    });

    // 1. Chrome desktop, custom header 0
    String body1 = executeGetBlocking(context, varyClient, req -> {
      req
        .putHeader("X-Custom-Header", "0x00000000")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 2. Chrome desktop, custom header deadbeef, should not be cached
    String body2 = executeGetBlocking(context, varyClient, req -> {
      req
        .putHeader("X-Custom-Header", "0xDEADBEEF")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 3. Chrome desktop, custom header 0, should be cached from req1
    String body3 = executeGetBlocking(context, varyClient, req -> {
      req
        .putHeader("X-Custom-Header", "0x00000000")
        .putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
    });

    // 4. iPhone mobile, custom header 0, should not be cached
    String body4 = executeGetBlocking(context, varyClient, req -> {
      req
        .putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1")
        .putHeader("X-Custom-Header", "0x00000000");
    });

    context.assertNotEquals(body1, body2);
    context.assertEquals(body1, body3);
    context.assertNotEquals(body1, body4);
    context.assertNotEquals(body2, body3);
    context.assertNotEquals(body2, body4);
    context.assertNotEquals(body3, body4);
  }

  @Test
  public void testVaryWithStaleResponse(TestContext context) {
    startMockServer(context, req -> {
      req.response().headers().add("Cache-Control", "public, max-age=2");
      req.response().headers().add("Vary", "User-Agent");
    });

    Async waiter = context.async();

    String body1 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    String body2 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    vertx.setTimer(3000, l -> waiter.complete());
    waiter.await();

    String body3 = executeGetBlocking(context, varyClient, req -> {
      req.putHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1");
    });

    context.assertEquals(body1, body2);
    context.assertNotEquals(body1, body3);
    context.assertNotEquals(body2, body3);
  }

  static class TestCacheStore implements CacheStore {
    public final Map<String, CachedHttpResponse> db = new ConcurrentHashMap<>();

    @Override
    public Future<CachedHttpResponse> get(CacheKey key) {
      return Future.succeededFuture(db.get(key.toString()));
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
