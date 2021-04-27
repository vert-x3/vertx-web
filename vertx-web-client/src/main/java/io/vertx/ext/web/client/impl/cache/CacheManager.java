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

import io.netty.handler.codec.DateFormatter;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.cache.CacheStore;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import java.util.Date;

/**
 * HTTP cache manager to process requests and responses and either cache, or reply from cache.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CacheManager {

  private final CacheStore cacheStore;
  private final CachingWebClientOptions options;

  public CacheManager(CacheStore cacheStore, CachingWebClientOptions options) {
    this.cacheStore = cacheStore;
    this.options = options;
  }

  public Future<HttpResponse<Buffer>> processRequest(HttpRequest<?> request) {
    return cacheStore
      .get(new CacheKey(request))
      .compose(resp -> respondFromCache((HttpRequestImpl<?>) request, resp));
  }

  public Future<HttpResponse<Buffer>> processResponse(HttpRequest<?> request, HttpResponse<Buffer> response) {
    if (options.cachedStatusCodes().contains(response.statusCode())) {
      return cacheResponse(request, response).map(response);
    } else {
      return Future.succeededFuture(response); // Response is not cacheable, do nothing
    }
  }

  private Future<HttpResponse<Buffer>> respondFromCache(HttpRequestImpl<?> request, CachedHttpResponse response) {
    if (response == null) {
      return Future.failedFuture("http cache miss");
    }

    if (response.isFresh()) {
      CacheControl cacheControl = response.getCacheControl();

      if (cacheControl.isVarying()) {
        return handleVaryingCache(request, response);
      } else {
        HttpResponse<Buffer> result = response.rehydrate();
        result.headers().set(HttpHeaders.AGE, DateFormatter.format(new Date(response.age())));
        return Future.succeededFuture(result);
      }
    } else {
      // TODO: This should be forwarded with If-None-Match and the cache updated accordingly
      // We could also add support for stale-while-revalidate and stale-if-error here if desired
      return Future.failedFuture("http cache miss");
    }
  }

  private Future<HttpResponse<Buffer>> handleVaryingCache(HttpRequestImpl<?> request, CachedHttpResponse response) {
    if (response.getVary().matchesRequest(request)) {
      return Future.succeededFuture(response.rehydrate());
    } else {
      return Future.failedFuture("matching variation not found");
    }
  }

  private Future<Void> cacheResponse(HttpRequest<?> request, HttpResponse<Buffer> response) {
    CacheControl cacheControl = CacheControl.parse(response.headers());

    if (!cacheControl.isCacheable()) {
      return Future.succeededFuture();
    }

    if (cacheControl.isPrivate() && !options.isPrivateCachingEnabled()) {
      // Configuration says don't cache this response
      return Future.succeededFuture();
    }

    CacheKey key = new CacheKey(request);
    CachedHttpResponse cachedResponse = CachedHttpResponse.wrap(response);

    return cacheStore.set(key, cachedResponse).mapEmpty();
  }
}
