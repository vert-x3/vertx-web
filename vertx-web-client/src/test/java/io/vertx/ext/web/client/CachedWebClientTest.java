package io.vertx.ext.web.client;

import io.vertx.core.Future;
import org.junit.Test;

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

        String res1 = syncRequest(webClient, "/");
        String res2 = syncRequest(webClient, "/");

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

        String res1 = syncRequest(webClient, "/");
        webClient.cache().flush();
        String res2 = syncRequest(webClient, "/");

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

        String res1 = syncRequest(webClient, "/");
        syncRequest(webClient, "/a");
        String res2 = syncRequest(webClient, "/");

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

        String res1 = syncRequest(webClient, "/");
        for (int i = 0; i < 3; i++) {
            syncRequest(webClient, "/"+i);
            syncRequest(webClient, "/");
        }
        String res2 = syncRequest(webClient, "/");

        assertEquals(res1, res2);
    }

    private String syncRequest(WebClient webClient, String uri) {
        Future<String> f = Future.future();
        webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, uri).send(h -> {
            f.complete(h.result().bodyAsString());
        });

        // Can I has await?
        while (!f.isComplete()) {

        }
        return f.result();
    }
}
