package io.vertx.ext.web.client.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.cache.CacheManager;
import io.vertx.ext.web.client.impl.CacheInterceptor;

import java.util.Optional;

/**
 * Implementation of Cache Manager based on Caffeine
 *
 * @author Alexey Soshin
 */
public class CaffeineCacheManager implements CacheManager<CacheInterceptor.CacheKey> {

  private final Cache<CacheInterceptor.CacheKey, HttpResponse<Object>> cache;

  public CaffeineCacheManager(Cache<CacheInterceptor.CacheKey, HttpResponse<Object>> cache) {
    this.cache = cache;
  }

  @Override
  public void flush() {
    cache.invalidateAll();
  }

  @Override
  public Optional<HttpResponse<Object>> fetch(CacheInterceptor.CacheKey cacheKey) {
    HttpResponse<Object> value = cache.getIfPresent(cacheKey);

    if (value == null) {
      return Optional.empty();
    } else {
      return Optional.of(value);
    }
  }

  @Override
  public void put(CacheInterceptor.CacheKey cacheKey, HttpResponse<Object> response) {
    cache.put(cacheKey, response);
  }

  @Override
  public void remove(CacheInterceptor.CacheKey cacheKey) {
    cache.invalidate(cacheKey);
  }
}
