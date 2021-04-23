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
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.spi.CacheStore;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP cache manager to process requests and responses and either reply from, or store in a cache.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CacheManager {

  private final CacheStore cacheStore;
  private final CachingWebClientOptions options;
  private final Map<CacheVariationsKey, Set<Vary>> variationsRegistry;

  public CacheManager(CacheStore cacheStore, CachingWebClientOptions options) {
    this.cacheStore = cacheStore;
    this.options = options;
    this.variationsRegistry = new ConcurrentHashMap<>();
  }

  public Future<HttpResponse<Buffer>> processRequest(HttpRequest<Buffer> request) {
    Vary variation = selectVariation(request);
    if (variation == null) {
      return request.send().compose(resp -> cacheResponse(request, resp));
    }

    return cacheStore
      .get(new CacheKey(request, variation))
      .compose(resp -> respondFromCache((HttpRequestImpl<Buffer>) request, resp));
  }

  public Future<HttpResponse<Buffer>> processResponse(HttpRequest<Buffer> request, HttpResponse<Buffer> response) {
    return processResponse(request, response, null);
  }

  private Future<HttpResponse<Buffer>> processResponse(HttpRequest<Buffer> request, HttpResponse<Buffer> response, CachedHttpResponse cachedResponse) {
    if (options.getCachedStatusCodes().contains(response.statusCode())) {
      // Request was successful, attempt to cache response
      return cacheResponse(request, response).map(response);
    } else if (cachedResponse != null && cachedResponse.useStaleIfError()) {
      return Future.succeededFuture(cachedResponse.rehydrate());
    } else {
      // Response is not cacheable, do nothing
      return Future.succeededFuture(response);
    }
  }

  private Vary selectVariation(HttpRequest<?> request) {
    CacheVariationsKey key = new CacheVariationsKey(request);
    Set<Vary> possibleVariations = variationsRegistry.getOrDefault(key, Collections.emptySet());

    for (Vary variation : possibleVariations) {
      if (variation.matchesRequest(request)) {
        return variation;
      }
    }

    return null;
  }

  private Future<HttpResponse<Buffer>> respondFromCache(HttpRequestImpl<Buffer> request, CachedHttpResponse response) {
    if (response == null) {
      return Future.failedFuture("http cache miss");
    }

    HttpResponse<Buffer> result = response.rehydrate();
    result.headers().set(HttpHeaders.AGE, Long.toString(response.age()));

    if (response.cacheControl().noCache()) {
      // We must validate with the server before releasing the cached data
      return handleStaleCacheResult(request, response);
    } else if (response.isFresh()) {
      // Response is current, reply with it immediately
      return Future.succeededFuture(result);
    } else if (response.useStaleWhileRevalidate()) {
      // Send off a request to revalidate the cache but don't want for a response, just respond
      // immediately with the cached value.
      handleStaleCacheResult(request, response);
      return Future.succeededFuture(result);
    } else {
      // Can't use the response as-is, fetch updated information before responding
      return handleStaleCacheResult(request, response);
    }
  }

  private Future<HttpResponse<Buffer>> handleStaleCacheResult(HttpRequestImpl<Buffer> request, CachedHttpResponse response) {
    request.headers().set(HttpHeaders.IF_NONE_MATCH, response.cacheControl().getEtag());

    return request
      .send()
      .compose(updatedResponse -> processRevalidationResponse(request, updatedResponse, response))
      .recover(e -> response.useStaleIfError() ? Future.succeededFuture(response.rehydrate()) : Future.failedFuture(e));
  }

  private Future<HttpResponse<Buffer>> processRevalidationResponse(HttpRequest<Buffer> request, HttpResponse<Buffer> response, CachedHttpResponse cachedResponse) {
    if (response.statusCode() == 304) {
      // The cache returned a stale result, but server has confirmed still good. Update cache
      return cacheResponse(request, cachedResponse.rehydrate());
    } else {
      return processResponse(request, response, cachedResponse);
    }
  }

  private Future<HttpResponse<Buffer>> cacheResponse(HttpRequest<?> request, HttpResponse<Buffer> response) {
    CacheControl cacheControl = CacheControl.parse(response.headers());

    if (!cacheControl.isCacheable()) {
      return Future.succeededFuture(response);
    }

    if (cacheControl.isPrivate() && !options.isPrivateCachingEnabled()) {
      return Future.succeededFuture(response);
    }

    if (cacheControl.isVarying() && !options.isVaryCachingEnabled()) {
      return Future.succeededFuture(response);
    }

    CacheVariationsKey variationsKey = new CacheVariationsKey(request);
    Vary variation = new Vary(request.headers(), response.headers());
    registerVariation(variationsKey, variation);

    CacheKey key = new CacheKey(request, variation);
    CachedHttpResponse cachedResponse = CachedHttpResponse.wrap(request, response, cacheControl);

    return cacheStore.set(key, cachedResponse).map(response);
  }

  private void registerVariation(CacheVariationsKey variationsKey, Vary variation) {
    Set<Vary> existing = variationsRegistry.getOrDefault(variationsKey, Collections.emptySet());
    Set<Vary> updated = new HashSet<>(existing);

    updated.add(variation);
    variationsRegistry.put(variationsKey, updated);
  }
}
