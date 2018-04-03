package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.Fluent;

/**
 * @author Alexey Soshin
 */
public class CachedWebClientOptions {
    private int maxEntries = 1000;

    @Fluent
    public CachedWebClientOptions setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }

    public int getMaxEntries() {
        return maxEntries;
    }
}
