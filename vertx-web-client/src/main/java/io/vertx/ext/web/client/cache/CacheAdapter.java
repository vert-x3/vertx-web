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
package io.vertx.ext.web.client.cache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.client.impl.cache.CacheKey;
import io.vertx.ext.web.client.impl.cache.CachedHttpResponse;

/**
 * An API to store and retrieve HTTP responses.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public interface CacheAdapter {

  Future<CachedHttpResponse> get(CacheKey key);

  Future<CachedHttpResponse> set(CacheKey key, CachedHttpResponse response);

  Future<Void> delete(CacheKey key);

  Future<Void> flush();

  default void get(CacheKey key, Handler<AsyncResult<CachedHttpResponse>> handler) {
    get(key).onComplete(handler);
  }

  default void set(CacheKey key, CachedHttpResponse response, Handler<AsyncResult<CachedHttpResponse>> handler) {
    set(key, response).onComplete(handler);
  }

  default void delete(CacheKey key, Handler<AsyncResult<Void>> handler) {
    delete(key).onComplete(handler);
  }

  default void flush(Handler<AsyncResult<Void>> handler) {
    flush().onComplete(handler);
  }
}
