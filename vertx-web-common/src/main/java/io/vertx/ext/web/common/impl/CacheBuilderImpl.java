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
package io.vertx.ext.web.common.impl;

import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.web.common.CacheBuilder;
import io.vertx.ext.web.common.CacheMissHandler;

/**
 * Implementation of the CacheBuilder API
 */
public class CacheBuilderImpl<K, V> implements CacheBuilder<K, V> {

  private final ContextInternal context;
  private final AsyncMap<K, V> store;

  private final CacheMissHandler<K, V> loader;
  private final long ttl;

  public CacheBuilderImpl(Vertx vertx, AsyncMap<K, V> store, long ttl, CacheMissHandler<K, V> loader) {
    this.context = (ContextInternal) vertx.getOrCreateContext();
    this.store = store;
    this.ttl = ttl;
    this.loader = loader;
  }

  @Override
  public CacheBuilder<K, V> evict(K k, Handler<AsyncResult<V>> completionHandler) {
    store.remove(k, completionHandler);
    return this;
  }

  @Override
  public Future<V> evict(K k) {
    return store.remove(k);
  }

  @Override
  public CacheBuilder<K, V> clear(Handler<AsyncResult<Void>> completionHandler) {
    store.clear(completionHandler);
    return this;
  }

  @Override
  public Future<Void> clear() {
    return store.clear();
  }

  @Override
  public CacheBuilder<K, V> size(Handler<AsyncResult<Integer>> completionHandler) {
    store.size(completionHandler);
    return this;
  }

  @Override
  public Future<Integer> size() {
    return store.size();
  }

  @Override
  public CacheBuilder<K, V> get(K k, Handler<AsyncResult<V>> completionHandler) {
    store.get(k, get -> {
      if (get.succeeded()) {
        final V value = get.result();
        if (value == null) {
          context.runOnContext(v0 -> loader.handle(k, context.promise(p -> {
            if (p.succeeded()) {
              store.put(k, p.result(), ttl, put -> {
                if (put.succeeded()) {
                  context.runOnContext(v1 -> completionHandler.handle(Future.succeededFuture(p.result())));
                } else {
                  context.runOnContext(v1 -> completionHandler.handle(Future.failedFuture(put.cause())));
                }
              });
            }
          })));
        } else {
          completionHandler.handle(Future.succeededFuture(value));
        }
      } else {
        completionHandler.handle(Future.failedFuture(get.cause()));
      }
    });
    return this;
  }

  @Override
  public Future<V> get(K k) {
    final Promise<V> promise = context.promise();
    get(k, promise);
    return promise.future();
  }
}
