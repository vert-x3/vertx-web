package io.vertx.ext.web.client.cache;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.vertx.ext.web.client.cache.CacheKeyValue.*;

/**
 * @author Alexey Soshin
 */
@DataObject
public class CacheOptions {

    private final List<CacheKeyValue> DEFAULT_CACHE_KEY_STRUCTURE = Arrays.asList(
            METHOD, HOST, PORT, URI, PARAMS, CONTENT_TYPE
    );

    private int maxEntries = 0;
    private List<CacheKeyValue> cacheKeyValue = DEFAULT_CACHE_KEY_STRUCTURE;

    public CacheOptions() {

    }

    public CacheOptions(CacheOptions other) {
        if (other != null) {
            this.maxEntries = other.getMaxEntries();
            if (other.cacheKeyValue != null) {
                this.cacheKeyValue = new ArrayList<>(other.cacheKeyValue);
            }
        }
    }

    /**
     * Creates a new instance from JSON
     *
     * @param json the JSON object
     */
    public CacheOptions(JsonObject json) {

    }

    /**
     * Passing zero or negative value disables the cache
     * @param maxEntries
     * @return
     */
    public CacheOptions setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public List<CacheKeyValue> getCacheKeyValue() {
        return cacheKeyValue;
    }

    public CacheOptions setCacheKeyValue(List<CacheKeyValue> cacheKeyValue) {
        this.cacheKeyValue = new ArrayList<>(cacheKeyValue);
        return this;
    }
}
