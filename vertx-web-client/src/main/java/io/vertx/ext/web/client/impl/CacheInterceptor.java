package io.vertx.ext.web.client.impl;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.CacheOptions;
import io.vertx.ext.web.client.HttpResponse;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Response cache implemented as WebClient interceptor
 */
public class CacheInterceptor implements Handler<HttpContext> {

    private static final Logger log = LoggerFactory.getLogger(CacheInterceptor.class);

    private final Map<CacheKey, HttpResponse<Object>> cache = new ConcurrentHashMap<>();
    private final LinkedHashSet<CacheKey> lru = new LinkedHashSet<>();
    private final CacheOptions options;

    public CacheInterceptor(CacheOptions options) {
        this.options = options;
    }

    @Override
    public void handle(HttpContext event) {
        HttpRequestImpl request = (HttpRequestImpl) event.request();

        // Cache only GET requests
        if (!request.method.equals(HttpMethod.GET)) {
            event.next();
        }
        else {
            CacheKey cacheKey = new CacheKey(request);

            invalidate();
            if (cache.containsKey(cacheKey)) {
                HttpResponse<Object> cacheValue = cache.get(cacheKey);
                synchronized (this) {
                    // Promote this value in cache
                    // First value are those to be removed, so we pull our element from whenever it is,
                    // and put it in the end
                    lru.remove(cacheKey);
                    lru.add(cacheKey);
                }
                event.getResponseHandler().handle(Future.succeededFuture(cacheValue));
            }
            // No cache entry
            else {
                Handler<AsyncResult<HttpResponse<Object>>> responseHandler = event.getResponseHandler();
                event.setResponseHandler(r -> {
                    if (r.succeeded()) {
                        HttpResponse<Object> response = r.result();

                        // Cache response and add it as most recently used
                        synchronized (this) {
                            lru.add(cacheKey);
                            cache.put(cacheKey, response);
                        }
                    }
                    responseHandler.handle(r);
                });
                event.next();
            }
        }
    }

    /**
     * Removes least recently used element from the cache
     */
    private void invalidate() {
        if (lru.size() > options.getMaxEntries()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Cache is full, size is %d", lru.size()));
            }
            synchronized (this) {
                Iterator<CacheKey> it = lru.iterator();
                if (it.hasNext()) {
                    CacheKey lruKey = it.next();
                    it.remove();
                    cache.remove(lruKey);
                }
            }
        }
    }

    public void flush() {
        synchronized (this) {
            lru.clear();
            cache.clear();
        }
    }

    private class CacheKey {
        private final HttpMethod method;
        private final String host;
        private final int port;
        private final String uri;
        private final String params;
        private final String contentType;

        CacheKey(HttpRequestImpl request) {
            this.method = request.method;
            this.host = request.host;
            this.port = request.port;
            this.uri = request.uri;
            this.contentType = request.headers().get("content-type");
            // Concatenate all query params
            this.params = StreamSupport.stream(request.queryParams().spliterator(), false).
                    sorted().
                    map(Object::toString).
                    collect(Collectors.joining());
        }

        /**
         * This is very important, as it allows us to locate "similar" cache values
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (port != cacheKey.port) return false;
            if (method != cacheKey.method) return false;
            if (host != null ? !host.equals(cacheKey.host) : cacheKey.host != null) return false;
            if (uri != null ? !uri.equals(cacheKey.uri) : cacheKey.uri != null) return false;
            if (params != null ? !params.equals(cacheKey.params) : cacheKey.params != null) return false;
            return contentType != null ? contentType.equals(cacheKey.contentType) : cacheKey.contentType == null;
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + (host != null ? host.hashCode() : 0);
            result = 31 * result + port;
            result = 31 * result + (uri != null ? uri.hashCode() : 0);
            result = 31 * result + (params != null ? params.hashCode() : 0);
            result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
            return result;
        }
    }
}