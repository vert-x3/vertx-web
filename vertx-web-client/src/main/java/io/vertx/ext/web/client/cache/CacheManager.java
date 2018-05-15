package io.vertx.ext.web.client.cache;

import io.vertx.core.Handler;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;

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
     * @param hitHandler    this handler is called if key was found in cache
     *                      to return the value immediately
     * @param missHandler   in case key is not in cache, you should supply a handler
     *                      that will be called when the request returns to set the value
     */
    void fetch(HttpRequest request,
               Handler<HttpResponse<Object>> hitHandler,
               Handler<Handler<HttpResponse<Object>>> missHandler);
}
