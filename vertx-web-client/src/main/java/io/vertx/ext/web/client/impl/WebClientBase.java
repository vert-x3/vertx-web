/*
 * Copyright 2022 Red Hat, Inc.
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
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientInternal;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.predicate.PredicateInterceptor;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;
import io.vertx.uritemplate.ExpandOptions;
import io.vertx.uritemplate.UriTemplate;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientBase implements WebClientInternal {

  final HttpClient client;
  final WebClientOptions options;
  final List<Handler<HttpContext<?>>> interceptors;

  public WebClientBase(HttpClient client, WebClientOptions options) {

    options = new WebClientOptions(options);
    if (options.getTemplateExpandOptions() == null) {
      options.setTemplateExpandOptions(new ExpandOptions());
    }

    this.client = client;
    this.options = options;
    this.interceptors = new CopyOnWriteArrayList<>();

    // Add base interceptor
    addInterceptor(new PredicateInterceptor());
  }

  WebClientBase(WebClientBase webClient) {
    this.client = webClient.client;
    this.options = new WebClientOptions(webClient.options);
    this.interceptors = new CopyOnWriteArrayList<>(webClient.interceptors);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI) {
    return request(method, serverAddress, options.getDefaultPort(), options.getDefaultHost(), requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, UriTemplate requestURI) {
    return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), options.getDefaultPort(), options.getDefaultHost(),
      requestURI, BodyCodecImpl.BUFFER, options.isFollowRedirects(), buildProxyOptions(options), buildHeaders(options));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions requestOptions) {
    Integer port = requestOptions.getPort();
    if (port == null) {
      port = options.getDefaultPort();
    }
    String host = requestOptions.getHost();
    if (host == null) {
      host = options.getDefaultHost();
    }
    HttpRequestImpl<Buffer> request = request(method, serverAddress, port, host, requestOptions.getURI());
    request.ssl(requestOptions.isSsl());
    request.timeout(requestOptions.getTimeout());
    request.followRedirects(requestOptions.getFollowRedirects());
    ProxyOptions proxyOptions = requestOptions.getProxyOptions();
    if (proxyOptions != null) {
      request.proxy(new ProxyOptions(proxyOptions));
    }
    request.traceOperation(requestOptions.getTraceOperation());
    return requestOptions.getHeaders() == null ? request : request.putHeaders(requestOptions.getHeaders());
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI) {
    return request(method, serverAddress, options.getDefaultPort(), host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, UriTemplate requestURI) {
    return request(method, serverAddress, options.getDefaultPort(), host, requestURI);
  }

  @Override
  public HttpRequestImpl<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI) {
    return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), port, host, requestURI,
      BodyCodecImpl.BUFFER, options.isFollowRedirects(), buildProxyOptions(options), buildHeaders(options));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, UriTemplate requestURI) {
    return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), port, host, requestURI,
      BodyCodecImpl.BUFFER, options.isFollowRedirects(), buildProxyOptions(options), buildHeaders(options));
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String surl) {
    ClientUri curi;
    try {
      curi = ClientUri.parse(surl);
    } catch (URISyntaxException | MalformedURLException e) {
      throw new VertxException(e);
    }
    return new HttpRequestImpl<>(this, method, serverAddress, curi.ssl, curi.port, curi.host, curi.uri,
      BodyCodecImpl.BUFFER, options.isFollowRedirects(), buildProxyOptions(options), buildHeaders(options));
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, UriTemplate absoluteURI) {
    return new HttpRequestImpl<>(this, method, serverAddress, absoluteURI,  BodyCodecImpl.BUFFER,
      options.isFollowRedirects(), buildProxyOptions(options), buildHeaders(options));
  }

  @Override
  public WebClientInternal addInterceptor(Handler<HttpContext<?>> interceptor) {
    // If a web client is constructed using another client, interceptors could get added twice.
    if (interceptors.stream().anyMatch(i -> i.getClass() == interceptor.getClass())) {
      throw new IllegalStateException(String.format("Client already contains a %s interceptor", interceptor.getClass()));
    }
    interceptors.add(interceptor);
    return this;
  }

  @Override
  public <T> HttpContext<T> createContext(Handler<AsyncResult<HttpResponse<T>>> handler) {
    HttpClientInternal client = (HttpClientInternal) this.client;
    return new HttpContext<>(client, options, interceptors, handler);
  }

  @Override
  public void close() {
    client.close();
  }

  private static MultiMap buildHeaders(WebClientOptions options) {
    if (options.isUserAgentEnabled()) {
      return HttpHeaders.set(HttpHeaders.USER_AGENT, options.getUserAgent());
    } else {
      return HttpHeaders.headers();
    }
  }

  private static ProxyOptions buildProxyOptions(WebClientOptions options) {
    if (options.getProxyOptions() !=  null) {
      return new ProxyOptions(options.getProxyOptions());
    } else {
      return null;
    }
  }
}
