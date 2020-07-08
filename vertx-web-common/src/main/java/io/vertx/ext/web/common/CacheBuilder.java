/*
 * Copyright 2018 Red Hat, Inc.
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
package io.vertx.ext.web.common;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.web.common.impl.CacheBuilderImpl;

/**
 * A simple Cache builder backed by an async map.
 *
 * Caches will hold values for the configured TTL and as they are backed by an async map, they can be local or cluster
 * wide caches.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @author Paulo Lopes
 */
@VertxGen
public interface CacheBuilder<K, V> {

  /**
   * Default TTL for cached values, upon adding a value to the cache, it will remain there up to the TTL
   */
  long DEFAULT_TTL = 30_000;

  /**
   * Creates a new CacheBuilder using the default TTL.
   *
   * @param vertx the vertx instance
   * @param storage the backend storage
   * @param handler the handler to fetch entries on cache miss
   * @param <K> the key type
   * @param <V> the value type (respecting the storage types)
   *
   * @return the cache instance
   */
  static <K, V> CacheBuilder<K, V> create(Vertx vertx, AsyncMap<K, V> storage, CacheMissHandler<K, V> handler) {
    return create(vertx, storage, DEFAULT_TTL, handler);
  }

  /**
   * Creates a new CacheBuilder.
   *
   * @param vertx the vertx instance
   * @param storage the backend storage
   * @param ttl the ttl for entries in the cache
   * @param handler the handler to fetch entries on cache miss
   * @param <K> the key type
   * @param <V> the value type (respecting the storage types)
   *
   * @return the cache instance
   */
  static <K, V> CacheBuilder<K, V> create(Vertx vertx, AsyncMap<K, V> storage, long ttl, CacheMissHandler<K, V> handler) {
    return new CacheBuilderImpl<>(vertx, storage, ttl, handler);
  }

  /**
   * Get a value from the cache, on miss, the configured cache miss handler will be called to fetch and populate the
   * cache.
   *
   * @param k key
   * @param completionHandler on operation complete will contain the asynchronous result
   * @return self
   */
  @Fluent
  CacheBuilder<K, V> get(K k, Handler<AsyncResult<V>> completionHandler);

  /**
   * Get a value from the cache, on miss, the configured cache miss handler will be called to fetch and populate the
   * cache.
   *
   * @param k key
   * @return future on operation complete will contain the asynchronous result
   */
  Future<V> get(K k);

  /**
   * Evict an element from the cache.
   * @param k key
   * @param completionHandler asynchronous handler for the result of the operation
   * @return self
   */
  @Fluent
  CacheBuilder<K, V> evict(K k, Handler<AsyncResult<V>> completionHandler);

  /**
   * Evict an element from the cache.
   * @param k key
   * @return asynchronous future for the result of the operation
   */
  Future<V> evict(K k);

  /**
   * Clears the cache.
   * @param completionHandler asynchronous handler for the result of the operation
   * @return self
   */
  @Fluent
  CacheBuilder<K, V> clear(Handler<AsyncResult<Void>> completionHandler);

  /**
   * Clears the cache.
   * @return asynchronous future for the result of the operation
   */
  Future<Void> clear();

  /**
   * Returns the size of the cache.
   * @param completionHandler asynchronous handler for the result of the operation
   * @return self
   */
  @Fluent
  CacheBuilder<K, V> size(Handler<AsyncResult<Integer>> completionHandler);

  /**
   * Returns the size of the cache.
   * @return asynchronous future for the result of the operation
   */
  Future<Integer> size();
}
