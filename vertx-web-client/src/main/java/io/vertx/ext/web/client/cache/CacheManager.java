package io.vertx.ext.web.client.cache;

import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;

import java.util.Optional;

/**
 * Abstracts caching of HTTP requests and responses
 * @author Alexey Soshin
 */
public interface CacheManager {

    /**
     * Clears the cache completely
     */
    void flush();

    /**
     * Attempts to fetch value corresponding to request from cache
     * @param request request is used to build cache key
     * @return cached HTTP response or Optional.empty
     */
    Optional<HttpResponse<Object>> fetch(HttpRequest request);

    /**
     * Puts new value in cache, where the key is based on request and value on response
     * @param request   HTTP request that was issues
     * @param response  HTTP response to be associated
     */
    void put(HttpRequest request, HttpResponse<Object> response);

    /**
     * Removes value from cache corresponding to request key
     * @param request HTTP request that should be removed from cache
     */
    void remove(HttpRequest request);
}
