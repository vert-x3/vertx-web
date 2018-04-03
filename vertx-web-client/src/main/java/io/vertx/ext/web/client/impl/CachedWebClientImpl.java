package io.vertx.ext.web.client.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.*;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * @author Alexey Soshin
 */
public class CachedWebClientImpl implements CachedWebClient {


    private static final Logger log = LoggerFactory.getLogger(CachedWebClientImpl.class);

    private final WebClientImpl client;
    private final CachedWebClientOptions options;
    private final CacheInterceptor cache;

    public CachedWebClientImpl(WebClient webClient, CachedWebClientOptions options) {
        if (!(webClient instanceof WebClientImpl)) {
            throw new RuntimeException("Can only wrap WebClientImpl");
        }
        this.client = (WebClientImpl) webClient;
        if (options == null) {
            options = new CachedWebClientOptions();
        }
        this.options = options;

        this.cache = new CacheInterceptor();
        this.client.addInterceptor(cache);
    }

    // Can I has delegate?
    @Override
    public HttpRequest<Buffer> get(int port, String host, String requestURI) {
        return client.get(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> get(String requestURI) {
        return client.get(requestURI);
    }

    @Override
    public HttpRequest<Buffer> get(String host, String requestURI) {
        return client.get(host, requestURI);
    }

    public static WebClient create(Vertx vertx) {
        return WebClient.create(vertx);
    }

    @Override
    public HttpRequest<Buffer> getAbs(String absoluteURI) {
        return client.getAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> post(String requestURI) {
        return client.post(requestURI);
    }

    public static WebClient create(Vertx vertx, WebClientOptions options) {
        return WebClient.create(vertx, options);
    }

    @Override
    public HttpRequest<Buffer> post(String host, String requestURI) {
        return client.post(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> post(int port, String host, String requestURI) {
        return client.post(port, host, requestURI);
    }

    public static WebClient wrap(HttpClient httpClient) {
        return WebClient.wrap(httpClient);
    }

    @Override
    public HttpRequest<Buffer> put(String requestURI) {
        return client.put(requestURI);
    }

    @Override
    public HttpRequest<Buffer> put(String host, String requestURI) {
        return client.put(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> put(int port, String host, String requestURI) {
        return client.put(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> delete(String host, String requestURI) {
        return client.delete(host, requestURI);
    }

    public static WebClient wrap(HttpClient httpClient, WebClientOptions options) {
        return WebClient.wrap(httpClient, options);
    }

    @Override
    public HttpRequest<Buffer> delete(String requestURI) {
        return client.delete(requestURI);
    }

    @Override
    public HttpRequest<Buffer> delete(int port, String host, String requestURI) {
        return client.delete(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(String requestURI) {
        return client.patch(requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(String host, String requestURI) {
        return client.patch(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> patch(int port, String host, String requestURI) {
        return client.patch(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(String requestURI) {
        return client.head(requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(String host, String requestURI) {
        return client.head(host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> head(int port, String host, String requestURI) {
        return client.head(port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> postAbs(String absoluteURI) {
        return client.postAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> putAbs(String absoluteURI) {
        return client.putAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> deleteAbs(String absoluteURI) {
        return client.deleteAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> patchAbs(String absoluteURI) {
        return client.patchAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> headAbs(String absoluteURI) {
        return client.headAbs(absoluteURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
        return client.request(method, requestURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, RequestOptions requestOptions) {
        return client.request(method, requestOptions);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
        return client.request(method, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
        return client.request(method, port, host, requestURI);
    }

    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method, String surl) {
        return client.requestAbs(method, surl);
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void flushCache() {
        cache.flush();
    }

    /**
     * Response cache implemented as WebClient interceptor
     */
    private class CacheInterceptor implements Handler<HttpContext> {

        private final Map<CacheKey, HttpResponse<Object>> cache = new ConcurrentHashMap<>();
        private final LinkedHashSet<CacheKey> lru = new LinkedHashSet<>();

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

            CacheKey(HttpRequestImpl request) {
                this.method = request.method;
                this.host = request.host;
                this.port = request.port;
                this.uri = request.uri;
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
                if (!host.equals(cacheKey.host)) return false;
                if (!uri.equals(cacheKey.uri)) return false;
                return params != null ? params.equals(cacheKey.params) : cacheKey.params == null;
            }

            @Override
            public int hashCode() {
                int result = method.hashCode();
                result = 31 * result + host.hashCode();
                result = 31 * result + port;
                result = 31 * result + uri.hashCode();
                result = 31 * result + (params != null ? params.hashCode() : 0);
                return result;
            }
        }
    }
}
