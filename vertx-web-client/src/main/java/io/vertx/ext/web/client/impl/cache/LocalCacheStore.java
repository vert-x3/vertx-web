package io.vertx.ext.web.client.impl.cache;

import io.vertx.core.Future;
import io.vertx.ext.web.client.spi.CacheStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalCacheStore implements CacheStore {

  private final Map<CacheKey, CachedHttpResponse> localMap = new ConcurrentHashMap<>();

  @Override
  public Future<CachedHttpResponse> get(CacheKey key) {
    return Future.succeededFuture(localMap.get(key));
  }

  @Override
  public Future<CachedHttpResponse> set(CacheKey key, CachedHttpResponse response) {
    return Future.succeededFuture(localMap.put(key, response));
  }

  @Override
  public Future<Void> delete(CacheKey key) {
    localMap.remove(key);
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> flush() {
    localMap.clear();
    return Future.succeededFuture();
  }
}
