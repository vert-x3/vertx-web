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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.CookieStore;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.SessionAwareWebClient;
import io.vertx.ext.web.client.WebClient;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public class SessionAwareWebClientImpl implements SessionAwareWebClient {
  private WebClient webclient;
  private CaseInsensitiveHeaders headers;
  private CookieStore cookieStore;

  public SessionAwareWebClientImpl(WebClient webClient, CookieStore cookieStore) {
    this.webclient = webClient;
    this.cookieStore = cookieStore;
  }

  private HttpRequest<Buffer> wrapRequest(HttpRequest<Buffer> req) {
    SessionAwareHttpRequestImpl request = new SessionAwareHttpRequestImpl((HttpRequestImpl<Buffer>) req, cookieStore);
    request.prepare(headers);
    return request;
  }
  
  public CookieStore getCookieStore() {
    return cookieStore;
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
    return wrapRequest(webclient.request(method, port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
    return wrapRequest(webclient.request(method, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
    return wrapRequest(webclient.request(method, requestURI));
  }

  @Override
  public HttpRequest<Buffer> request(HttpMethod method, RequestOptions options) {
    return wrapRequest(webclient.request(method, options));
  }

  @Override
  public HttpRequest<Buffer> requestAbs(HttpMethod method, String absoluteURI) {
    return wrapRequest(webclient.requestAbs(method, absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> get(String requestURI) {
    return wrapRequest(webclient.get(requestURI));
  }

  @Override
  public HttpRequest<Buffer> get(int port, String host, String requestURI) {
    return wrapRequest(webclient.get(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> get(String host, String requestURI) {
    return wrapRequest(webclient.get(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> getAbs(String absoluteURI) {
    return wrapRequest(webclient.getAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> post(String requestURI) {
    return wrapRequest(webclient.post(requestURI));
  }

  @Override
  public HttpRequest<Buffer> post(int port, String host, String requestURI) {
    return wrapRequest(webclient.post(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> post(String host, String requestURI) {
    return wrapRequest(webclient.post(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> postAbs(String absoluteURI) {
    return wrapRequest(webclient.postAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> put(String requestURI) {
    return wrapRequest(webclient.put(requestURI));
  }

  @Override
  public HttpRequest<Buffer> put(int port, String host, String requestURI) {
    return wrapRequest(webclient.put(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> put(String host, String requestURI) {
    return wrapRequest(webclient.put(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> putAbs(String absoluteURI) {
    return wrapRequest(webclient.putAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> delete(String requestURI) {
    return wrapRequest(webclient.delete(requestURI));
  }

  @Override
  public HttpRequest<Buffer> delete(int port, String host, String requestURI) {
    return wrapRequest(webclient.delete(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> delete(String host, String requestURI) {
    return wrapRequest(webclient.delete(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> deleteAbs(String absoluteURI) {
    return wrapRequest(webclient.deleteAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> patch(String requestURI) {
    return wrapRequest(webclient.patch(requestURI));
  }

  @Override
  public HttpRequest<Buffer> patch(int port, String host, String requestURI) {
    return wrapRequest(webclient.patch(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> patch(String host, String requestURI) {
    return wrapRequest(webclient.patch(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> patchAbs(String absoluteURI) {
    return wrapRequest(webclient.patchAbs(absoluteURI));
  }

  @Override
  public HttpRequest<Buffer> head(String requestURI) {
    return wrapRequest(webclient.head(requestURI));
  }

  @Override
  public HttpRequest<Buffer> head(int port, String host, String requestURI) {
    return wrapRequest(webclient.head(port, host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> head(String host, String requestURI) {
    return wrapRequest(webclient.head(host, requestURI));
  }

  @Override
  public HttpRequest<Buffer> headAbs(String absoluteURI) {
    return wrapRequest(webclient.headAbs(absoluteURI));
  }

  @Override
  public void close() {
    webclient.close();
  }

  private CaseInsensitiveHeaders headers() {
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
