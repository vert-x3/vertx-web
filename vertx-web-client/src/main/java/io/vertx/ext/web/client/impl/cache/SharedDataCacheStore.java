/*
 * Copyright 2021 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.client.impl.cache;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.client.spi.CacheStore;

/**
 * A basic implementation of a {@link CacheStore} using an {@link AsyncMap} from {@link SharedData}.
 */
public class SharedDataCacheStore implements CacheStore {

  private static final String ASYNC_MAP_NAME = "HttpCacheStore";

  private final SharedData sharedData;

  public SharedDataCacheStore(Vertx vertx) {
    this.sharedData = vertx.sharedData();
  }

  @Override
  public Future<CachedHttpResponse> get(CacheKey key) {
    return asyncMap().compose(map -> map.get(key));
  }

  @Override
  public Future<CachedHttpResponse> set(CacheKey key, CachedHttpResponse response) {
    return asyncMap()
      .compose(map -> map.put(key, response))
      .map(response);
  }

  @Override
  public Future<Void> delete(CacheKey key) {
    return asyncMap().compose(map -> map.remove(key)).mapEmpty();
  }

  @Override
  public Future<Void> flush() {
    return asyncMap().compose(AsyncMap::clear);
  }

  private Future<AsyncMap<CacheKey, CachedHttpResponse>> asyncMap() {
    return sharedData.getAsyncMap(ASYNC_MAP_NAME);
  }
}
