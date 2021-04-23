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
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.impl.cache.CacheManager;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CachingWebClientImpl extends WebClientBase {

  private final CacheManager cacheManager;
  private final CachingWebClientOptions options;

  public CachingWebClientImpl(HttpClient client, CacheManager cacheManager, CachingWebClientOptions options) {
    super(client, options);
    this.cacheManager = cacheManager;
    this.options = options;
  }

  public CachingWebClientImpl(WebClientBase webClient, CacheManager cacheManager, CachingWebClientOptions options) {
    super(webClient);
    this.cacheManager = cacheManager;
    this.options = options;
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
    return request(method, (SocketAddress) null, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI) {
    HttpRequest<Buffer> delegate = super.request(method, serverAddress, requestURI);
    return CachedHttpRequestImpl.wrap(delegate, cacheManager, options);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, RequestOptions requestOptions) {
    return request(method, null, requestOptions);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions requestOptions) {
    HttpRequest<Buffer> delegate = super.request(method, serverAddress, requestOptions);
    return CachedHttpRequestImpl.wrap(delegate, cacheManager, options);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
    return request(method, null, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI) {
    HttpRequest<Buffer> delegate = super.request(method, serverAddress, host, requestURI);
    return CachedHttpRequestImpl.wrap(delegate, cacheManager, options);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
    return request(method, null, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI) {
    HttpRequest<Buffer> delegate = super.request(method, serverAddress, port, host, requestURI);
    return CachedHttpRequestImpl.wrap(delegate, cacheManager, options);
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, String surl) {
    return requestAbs(method, null, surl);
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String surl) {
    HttpRequest<Buffer> delegate = super.requestAbs(method, serverAddress, surl);
    return CachedHttpRequestImpl.wrap(delegate, cacheManager, options);
  }
}
