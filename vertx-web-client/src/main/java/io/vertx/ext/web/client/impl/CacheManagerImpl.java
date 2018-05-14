package io.vertx.ext.web.client.impl;

import io.vertx.ext.web.client.cache.CacheManager;
import io.vertx.ext.web.client.CacheOptions;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * @author Alexey Soshin
 */
public class CacheManagerImpl implements CacheManager {
    private CacheInterceptor cacheInterceptor;

    void initCache(WebClientInternal client, WebClientOptions options) {
        if (options.getCacheOptions() != null) {
            CacheOptions cacheOptions = options.getCacheOptions();
            if (cacheOptions.getMaxEntries() > 0) {
                this.cacheInterceptor = new CacheInterceptor(cacheOptions);
                client.addInterceptor(this.cacheInterceptor);
            }
        }
    }

    @Override
    public void flush() {
        if (this.cacheInterceptor != null) {
            this.cacheInterceptor.flush();
        }
    }
}
