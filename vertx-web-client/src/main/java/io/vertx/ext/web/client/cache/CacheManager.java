package io.vertx.ext.web.client.cache;

import io.vertx.core.Handler;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;

import java.util.Optional;

/**
 * @author Alexey Soshin
 */
public interface CacheManager {

    /**
     * Clears the cache completely
     */
    void flush();

    /**
     * Attempts to fetch value corresponding to request from cache
     * @param request       request is used to build cache key
     * @param missHandler   in case key is not in cache, you should supply a handler
     *                      that will be called when the request returns to set the value
     * @return cached HTTP response or Optional.empty
     */
    Optional<HttpResponse<Object>> fetch(HttpRequest request,
                                         Handler<Handler<HttpResponse<Object>>> missHandler);
}
