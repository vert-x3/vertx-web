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
package io.vertx.ext.web.client.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.impl.cache.CacheKey;
import io.vertx.ext.web.client.impl.cache.CachedHttpResponse;
import io.vertx.ext.web.client.impl.cache.NoOpCacheStore;
import io.vertx.ext.web.client.impl.cache.SharedDataCacheStore;

/**
 * An API to store and retrieve HTTP responses.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public interface CacheStore {

  /**
   * Builds a no-op cache store.
   *
   * @return the new cache store
   */
  static CacheStore build() {
    return new NoOpCacheStore();
  }

  /**
   * Builds a cache store that uses an {@link io.vertx.core.shareddata.AsyncMap} from
   * {@link io.vertx.core.shareddata.SharedData}.
   *
   * @param vertx the vertx instance
   * @return the new cache store
   */
  static CacheStore sharedDataStore(Vertx vertx) {
    return new SharedDataCacheStore(vertx);
  }

  /**
   * Retrieve a cached response.
   *
   * @param key the key to retrieve
   * @return the response as stored in the cache
   */
  Future<CachedHttpResponse> get(CacheKey key);

  /**
   * Add a response in the cache with the given key.
   *
   * @param key      the key to store the response at
   * @param response the response to store
   * @return the response
   */
  Future<CachedHttpResponse> set(CacheKey key, CachedHttpResponse response);

  /**
   * Delete a key from the cache.
   *
   * @param key the key to delete
   * @return a future so the API can be composed fluently
   */
  Future<Void> delete(CacheKey key);

  /**
   * Delete all entries from the cache.
   *
   * @return a future so the API can be composed fluently
   */
  Future<Void> flush();

  /**
   * Retrieve a cached response.
   *
   * @param key     the key to retrieve
   * @param handler a handler to receive the cached response
   * @see #get(CacheKey)
   */
  default void get(CacheKey key, Handler<AsyncResult<CachedHttpResponse>> handler) {
    get(key).onComplete(handler);
  }

  /**
   * Add a response in the cache with the given key.
   *
   * @param key      the key to store the response at
   * @param response the response to store
   * @param handler  a handler to receive the stored response
   * @see #set(CacheKey, CachedHttpResponse)
   */
  default void set(CacheKey key, CachedHttpResponse response, Handler<AsyncResult<CachedHttpResponse>> handler) {
    set(key, response).onComplete(handler);
  }

  /**
   * Delete all variations of a key from the cache.
   *
   * @param key     the key to delete
   * @param handler a handler to receive the delete result
   * @see #delete(CacheKey)
   */
  default void delete(CacheKey key, Handler<AsyncResult<Void>> handler) {
    delete(key).onComplete(handler);
  }

  /**
   * Delete all entries from the cache.
   *
   * @param handler a handler to receive the flush result.
   * @see #flush()
   */
  default void flush(Handler<AsyncResult<Void>> handler) {
    flush().onComplete(handler);
  }
}
