package io.vertx.ext.web.client;

import io.vertx.core.Future;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexey Soshin
 */
public class CachedWebClientTest extends WebClientTest {

    @Test
    public void testClientCacheShouldReturnCachedValueForSameRouteIfCacheNotFull() throws Exception {

        WebClient webClient = CachedWebClient.create(WebClient.create(vertx),
                new CachedWebClientOptions().setMaxEntries(1));

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        String res1 = syncRequest(webClient, "/");
        String res2 = syncRequest(webClient, "/");

        assertEquals(res1, res2);
    }

    private String syncRequest(WebClient webClient, String uri) {
        Future<String> f = Future.future();
        webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/").send(h -> {
            f.complete(h.result().bodyAsString());
        });

        // Can I has await?
        while (!f.isComplete()) {

        }
        return f.result();
    }
}
