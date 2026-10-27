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
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.CachingWebClientConfig;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.spi.CacheStore;
import io.vertx.ext.web.codec.BodyCodec;
import io.netty.util.internal.StringUtil;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
  private static final String SENT_HEADERS = "cache.sent_headers";

  private final CacheStore publicCacheStore;
  private final CachingWebClientConfig options;
  private final Map<CacheVariationsKey, Set<Vary>> variationsRegistry;

  public CacheInterceptor(CacheStore store, CachingWebClientConfig options) {
    this.publicCacheStore = store;
    this.options = options;
    this.variationsRegistry = new ConcurrentHashMap<>();
  }

  @Override
  public void handle(HttpContext<?> context) {
    if (!usesBufferCodec(context)) {
      // Caching requires the raw response body, but a custom body codec decodes the response as it
      // streams and does not expose the buffer. Such requests bypass the cache entirely until the
      // cache can store and re-decode the raw body.
      context.next();
      return;
    }
    if (context.phase() == ClientPhase.SEND_REQUEST) {
      // Capture the headers actually sent. A WebClientSession applies its headers (e.g.
      // Authorization) to the client request at this phase, after this interceptor runs, so the
      // reference stored here reflects them by the time a cache decision reads it.
      context.set(SENT_HEADERS, context.clientRequest().headers());
      context.next();
      return;
    }
    if (context.get(IS_CACHE_REVALIDATION) == Boolean.TRUE) {
      // A background refresh always carries the stale response it is revalidating
      CachedHttpResponse toRevalidate = context.get(RESPONSE_TO_REVALIDATE);
      switch (context.phase()) {
        case CREATE_REQUEST:
          String etag = toRevalidate.getCacheControl().getEtag();
          if (etag != null) {
            // Make the background refresh a conditional request so the server can answer 304
            context.requestOptions().putHeader(HttpHeaders.IF_NONE_MATCH, etag);
          }
          context.next();
          break;
        case DISPATCH_RESPONSE:
          processRevalidationResponse((HttpContext<Buffer>) context, toRevalidate).onComplete(ar -> {
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
        case FAILURE:
          handleFailure((HttpContext<Buffer>) context);
          break;
        default:
          context.next();
          break;
      }
    }
  }

  private void handleFailure(HttpContext<Buffer> context) {
    CachedHttpResponse cachedResponse = context.get(RESPONSE_TO_REVALIDATE);
    if (cachedResponse != null && cachedResponse.useStaleIfError()) {
      // The revalidation failed, serve the stale response instead of the failure (RFC 5861)
      context.set(IS_CACHE_DISPATCH, true);
      context.dispatchResponse(dispatchedResponse(cachedResponse));
    } else {
      context.next();
    }
  }

  private void handleCreateRequest(HttpContext<Buffer> context) {
    RequestOptions request = context.requestOptions();
    if (options.getCachedMethods().contains(request.getMethod())) {
      Object body = context.body();
      if (body != null) {
        context.set("cache.body_fingerprint", getBodyFingerprint(body));
      }
    }

    Vary variation;

    if (!options.getCachedMethods().contains(request.getMethod()) || (variation = selectVariation(request)) == null) {
      context.next();
      return;
    }

    CacheKey key = new CacheKey(request, variation, context.get("cache.body_fingerprint"));
    Future<CachedHttpResponse> lookup;

    if (context.privateCacheStore() != null) {
      // Check the local private store first, then the public shared store when the private
      // lookup misses or fails
      lookup = context.privateCacheStore()
        .get(key)
        .transform((cached, err) -> cached != null ? Future.succeededFuture(cached) : publicCacheStore.get(key));
    } else {
      lookup = publicCacheStore.get(key);
    }

    lookup
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
    } else if (cachedResponse != null && isErrorStatus(response.statusCode()) && cachedResponse.useStaleIfError()) {
      // The server responded with an error, use the cached result (RFC 5861)
      return Future.succeededFuture(dispatchedResponse(cachedResponse));
    } else {
      // Response is not cacheable, do nothing
      return Future.succeededFuture(response);
    }
  }

  private static boolean isErrorStatus(int statusCode) {
    // RFC 5861 defines an error as a 500, 502, 503 or 504 response
    return statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
  }

  private static HttpResponse<Buffer> dispatchedResponse(CachedHttpResponse response) {
    HttpResponse<Buffer> result = response.rehydrate();
    result.headers().set(HttpHeaders.AGE, Long.toString(response.age()));
    return result;
  }

  private Optional<HttpResponse<Buffer>> respondFromCache(HttpContext<Buffer> context, CachedHttpResponse response) {
    if (response == null) {
      return Optional.empty();
    }

    HttpResponse<Buffer> result = dispatchedResponse(response);

    if (response.getCacheControl().noCache()) {
      // We must validate with the server before releasing the cached data
      markForRevalidation(context, response);
      return Optional.empty();
    } else if (response.isFresh()) {
      // Response is current, reply with it immediately
      return Optional.of(result);
    } else if (response.useStaleWhileRevalidate()) {
      // Send off a request to revalidate the cache but don't wait for a response
      HttpContext<Buffer> duplicate = context.duplicate();
      duplicate.set(IS_CACHE_REVALIDATION, true);
      duplicate.set(RESPONSE_TO_REVALIDATE, response);
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
    String etag = response.getCacheControl().getEtag();
    if (etag != null) {
      context.requestOptions().putHeader(HttpHeaders.IF_NONE_MATCH, etag);
    }
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

    boolean privateOnly = cacheControl.isPrivate();
    if (!privateOnly && isAuthorizedRequest(context) && !cacheControl.allowsAuthorizedSharedCaching()) {
      // A shared cache must not store a response to a request with an Authorization header,
      // unless a cache directive explicitly allows it (RFC 9111, section 3.5)
      privateOnly = true;
    }
    if (!privateOnly && response.headers().contains(HttpHeaders.SET_COOKIE)) {
      // Replaying a Set-Cookie header to other users would leak the cookie (RFC 9111, section 7.3)
      privateOnly = true;
    }

    if (privateOnly && context.privateCacheStore() == null) {
      return Future.succeededFuture(response);
    }

    if (cacheControl.isVarying() && !options.isVaryCachingEnabled()) {
      return Future.succeededFuture(response);
    }

    CacheVariationsKey variationsKey = new CacheVariationsKey(context.requestOptions());
    Vary variation = new Vary(request.headers(), response.headers(), options.isDecompressionEnabled());
    registerVariation(variationsKey, variation);

    String bodyFingerprint = context.get("cache.body_fingerprint");
    CacheKey key = new CacheKey(context.requestOptions(), variation, bodyFingerprint != null ? bodyFingerprint : "");
    CachedHttpResponse cachedResponse = CachedHttpResponse.wrap(response, cacheControl);

    if (privateOnly) {
      return context.privateCacheStore().set(key, cachedResponse).map(response);
    } else {
      return publicCacheStore.set(key, cachedResponse).map(response);
    }
  }

  private static boolean usesBufferCodec(HttpContext<?> context) {
    return context.request().bodyCodec() == BodyCodec.buffer();
  }

  private static boolean isAuthorizedRequest(HttpContext<?> context) {
    // Prefer the headers actually sent (they include headers applied by a WebClientSession),
    // falling back to the request options when the request has not been sent yet.
    MultiMap headers = context.get(SENT_HEADERS);
    if (headers == null) {
      headers = context.requestOptions().getHeaders();
    }
    return headers != null && headers.contains(HttpHeaders.AUTHORIZATION);
  }

  private void registerVariation(CacheVariationsKey variationsKey, Vary variation) {
    Set<Vary> existing = variationsRegistry.getOrDefault(variationsKey, Collections.emptySet());
    Set<Vary> updated = new HashSet<>(existing);

    updated.add(variation);
    variationsRegistry.put(variationsKey, updated);
  }

  private static String getBodyFingerprint(Object body) {
    Objects.requireNonNull(body, "body cannot be null");
    if (body instanceof Buffer) {
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(((Buffer) body).getBytes());
        return StringUtil.toHexString(hash);
      } catch (Exception e) {
        return body.toString();
      }
    }
    return body.toString();
  }
}
