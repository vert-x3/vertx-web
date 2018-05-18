package io.vertx.ext.web.client.impl;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.cache.CacheOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.cache.CacheManager;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexey Soshin
 */
public class CacheManagerImpl implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CacheManagerImpl.class);

    private final Map<CacheInterceptor.CacheKey, CacheValue> cache = new ConcurrentHashMap<>();
    private final LinkedHashSet<CacheInterceptor.CacheKey> lru = new LinkedHashSet<>();
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
                Iterator<CacheInterceptor.CacheKey> it = lru.iterator();
                if (it.hasNext()) {
                    CacheInterceptor.CacheKey lruKey = it.next();
                    it.remove();
                    cache.remove(lruKey);
                }
            }
        }
    }

    public Optional<HttpResponse<Object>> fetch(CacheInterceptor.CacheKey cacheKey) {

        // Always invalidate before checking if cache contains the value
        invalidate();
        if (cache.containsKey(cacheKey)) {
            CacheValue cacheValue = cache.get(cacheKey);

            synchronized (this) {
                // Promote this value in cache
                // First value are those to be removed, so we pull our element from whenever it is,
                // and put it in the end
                lru.remove(cacheKey);
                lru.add(cacheKey);
            }
            return Optional.of(cacheValue.value);
        }

        return Optional.empty();
    }


    public void remove(CacheInterceptor.CacheKey cacheKey) {
        synchronized (this) {
            cache.remove(cacheKey);
            lru.remove(cacheKey);
        }
    }

    public void put(CacheInterceptor.CacheKey cacheKey, HttpResponse<Object> response) {
        synchronized (this) {
            lru.add(cacheKey);
            cache.put(cacheKey, new CacheManagerImpl.CacheValue(response));
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
