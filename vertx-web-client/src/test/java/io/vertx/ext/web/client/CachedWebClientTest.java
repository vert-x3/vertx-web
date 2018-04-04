package io.vertx.ext.web.client;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotEquals;

/**
 * @author Alexey Soshin
 */
public class CachedWebClientTest extends WebClientTest {

    @Test
    public void testShouldReturnCachedValueForSameRouteIfCacheNotFull() throws Exception {

        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(1)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        String res1 = syncRequest(webClient, "/", null);
        String res2 = syncRequest(webClient, "/", null);

        assertEquals(res1, res2);
    }

    @Test
    public void testClientCacheShouldNotReturnCachedValueForSameRouteIfCacheFlushed() throws Exception {

        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(1)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        String res1 = syncRequest(webClient, "/", null);
        webClient.cache().flush();
        String res2 = syncRequest(webClient, "/", null);

        assertNotEquals(res1, res2);
    }

    @Test
    public void testClientCacheShouldNotReturnCachedValueForSameRouteIfCacheFull() throws Exception {

        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(1)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        String res1 = syncRequest(webClient, "/", null);
        syncRequest(webClient, "/a", null);
        String res2 = syncRequest(webClient, "/", null);

        assertNotEquals(res1, res2);
    }

    @Test
    public void testShouldPromoteSameValueInCache() throws Exception {

        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(3)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        String res1 = syncRequest(webClient, "/", null);
        for (int i = 0; i < 3; i++) {
            syncRequest(webClient, "/"+i, null);
            syncRequest(webClient, "/", null);
        }
        String res2 = syncRequest(webClient, "/", null);

        assertEquals(res1, res2);
    }

    @Test
    public void testShouldRespectSameETag() throws Exception {
        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(3)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().putHeader("ETag", "123").end(UUID.randomUUID().toString());
        });
        startServer();

        Map<String, String> headers = new HashMap<>();
        headers.put("ETag", "123");

        String res1 = syncRequest(webClient, "/", null);
        String res2 = syncRequest(webClient, "/", headers);

        assertEquals(res1, res2);
    }

    @Test
    public void testShouldRespectCacheControlNoCache() throws Exception {
        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(3)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        Map<String, String> headers = new HashMap<>();
        headers.put("cache-control", "no-cache");

        String res1 = syncRequest(webClient, "/", null);
        String res2 = syncRequest(webClient, "/", headers);

        assertNotEquals(res1, res2);
    }

    @Test
    public void testShouldRespectCacheControlPublic() throws Exception {
        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(3)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        Map<String, String> headers = new HashMap<>();
        headers.put("cache-control", "public");

        String res1 = syncRequest(webClient, "/", null);
        String res2 = syncRequest(webClient, "/", headers);

        assertEquals(res1, res2);
    }

    @Test
    public void testShouldRespectDifferentETag() throws Exception {
        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(3)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().putHeader("ETag", "345").end(UUID.randomUUID().toString());
        });
        startServer();

        Map<String, String> headers = new HashMap<>();
        headers.put("ETag", "123");

        String res1 = syncRequest(webClient, "/", null);
        String res2 = syncRequest(webClient, "/", headers);

        assertNotEquals(res1, res2);
    }

    @Test
    public void testShouldRespectSameContentType() throws Exception {
        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(3)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();


        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/plain");

        String res1 = syncRequest(webClient, "/", headers);
        String res2 = syncRequest(webClient, "/", headers);

        assertEquals(res1, res2);
    }

    @Test
    public void testShouldRespectDifferentContentType() throws Exception {
        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setCacheOptions(new CacheOptions().setMaxEntries(3)));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();


        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/html");

        String res1 = syncRequest(webClient, "/", headers);

        headers.put("content-type", "text/plain");
        String res2 = syncRequest(webClient, "/", headers);

        assertNotEquals(res1, res2);
    }

    private String syncRequest(WebClient webClient, String uri, Map<String, String> headers) {
        Future<String> f = Future.future();
        HttpRequest<Buffer> req = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, uri);

        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                req.putHeader(e.getKey(), e.getValue());
            }
        }

        req.send(h -> {
            f.complete(h.result().bodyAsString());
        });

        // Can I has await?
        while (!f.isComplete()) {

        }
        return f.result();
    }
}
