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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClientCache;
import io.vertx.ext.web.client.WebClientCacheOptions;
import io.vertx.ext.web.client.cache.CacheAdapter;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class WebClientCacheAware extends WebClientBase implements WebClientCache {

  private final CacheAdapter cacheAdapter;
  private final WebClientCacheOptions options;

  public WebClientCacheAware(HttpClient client, CacheAdapter cacheAdapter, WebClientCacheOptions options) {
    super(client, options);
    this.cacheAdapter = cacheAdapter;
    this.options = options;
  }

  public WebClientCacheAware(WebClientBase webClient, CacheAdapter cacheAdapter, WebClientCacheOptions options) {
    super(webClient);
    this.cacheAdapter = cacheAdapter;
    this.options = options;
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
    return request(method, (SocketAddress) null, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI) {
    return new CachedHttpRequestImpl(this, method, serverAddress, options.isSsl(),
      options.getDefaultPort(), options.getDefaultHost(), requestURI, BodyCodecImpl.BUFFER, options, cacheAdapter);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, RequestOptions requestOptions) {
    return request(method, null, requestOptions);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions requestOptions) {
    HttpRequest<Buffer> request = new CachedHttpRequestImpl(this, method, serverAddress, requestOptions.isSsl(),
      requestOptions.getPort(), requestOptions.getHost(), requestOptions.getURI(), BodyCodecImpl.BUFFER, options, cacheAdapter);

    return requestOptions.getHeaders() == null ? request : request.putHeaders(requestOptions.getHeaders());
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
    return request(method, null, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI) {
    return new CachedHttpRequestImpl(this, method, serverAddress, options.isSsl(),
      options.getDefaultPort(), host, requestURI, BodyCodecImpl.BUFFER, options, cacheAdapter);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
    return request(method, null, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI) {
    return new CachedHttpRequestImpl(this, method, serverAddress, options.isSsl(),
      port, host, requestURI, BodyCodecImpl.BUFFER, options, cacheAdapter);
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, String surl) {
    return requestAbs(method, null, surl);
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String surl) {
    HttpRequest<Buffer> base = super.requestAbs(method, serverAddress, surl);
    return CachedHttpRequestImpl.wrap(base, cacheAdapter);
  }
}
