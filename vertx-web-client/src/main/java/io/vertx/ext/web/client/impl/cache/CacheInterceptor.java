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
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.spi.CacheStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An interceptor for caching responses that operates on the {@link HttpContext}.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CacheInterceptor implements Handler<HttpContext<?>> {

  private static final String IS_CACHE_DISPATCH = "cache.dispatch";
  private static final String RESPONSE_TO_REVALIDATE = "cache.response_to_revalidate";
  private static final String IS_CACHE_REVALIDATION = "cache.revalidation";

  private final CacheStore publicCacheStore;
  private final CachingWebClientOptions options;
  private final Map<CacheVariationsKey, Set<Vary>> variationsRegistry;

  public CacheInterceptor(CacheStore store, CachingWebClientOptions options) {
    this.publicCacheStore = store;
    this.options = options;
    this.variationsRegistry = new ConcurrentHashMap<>();
  }

  @Override
  public void handle(HttpContext<?> context) {
    if (context.get(IS_CACHE_REVALIDATION) == Boolean.TRUE) {
      switch (context.phase()) {
        case DISPATCH_RESPONSE:
          processResponse((HttpContext<Buffer>) context, null).onComplete(ar -> {
            // Don't go further
          });
          break;
        default:
          context.next();
          break;
      }
    } else {
      switch (context.phase()) {
        case CREATE_REQUEST:
          handleCreateRequest((HttpContext<Buffer>) context);
          break;
        case DISPATCH_RESPONSE:
          handleDispatchResponse((HttpContext<Buffer>) context);
          break;
        default:
          context.next();
          break;
      }
    }
  }

  private void handleCreateRequest(HttpContext<Buffer> context) {
    RequestOptions request = context.requestOptions();
    Vary variation;

    if (!options.getCachedMethods().contains(request.getMethod()) || (variation = selectVariation(request)) == null) {
      context.next();
      return;
    }

    Promise<CachedHttpResponse> promise = Promise.promise();
    CacheKey key = new CacheKey(request, variation);

    if (context.privateCacheStore() != null) {
      // Check the local private store first
      context.privateCacheStore().get(key).onSuccess(cached -> {
        if (cached == null) {
          // If the local private store doesn't have the result, try the public shared store
          publicCacheStore.get(key).onComplete(promise);
        } else {
          promise.complete(cached);
        }
      });
    } else {
      publicCacheStore.get(key).onComplete(promise);
    }

    promise
      .future()
      .map(cached -> respondFromCache(context, cached))
      .onComplete(ar -> {
        if (ar.succeeded() && ar.result().isPresent()) {
          context.set(IS_CACHE_DISPATCH, true);
          context.dispatchResponse(ar.result().get());
        } else {
          context.next();
        }
      });
  }

  private void handleDispatchResponse(HttpContext<Buffer> context) {
    if (context.get(IS_CACHE_DISPATCH) == Boolean.TRUE) {
      context.next();
      return;
    }

    CachedHttpResponse responseToValidate = context.get(RESPONSE_TO_REVALIDATE);
    if (responseToValidate != null) {
      // We're revalidating a cached response
      processRevalidationResponse(context, responseToValidate).onComplete(ar -> {
        if (ar.succeeded()) {
          context.response(ar.result());
        }
        context.next();
      });
    } else {
      // We're storing a new response in cache
      processResponse(context, null).onComplete(ar -> {
        context.next();
      });
    }
  }

  private Vary selectVariation(RequestOptions request) {
    CacheVariationsKey key = new CacheVariationsKey(request);
    Set<Vary> possibleVariations = variationsRegistry.getOrDefault(key, Collections.emptySet());

    for (Vary variation : possibleVariations) {
      if (variation.matchesRequest(request)) {
        return variation;
      }
    }

    return null;
  }

  private Future<HttpResponse<Buffer>> processResponse(HttpContext<Buffer> context, CachedHttpResponse cachedResponse) {
    HttpResponse<Buffer> response = context.response();

    if (options.getCachedStatusCodes().contains(response.statusCode())) {
      // Request was successful, attempt to cache response
      return cacheResponse(context, response).map(response);
    } else if (cachedResponse != null && cachedResponse.useStaleIfError()) {
      // The response is a status code we don't know about, assume revalidate failed and use cached result
      return Future.succeededFuture(cachedResponse.rehydrate());
    } else {
      // Response is not cacheable, do nothing
      return Future.succeededFuture(response);
    }
  }

  private Optional<HttpResponse<Buffer>> respondFromCache(HttpContext<Buffer> context, CachedHttpResponse response) {
    if (response == null) {
      return Optional.empty();
    }

    HttpResponse<Buffer> result = response.rehydrate();
    result.headers().set(HttpHeaders.AGE, Long.toString(response.age()));

    if (response.getCacheControl().noCache()) {
      // We must validate with the server before releasing the cached data
      markForRevalidation(context, response);
      return Optional.empty();
    } else if (response.isFresh()) {
      // Response is current, reply with it immediately
      return Optional.of(result);
    } else if (response.useStaleWhileRevalidate()) {
      // Send off a request to revalidate the cache but don't want for a response,
      HttpContext<Buffer> duplicate = context.duplicate();
      duplicate.set(IS_CACHE_REVALIDATION, true);
      duplicate.prepareRequest(context.request(), context.contentType(), context.body());
      // Just respond immediately with the cached value.
      return Optional.of(result);
    } else {
      // Can't use the response as-is, fetch updated information before responding
      markForRevalidation(context, response);
      return Optional.empty();
    }
  }

  private void markForRevalidation(HttpContext<?> context, CachedHttpResponse response) {
    context.request().headers().set(HttpHeaders.IF_NONE_MATCH, response.getCacheControl().getEtag());
    context.set(RESPONSE_TO_REVALIDATE, response);
  }

  private Future<HttpResponse<Buffer>> processRevalidationResponse(HttpContext<Buffer> context, CachedHttpResponse cachedResponse) {
    if (context.response().statusCode() == 304) {
      // The cache returned a stale result, but server has confirmed still good. Update cache
      return cacheResponse(context, cachedResponse.rehydrate());
    } else {
      return processResponse(context, cachedResponse);
    }
  }

  private Future<HttpResponse<Buffer>> cacheResponse(HttpContext<?> context, HttpResponse<Buffer> response) {
    HttpRequest<?> request = context.request();
    CacheControl cacheControl = CacheControl.parse(response.headers());

    if (!cacheControl.isCacheable()) {
      return Future.succeededFuture(response);
    }

    if (cacheControl.isPrivate() && context.privateCacheStore() == null) {
      return Future.succeededFuture(response);
    }

    if (cacheControl.isVarying() && !options.isVaryCachingEnabled()) {
      return Future.succeededFuture(response);
    }

    CacheVariationsKey variationsKey = new CacheVariationsKey(context.requestOptions());
    Vary variation = new Vary(request.headers(), response.headers());
    registerVariation(variationsKey, variation);

    CacheKey key = new CacheKey(context.requestOptions(), variation);
    CachedHttpResponse cachedResponse = CachedHttpResponse.wrap(response, cacheControl);

    if (cacheControl.isPrivate()) {
      return context.privateCacheStore().set(key, cachedResponse).map(response);
    } else {
      return publicCacheStore.set(key, cachedResponse).map(response);
    }
  }

  private void registerVariation(CacheVariationsKey variationsKey, Vary variation) {
    Set<Vary> existing = variationsRegistry.getOrDefault(variationsKey, Collections.emptySet());
    Set<Vary> updated = new HashSet<>(existing);

    updated.add(variation);
    variationsRegistry.put(variationsKey, updated);
  }
}
