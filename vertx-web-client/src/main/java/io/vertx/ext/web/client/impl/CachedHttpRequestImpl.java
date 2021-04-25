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
package io.vertx.ext.web.client.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientCacheOptions;
import io.vertx.ext.web.client.cache.CacheAdapter;
import io.vertx.ext.web.client.impl.cache.CacheManager;
import io.vertx.ext.web.codec.BodyCodec;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CachedHttpRequestImpl extends HttpRequestImpl<Buffer> {

  private final CacheManager cacheManager;
  private final WebClientCacheOptions options;

  static CachedHttpRequestImpl wrap(HttpRequest<Buffer> request, CacheAdapter cacheAdapter) {
    return new CachedHttpRequestImpl((HttpRequestImpl<Buffer>) request, cacheAdapter);
  }

  CachedHttpRequestImpl(WebClientInternal client, HttpMethod method, SocketAddress serverAddress,
    Boolean ssl, Integer port, String host, String uri, BodyCodec<Buffer> codec,
    WebClientCacheOptions options, CacheAdapter cacheAdapter) {
    super(client, method, serverAddress, ssl, port, host, uri, codec, options);
    this.options = options;
    this.cacheManager = new CacheManager(cacheAdapter, options);
  }

  CachedHttpRequestImpl(WebClientInternal client, HttpMethod method, SocketAddress serverAddress,
      String protocol, Boolean ssl, Integer port, String host, String uri, BodyCodec<Buffer> codec,
    WebClientCacheOptions options, CacheAdapter cacheAdapter) {

    super(client, method, serverAddress, protocol, ssl, port, host, uri, codec, options);
    this.options = options;
    this.cacheManager = new CacheManager(cacheAdapter, options);
  }

  private CachedHttpRequestImpl(CachedHttpRequestImpl other) {
    super(other);
    this.options = other.options;
    this.cacheManager = other.cacheManager;
  }

  private CachedHttpRequestImpl(HttpRequestImpl<Buffer> base, CacheAdapter cacheAdapter) {
    super(base);
    this.options = new WebClientCacheOptions(base.options);
    this.cacheManager = new CacheManager(cacheAdapter, options);
  }

  @Override
  protected void send(String contentType, Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    if (options.isCachingEnabled()) {
      sendWithCache(contentType, body, handler);
    } else {
      super.send(contentType, body, handler);
    }
  }

  private void sendWithCache(String contentType, Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    cacheManager.processRequest(this).onComplete(cacheAR -> {
      if (cacheAR.succeeded()) {
        // Cache hit!
        handler.handle(Future.succeededFuture(cacheAR.result()));
      } else {
        // Cache miss
        HttpContext<Buffer> context = client.createContext(responseAR -> {
          if (responseAR.succeeded()) {
            cacheManager.processResponse(this, responseAR.result()).onComplete(handler);
          } else {
            handler.handle(responseAR);
          }
        });
        context.prepareRequest(this, contentType, body);
      }
    });
  }
}
