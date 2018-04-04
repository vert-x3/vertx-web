package io.vertx.ext.web.client;

import io.vertx.ext.web.client.impl.CachedWebClientImpl;

/**
 * Wrapper for WebClient that is able to cache responses and serve them instead of going over the network again
 *
 * @author Alexey Soshin
 */
public interface CachedWebClient extends WebClient {
    static CachedWebClient create(WebClient webClient, CacheOptions options) {
        return new CachedWebClientImpl(webClient, options);
    }

    /**
     * Flushes all caches
     */
    void flushCache();
}
