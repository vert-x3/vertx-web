package io.vertx.ext.web.client.impl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.CacheOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.cache.CacheKeyValue;
import io.vertx.ext.web.client.cache.CacheManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Alexey Soshin
 */
public class CacheManagerImpl implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CacheManagerImpl.class);

    private final Map<CacheKey, CacheValue> cache = new ConcurrentHashMap<>();
    private final LinkedHashSet<CacheKey> lru = new LinkedHashSet<>();
    private CacheOptions options;

    CacheManagerImpl(CacheOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("No cache options supplied");
        }
        this.options = options;
    }

    @Override
    public void flush() {
        synchronized (this) {
            lru.clear();
            cache.clear();
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

    public Optional<HttpResponse<Object>> fetch(HttpRequest request,
                      Handler<Handler<HttpResponse<Object>>> missHandler) {
        CacheKey cacheKey = new CacheKey(request);

        // Always invalidate before checking if cache contains the value
        invalidate();
        Set<String> cacheControl = parseCacheControl(request);
        if (shouldUseCache(cacheControl) && cache.containsKey(cacheKey)) {
            CacheValue cacheValue = cache.get(cacheKey);

            if (isValueExpired(request, cacheControl, cacheValue)) {
                synchronized (this) {
                    cache.remove(cacheKey);
                    lru.remove(cacheKey);
                }
                handleCacheMiss(missHandler, cacheKey);
            }
            else {
                synchronized (this) {
                    // Promote this value in cache
                    // First value are those to be removed, so we pull our element from whenever it is,
                    // and put it in the end
                    lru.remove(cacheKey);
                    lru.add(cacheKey);
                }
                return Optional.of(cacheValue.value);
            }
        }
        // No cache entry
        else {
            handleCacheMiss(missHandler, cacheKey);
        }

        return Optional.empty();
    }

    private void handleCacheMiss(Handler<Handler<HttpResponse<Object>>> missHandler,
                                 CacheKey cacheKey) {
        missHandler.handle(new Handler<HttpResponse<Object>>() {
            @Override
            public void handle(HttpResponse<Object> httpResponse) {
                // Cache response and add it as most recently used
                synchronized (this) {
                    lru.add(cacheKey);
                    cache.put(cacheKey, new CacheManagerImpl.CacheValue(httpResponse));
                }
            }
        });
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
                                   CacheValue cacheValue) {
        if (!isValidEtag(request, cacheValue)) {
            return true;
        }

        Optional<Integer> maxAge = parseMaxAge(cacheControl);

        // Cannot expire if not set
        if (!maxAge.isPresent()) {
            return false;
        }

        // Check that this entry has not expired if age was set
        return System.currentTimeMillis() > cacheValue.createdAt + maxAge.get();
    }

    /**
     * Compares ETag header value from the request with the one cached
     * @param request
     * @param cacheValue
     * @return true if one of the ETags is empty, or both equals
     *         false if both present but not equal
     */
    private boolean isValidEtag(HttpRequest request, CacheValue cacheValue) {
        String requestEtag = request.headers().get("ETag");

        if (requestEtag == null) {
            return true;
        }

        String responseEtag = cacheValue.value.headers().get("ETag");

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
            for (CacheKeyValue v : keyStructure) {
                switch (v) {
                    case METHOD:
                        this.method = request.getMethod();
                        break;
                    case HOST:
                        this.host = request.getHost();
                        break;
                    case PORT:
                        this.port = request.getPort();
                        break;
                    case URI:
                        this.uri = request.getURI();
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

    public class CacheValue {
        long createdAt;
        HttpResponse<Object> value;

        CacheValue(HttpResponse<Object> response) {
            this.value = response;
            this.createdAt = System.currentTimeMillis();
        }
    }
}
