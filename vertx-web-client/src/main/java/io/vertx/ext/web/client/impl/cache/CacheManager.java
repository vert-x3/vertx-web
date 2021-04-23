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
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.cache.CacheAdapter;
import io.vertx.ext.web.client.impl.HttpRequestImpl;

/**
 * HTTP cache manager to process requests and responses and either cache, or reply from cache.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CacheManager {

  private final CacheAdapter cacheAdapter;

  public CacheManager(CacheAdapter cacheAdapter) {
    this.cacheAdapter = cacheAdapter;
  }

  public Future<HttpResponse<Buffer>> processRequest(HttpRequest<?> request) {
    return cacheAdapter
      .get(new CacheKey(request))
      .compose(resp -> handleCacheResult((HttpRequestImpl<?>) request, resp));
  }

  public Future<Void> processResponse(HttpRequest<?> request, HttpResponse<?> response) {
    switch (response.statusCode()) {
      case 200:
      case 301:
      case 404:
        return cacheResponse(request, response);
      default:
        // Response is not cacheable, do nothing
        return Future.succeededFuture();
    }
  }

  private Future<HttpResponse<Buffer>> handleCacheResult(HttpRequestImpl<?> request, CachedHttpResponse response) {
    if (response == null) {
      return Future.failedFuture("http cache miss");
    }

    if (response.isFresh()) {
      CacheControl cacheControl = response.getCacheControl();

      if (cacheControl.isVarying()) {
        return handleVaryingCache(request, response);
      } else {
        return Future.succeededFuture(response.rehydrate());
      }
    } else {
      // TODO: This should be forwarded with If-None-Match and the cache updated accordingly
      // We could also add support for stale-while-revalidate and stale-if-error here if desired
      return Future.failedFuture("http cache miss");
    }
  }

  private Future<HttpResponse<Buffer>> handleVaryingCache(HttpRequestImpl<?> request, CachedHttpResponse response) {
    // TODO: Vary based cache, but for now we don't so fail
    return Future.failedFuture("Vary is not yet supported");
  }

  private Future<Void> cacheResponse(HttpRequest<?> request, HttpResponse<?> response) {
    CacheControl cacheControl = CacheControl.parse(response.headers());

    if (cacheControl.isCacheable()) {
      CacheKey key = new CacheKey(request);
      return cacheAdapter.set(key, CachedHttpResponse.create(response));
    } else {
      return Future.succeededFuture();
    }
  }
}
