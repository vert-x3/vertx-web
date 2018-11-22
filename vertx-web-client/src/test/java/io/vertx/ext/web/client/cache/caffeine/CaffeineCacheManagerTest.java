package io.vertx.ext.web.client.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.vertx.ext.web.client.CachedWebClientTest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.cache.CacheManager;
import io.vertx.ext.web.client.impl.CacheInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by alexeysoshin on 17/05/2018.
 */
public class CaffeineCacheManagerTest extends CachedWebClientTest {

    @Test
    public void testShouldCacheSameRequest() throws Exception {
        WebClientOptions options = new WebClientOptions();

        Cache<CacheInterceptor.CacheKey, HttpResponse<Object>> cache = Caffeine.newBuilder()
                .maximumSize(1)
                .build();
        WebClient webClient = WebClient.create(vertx,
                options, new CaffeineCacheManager(cache));

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
    public void testFlushShouldEmptyCache() throws Exception {
        WebClientOptions options = new WebClientOptions();

        Cache<CacheInterceptor.CacheKey, HttpResponse<Object>> cache = Caffeine.newBuilder()
                .maximumSize(1)
                .build();

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(cache);
        WebClient webClient = WebClient.create(vertx,
                options, cacheManager);

        // Generate unique ID each time
        server.requestHandler(h -> {
            h.response().end(UUID.randomUUID().toString());
        });
        startServer();

        String res1 = syncRequest(webClient, "/abc", null);

        cacheManager.flush();
        String res2 = syncRequest(webClient, "/abc", null);

        assertNotEquals(res1, res2);
    }
}
