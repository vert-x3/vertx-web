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
     * @param request       request 
     * @param hitHandler
     * @param missHandler
     */
    void fetch(HttpRequest request,
               Handler<HttpResponse<Object>> hitHandler,
               Handler<Handler<HttpResponse<Object>>> missHandler);
}
