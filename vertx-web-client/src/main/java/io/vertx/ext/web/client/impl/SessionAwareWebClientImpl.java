/*
 * Copyright (c) 2011-2018 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.SessionAwareWebClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.spi.CookieStore;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public class SessionAwareWebClientImpl implements SessionAwareWebClient {
  private WebClientImpl webclient;
  private CaseInsensitiveHeaders headers;
  private CookieStore cookieStore;

  public SessionAwareWebClientImpl(WebClient webClient, CookieStore cookieStore) {
    this.webclient = (WebClientImpl) webClient;
    this.cookieStore = cookieStore;
    addInterceptor();
  }
  
  private void addInterceptor() {
    synchronized (this.webclient.interceptors) {
      boolean add = true;
      for (Handler<HttpContext<?>> h : this.webclient.interceptors) {
        if (h instanceof SessionAwareInterceptor) {
          add = false;
          break;
        }
      }
      if (add) {
        this.webclient.addInterceptor(new SessionAwareInterceptor());
      }
    }
  }

  private HttpRequest<Buffer> prepareRequest(HttpRequest<Buffer> req) {
    ((HttpRequestImpl<Buffer>) req).addOnContextCreated(context -> {
      SessionAwareInterceptor.prepareContext(context, SessionAwareWebClientImpl.this);
    });
    return req;
  }
  
  public CookieStore getCookieStore() {
    return cookieStore;
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
    return prepareRequest(webclient.request(method, port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
    return prepareRequest(webclient.request(method, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
    return prepareRequest(webclient.request(method, requestURI));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, RequestOptions options) {
    return prepareRequest(webclient.request(method, options));
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, String absoluteURI) {
    return prepareRequest(webclient.requestAbs(method, absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> get(String requestURI) {
    return prepareRequest(webclient.get(requestURI));
  }

  @Override
  public HttpRequest<Buffer> get(int port, String host, String requestURI) {
    return prepareRequest(webclient.get(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> get(String host, String requestURI) {
    return prepareRequest(webclient.get(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> getAbs(String absoluteURI) {
    return prepareRequest(webclient.getAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> post(String requestURI) {
    return prepareRequest(webclient.post(requestURI));
  }

  @Override
  public HttpRequest<Buffer> post(int port, String host, String requestURI) {
    return prepareRequest(webclient.post(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> post(String host, String requestURI) {
    return prepareRequest(webclient.post(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> postAbs(String absoluteURI) {
    return prepareRequest(webclient.postAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> put(String requestURI) {
    return prepareRequest(webclient.put(requestURI));
  }

  @Override
  public HttpRequest<Buffer> put(int port, String host, String requestURI) {
    return prepareRequest(webclient.put(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> put(String host, String requestURI) {
    return prepareRequest(webclient.put(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> putAbs(String absoluteURI) {
    return prepareRequest(webclient.putAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> delete(String requestURI) {
    return prepareRequest(webclient.delete(requestURI));
  }

  @Override
  public HttpRequest<Buffer> delete(int port, String host, String requestURI) {
    return prepareRequest(webclient.delete(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> delete(String host, String requestURI) {
    return prepareRequest(webclient.delete(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> deleteAbs(String absoluteURI) {
    return prepareRequest(webclient.deleteAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> patch(String requestURI) {
    return prepareRequest(webclient.patch(requestURI));
  }

  @Override
  public HttpRequest<Buffer> patch(int port, String host, String requestURI) {
    return prepareRequest(webclient.patch(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> patch(String host, String requestURI) {
    return prepareRequest(webclient.patch(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> patchAbs(String absoluteURI) {
    return prepareRequest(webclient.patchAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> head(String requestURI) {
    return prepareRequest(webclient.head(requestURI));
  }

  @Override
  public HttpRequest<Buffer> head(int port, String host, String requestURI) {
    return prepareRequest(webclient.head(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> head(String host, String requestURI) {
    return prepareRequest(webclient.head(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> headAbs(String absoluteURI) {
    return prepareRequest(webclient.headAbs(absoluteURI));
  }

  @Override
  public void close() {
    webclient.close();
  }

  protected CaseInsensitiveHeaders headers() {
    if (headers == null) {
      headers = new CaseInsensitiveHeaders();
    }
    return headers;
  }

  @Override
  public SessionAwareWebClient setHeader(CharSequence name, CharSequence value) {
    headers().add(name, value);
    return this;
  }

  @Override
  public SessionAwareWebClient setHeader(String name, String value) {
    headers().add(name, value);
    return this;
  }

  @Override
  public SessionAwareWebClient setHeader(CharSequence name, Iterable<CharSequence> values) {
    headers().add(name, values);
    return this;
  }

  @Override
  public SessionAwareWebClient setHeader(String name, Iterable<String> values) {
    headers().add(name, values);
    return this;
  }

  @Override
  public SessionAwareWebClient removeHeader(CharSequence name) {
    headers().remove(name);
    return this;
  }

  @Override
  public SessionAwareWebClient removeHeader(String name) {
    headers().remove(name);
    return this;
  }

}
