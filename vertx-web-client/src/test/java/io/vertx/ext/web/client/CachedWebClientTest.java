package io.vertx.ext.web.client;

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


        CountDownLatch latch = new CountDownLatch(2);
        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        StringHolder s1 = new StringHolder();
        webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/").send(h -> {
            s1.s = h.result().bodyAsString();
            latch.countDown();
        });

        // This should be fetched from cache
        StringHolder s2 = new StringHolder();
        webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/").send(h -> {
            s2.s = h.result().bodyAsString();
            latch.countDown();
        });

        latch.await(1, TimeUnit.SECONDS);

        assertEquals(s1.s, s2.s);
    }

    /**
     * Convenience class to hold responses
     */
    class StringHolder {
        public String s;
    }
}
