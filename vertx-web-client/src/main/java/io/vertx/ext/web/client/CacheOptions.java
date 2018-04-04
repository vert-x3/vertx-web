package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.Fluent;

/**
 * @author Alexey Soshin
 */
public class CacheOptions {
    private int maxEntries = 1000;

    @Fluent
    public CacheOptions setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }

    public int getMaxEntries() {
        return maxEntries;
    }
}
