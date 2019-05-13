/*
 * Copyright 2014 Red Hat, Inc.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.*;
import io.vertx.ext.web.client.impl.predicate.PredicateInterceptor;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientBase implements WebClientInternal {

  final HttpClient client;
  final WebClientOptions options;
  private final List<Handler<HttpContext<?>>> interceptors;

  public WebClientBase(HttpClient client, WebClientOptions options) {
    this.client = client;
    this.options = new WebClientOptions(options);
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
  public HttpRequest<Buffer> get(int port, String host, String requestURI) {
    return request(HttpMethod.GET, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> get(int port, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return get(port, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> get(String requestURI) {
    return request(HttpMethod.GET, requestURI);
  }

  @Override
  public HttpRequest<Buffer> get(PathTemplate pathTemplate, PathParameters pathParameters) {
    return get(pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> get(String host, String requestURI) {
    return request(HttpMethod.GET, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> get(String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return get(host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> getAbs(String absoluteURI) {
    return requestAbs(HttpMethod.GET, absoluteURI);
  }

  @Override
  public HttpRequest<Buffer> post(String requestURI) {
    return request(HttpMethod.POST, requestURI);
  }

  @Override
  public HttpRequest<Buffer> post(PathTemplate pathTemplate, PathParameters pathParameters) {
    return post(pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> post(String host, String requestURI) {
    return request(HttpMethod.POST, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> post(String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return post(host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> post(int port, String host, String requestURI) {
    return request(HttpMethod.POST, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> post(int port, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return post(port, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> put(String requestURI) {
    return request(HttpMethod.PUT, requestURI);
  }

  @Override
  public HttpRequest<Buffer> put(PathTemplate pathTemplate, PathParameters pathParameters) {
    return put(pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> put(String host, String requestURI) {
    return request(HttpMethod.PUT, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> put(String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return put(host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> put(int port, String host, String requestURI) {
    return request(HttpMethod.PUT, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> put(int port, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return put(port, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> delete(String host, String requestURI) {
    return request(HttpMethod.DELETE, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> delete(String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return delete(host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> delete(String requestURI) {
    return request(HttpMethod.DELETE, requestURI);
  }

  @Override
  public HttpRequest<Buffer> delete(PathTemplate pathTemplate, PathParameters pathParameters) {
    return delete(pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> delete(int port, String host, String requestURI) {
    return request(HttpMethod.DELETE, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> delete(int port, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return delete(port, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> patch(String requestURI) {
    return request(HttpMethod.PATCH, requestURI);
  }

  @Override
  public HttpRequest<Buffer> patch(PathTemplate pathTemplate, PathParameters pathParameters) {
    return patch(pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> patch(String host, String requestURI) {
    return request(HttpMethod.PATCH, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> patch(String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return patch(host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> patch(int port, String host, String requestURI) {
    return request(HttpMethod.PATCH, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> patch(int port, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return patch(port, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> head(String requestURI) {
    return request(HttpMethod.HEAD, requestURI);
  }

  @Override
  public HttpRequest<Buffer> head(PathTemplate pathTemplate, PathParameters pathParameters) {
    return head(pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> head(String host, String requestURI) {
    return request(HttpMethod.HEAD, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> head(String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return head(host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> head(int port, String host, String requestURI) {
    return request(HttpMethod.HEAD, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> postAbs(String absoluteURI) {
    return requestAbs(HttpMethod.POST, absoluteURI);
  }

  @Override
  public HttpRequest<Buffer> putAbs(String absoluteURI) {
    return requestAbs(HttpMethod.PUT, absoluteURI);
  }

  @Override
  public HttpRequest<Buffer> deleteAbs(String absoluteURI) {
    return requestAbs(HttpMethod.DELETE, absoluteURI);
  }

  @Override
  public HttpRequest<Buffer> patchAbs(String absoluteURI) {
    return requestAbs(HttpMethod.PATCH, absoluteURI);
  }

  @Override
  public HttpRequest<Buffer> headAbs(String absoluteURI) {
    return requestAbs(HttpMethod.HEAD, absoluteURI);
  }

  public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
    return request(method, (SocketAddress) null, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, PathTemplate pathTemplate, PathParameters pathParameters) {
    return request(method, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI) {
    return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), options.getDefaultPort(), options.getDefaultHost(),
      requestURI, BodyCodecImpl.BUFFER, options);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, PathTemplate pathTemplate, PathParameters pathParameters) {
    return request(method, serverAddress, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, RequestOptions requestOptions) {
    return request(method, null, requestOptions);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions requestOptions) {
      HttpRequestImpl<Buffer> request = new HttpRequestImpl<>(this, method, serverAddress, requestOptions.isSsl(), requestOptions.getPort(),
      requestOptions.getHost(), requestOptions.getURI(), BodyCodecImpl.BUFFER, options);
      return requestOptions.getHeaders() == null ? request : request.putHeaders(requestOptions.getHeaders());
  }

  public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
    return request(method, null, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return request(method, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI) {
    return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), options.getDefaultPort(), host, requestURI, BodyCodecImpl.BUFFER, options);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return request(method, serverAddress, host, pathTemplate.expand(pathParameters));
  }

  public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
    return request(method, null, port, host, requestURI);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, int port, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return request(method, port, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI) {
    return new HttpRequestImpl<>(this, method, serverAddress, options.isSsl(), port, host, requestURI, BodyCodecImpl.BUFFER, options);
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, PathTemplate pathTemplate, PathParameters pathParameters) {
    return request(method, serverAddress, port, host, pathTemplate.expand(pathParameters));
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, String surl) {
    return requestAbs(method, null, surl);
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String surl) {
    // Note - parsing a URL this way is slower than specifying host, port and relativeURI
    URL url;
    try {
      url = new URL(surl);
    } catch (MalformedURLException e) {
      throw new VertxException("Invalid url: " + surl);
    }
    boolean ssl = false;
    int port = url.getPort();
    String protocol = url.getProtocol();
    if ("ftp".equals(protocol)) {
      if (port == -1) {
        port = 21;
      }
    } else {
      char chend = protocol.charAt(protocol.length() - 1);
      if (chend == 'p') {
        if (port == -1) {
          port = 80;
        }
      } else if (chend == 's'){
        ssl = true;
        if (port == -1) {
          port = 443;
        }
      }
    }
    return new HttpRequestImpl<>(this, method, serverAddress, protocol, ssl, port, url.getHost(), url.getFile(),
            BodyCodecImpl.BUFFER, options);
  }

  @Override
  public WebClientInternal addInterceptor(Handler<HttpContext<?>> interceptor) {
    interceptors.add(interceptor);
    return this;
  }

  @Override
  public <T> HttpContext<T> createContext(Handler<AsyncResult<HttpResponse<T>>> handler) {
    HttpClientImpl client = (HttpClientImpl) this.client;
    return new HttpContext<>(client, interceptors, handler);
  }

  @Override
  public void close() {
    client.close();
  }
}
