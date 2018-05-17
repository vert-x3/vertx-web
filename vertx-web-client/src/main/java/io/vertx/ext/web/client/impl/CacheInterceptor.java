package io.vertx.ext.web.client.impl;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.CacheOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.cache.CacheKeyValue;
import io.vertx.ext.web.client.cache.CacheManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Response cache implemented as WebClient interceptor
 */
public class CacheInterceptor implements Handler<HttpContext> {

    private final CacheManager cacheManager;

    private final DateFormat dateTimeFormatter;
    private final CacheOptions options;

    CacheInterceptor(CacheManager cacheManager, CacheOptions options) {
        this.cacheManager = cacheManager;
        this.options = options;

        this.dateTimeFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        this.dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void handle(HttpContext event) {
        HttpRequest request = event.request();

        HttpMethod method = ((HttpRequestImpl) request).method;

        // Cache only GET requests
        if (!method.equals(HttpMethod.GET)) {
            event.next();
        }
        else {

            if (request.headers().get("date") == null) {
                request.putHeader("date", dateTimeFormatter.format(new Date()));
            }

            CacheKey cacheKey = new CacheKey(request);
            // If this is a GET call, we should check the value in cache
            Optional<HttpResponse<Object>> valueFromCache = cacheManager.fetch(cacheKey);

            if (valueFromCache.isPresent()) {
                HttpResponse<Object> value = valueFromCache.get();
                Set<String> cacheControl = parseCacheControl(request);
                if (shouldUseCache(cacheControl)) {
                    if (!isValueExpired(request, cacheControl, value)) {
                        event.getResponseHandler().handle(Future.succeededFuture(valueFromCache.get()));
                        return;
                    }
                    else {
                        cacheManager.remove(cacheKey);
                    }
                }
            }

            handleCacheMiss(event, request);
        }
    }

    private void handleCacheMiss(HttpContext event, HttpRequest request) {
        Handler<AsyncResult<HttpResponse<Object>>> responseHandler = event.getResponseHandler();
        event.setResponseHandler(r -> {
            if (r.succeeded()) {
                cacheManager.put(new CacheKey(request), r.result());
            }
            responseHandler.handle(r);
        });
        event.next();
    }

    private boolean shouldUseCache(Set<String> cacheControl) {
        if (cacheControl.isEmpty()) {
            return true;
        }

        if (cacheControl.contains("public")) {
            return true;
        }

        return false;
    }

    /**
     * Creates a set of values out of Cache-Control header
     * @param request
     * @return
     */
    private Set<String> parseCacheControl(HttpRequest request) {
        String cacheControlValue = request.headers().get("cache-control");

        if (cacheControlValue == null) {
            return Collections.emptySet();
        }

        return Arrays.stream(cacheControlValue.split(",")).
                filter(Objects::nonNull).
                map(String::trim).
                map(String::toLowerCase).
                collect(Collectors.toSet());
    }

    /**
     * Validates ETag and max-age of the value in cache
     * @param request
     * @param cacheControl
     * @param cacheValue
     * @return
     */
    private boolean isValueExpired(HttpRequest request,
                                   Set<String> cacheControl,
                                   HttpResponse<Object> cacheValue) {
        if (!isValidEtag(request, cacheValue)) {
            return true;
        }

        Optional<Integer> maxAge = parseMaxAge(cacheControl);

        // Cannot expire if not set
        if (!maxAge.isPresent()) {
            return false;
        }

        try {
            String dateHeaderValue = cacheValue.getHeader("date");
            if (dateHeaderValue == null) {
                return false;
            }
            Date date = dateTimeFormatter.parse(dateHeaderValue);
            long current = System.currentTimeMillis();

            // Check that this entry has not expired if age was set
            return current > date.getTime() + maxAge.get();
        }
        catch (ParseException pe) {
            return false;
        }
    }

    /**
     * Compares ETag header value from the request with the one cached
     * @param request
     * @param cacheValue
     * @return true if one of the ETags is empty, or both equals
     *         false if both present but not equal
     */
    private boolean isValidEtag(HttpRequest request, HttpResponse<Object> cacheValue) {
        String requestEtag = request.headers().get("ETag");

        if (requestEtag == null) {
            return true;
        }

        String responseEtag = cacheValue.headers().get("ETag");

        if (responseEtag == null) {
            return true;
        }

        // If ETags are different, value should be considered expired
        if  (!responseEtag.equals(requestEtag)) {
            return false;
        }

        return true;
    }

    /**
     * Gets max-age header value if exists
     * @param cacheControl set of key=value pairs of Cache-Control header
     * @return optional representing value of max-age part
     */
    private Optional<Integer> parseMaxAge(Set<String> cacheControl) {
        Optional<String> maxAge = cacheControl.stream().
                filter(c -> c.startsWith("max-age")).
                findFirst();

        if (maxAge.isPresent()) {
            String[] maxAgeParts = maxAge.get().split("=");
            if (maxAgeParts.length > 1) {
                try {
                    return Optional.of(Integer.parseInt(maxAgeParts[1]));
                }
                catch (NumberFormatException nfe) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Maps between HTTP request and a key in hash map
     */
    public class CacheKey {
        private HttpMethod method = null;
        private String host = null;
        private Integer port = null;
        private String uri = null;
        private String params = null;
        private String contentType = null;

        CacheKey(HttpRequest request, List<CacheKeyValue> keyStructure) {
            HttpRequestImpl r = (HttpRequestImpl) request;
            for (CacheKeyValue v : keyStructure) {
                switch (v) {
                    case METHOD:
                        this.method = r.method;
                        break;
                    case HOST:
                        this.host = r.host;
                        break;
                    case PORT:
                        this.port = r.port;
                        break;
                    case URI:
                        this.uri = r.uri;
                        break;
                    case CONTENT_TYPE:
                        this.contentType = request.headers().get("content-type");
                        break;
                    case PARAMS:
                        // Concatenate all query params
                        this.params = StreamSupport.stream(request.queryParams().spliterator(), false).
                                sorted(Comparator.comparing(Map.Entry::getKey)).
                                map(Object::toString).
                                collect(Collectors.joining());
                        break;
                    // This would be the case if we added a new enum, but forgot to handle it here
                    default:
                        throw new RuntimeException("Unsupported cache key value " + v);
                }
            }
        }

        CacheKey(HttpRequest request) {
            this(request, options.getCacheKeyValue());
        }

        @Override
        public String toString() {
            return "CacheKey{" +
                    "method=" + method +
                    ", host='" + host + '\'' +
                    ", port=" + port +
                    ", uri='" + uri + '\'' +
                    ", params='" + params + '\'' +
                    ", contentType='" + contentType + '\'' +
                    '}';
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

            if (port != null ? !port.equals(cacheKey.port) : cacheKey.port != null) return false;
            if (method != null ? !method.equals(cacheKey.method) : cacheKey.method != null) return false;
            if (host != null ? !host.equals(cacheKey.host) : cacheKey.host != null) return false;
            if (uri != null ? !uri.equals(cacheKey.uri) : cacheKey.uri != null) return false;
            if (params != null ? !params.equals(cacheKey.params) : cacheKey.params != null) return false;
            return contentType != null ? contentType.equals(cacheKey.contentType) : cacheKey.contentType == null;
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + (host != null ? host.hashCode() : 0);
            result = 31 * result + (port != null ? port.hashCode() : 0);
            result = 31 * result + (uri != null ? uri.hashCode() : 0);
            result = 31 * result + (params != null ? params.hashCode() : 0);
            result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
            return result;
        }
    }
}