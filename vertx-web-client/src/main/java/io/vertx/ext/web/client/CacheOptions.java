package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.Fluent;

/**
 * @author Alexey Soshin
 */
public class CacheOptions {
    private int maxEntries = 0;

    public CacheOptions() {

    }

    public CacheOptions(CacheOptions other) {
        if (other != null) {
            this.maxEntries = other.getMaxEntries();
        }
    }

    /**
     * Passing zero or negative value disables the cache
     * @param maxEntries
     * @return
     */
    @Fluent
    public CacheOptions setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }

    public int getMaxEntries() {
        return maxEntries;
    }
}
