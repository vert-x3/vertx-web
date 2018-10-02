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

import java.util.List;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.client.CookieStore;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public class SessionAwareHttpRequestImpl implements HttpRequest<Buffer> {
  private HttpRequestImpl<Buffer> request;
  private CookieStore cookieStore;
  private MultiMap headers;

  public SessionAwareHttpRequestImpl(HttpRequestImpl<Buffer> request, CookieStore cookieStore, CaseInsensitiveHeaders clientHeaders) {
    this.request = request;
    this.cookieStore = cookieStore;
    if (clientHeaders != null) {
      this.headers = new CaseInsensitiveHeaders().addAll(clientHeaders);
    }
  }

  protected void prepare() {
    // we need to reset the headers at every "send" because cookies can be changed,
    // either by the server (that sent new ones) or by the user.
    if (request.headers != null) {
      request.headers().clear();
    }
    if (headers != null) {
      request.headers().addAll(headers);
    }

    String domain = request.virtualHost;
    if (domain == null) {
      domain = request.host;
    }
    
    String uri = request.uri;
    int pos = uri.indexOf('?');
    if (pos > -1) {
      uri = uri.substring(0, pos);
    }

    Iterable<Cookie> cookies = cookieStore.get(request.ssl, domain, uri);
    for (Cookie c : cookies) {
      request.headers().add("cookie", ClientCookieEncoder.STRICT.encode(c));
    }
  }

  @Override
  public HttpRequest<Buffer> method(HttpMethod value) {
    return request.method(value);
  }

  @Override
  public HttpRequest<Buffer> port(int value) {
    request.port(value);
    return this;
  }

  @Override
  public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
    return request.as(responseCodec);
  }

  @Override
  public HttpRequest<Buffer> host(String value) {
    request.host(value);
    return this;
  }

  @Override
  public HttpRequest<Buffer> virtualHost(String value) {
    request.virtualHost(value);
    return this;
  }

  @Override
  public HttpRequest<Buffer> uri(String value) {
    request.uri(value);
    return this;
  }

  @Override
  public HttpRequest<Buffer> putHeader(String name, String value) {
    headers().set(name, value);
    return this;
  }

  @Override
  public MultiMap headers() {
    if (headers == null) {
      headers = new CaseInsensitiveHeaders();
    }
    return headers;
  }

  @Override
  public HttpRequest<Buffer> ssl(boolean value) {
    request.ssl(value);
    return this;
  }

  @Override
  public HttpRequest<Buffer> timeout(long value) {
    request.timeout(value);
    return this;
  }

  @Override
  public HttpRequest<Buffer> addQueryParam(String paramName, String paramValue) {
    request.addQueryParam(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<Buffer> setQueryParam(String paramName, String paramValue) {
    request.setQueryParam(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<Buffer> followRedirects(boolean value) {
    request.followRedirects(value);
    return this;
  }

  @Override
  public MultiMap queryParams() {
    return request.queryParams();
  }

  @Override
  public HttpRequest<Buffer> copy() {
    request.copy();
    return this;
  }

  @Override
  public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    prepare();
    request.sendStream(body, cookieHandler(handler));
  }

  @Override
  public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    prepare();
    request.sendBuffer(body, cookieHandler(handler));
  }

  @Override
  public void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    prepare();
    request.sendJsonObject(body, cookieHandler(handler));
  }

  @Override
  public void sendJson(Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    prepare();
    request.sendJson(body, cookieHandler(handler));
  }

  @Override
  public void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    prepare();
    request.sendForm(body, cookieHandler(handler));
  }

  @Override
  public void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    prepare();
    request.sendMultipartForm(body, cookieHandler(handler));
  }

  @Override
  public void send(Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    prepare();
    request.send(cookieHandler(handler));
  }

  private Handler<AsyncResult<HttpResponse<Buffer>>> cookieHandler(Handler<AsyncResult<HttpResponse<Buffer>>> wrapped) {
    return ar -> {
      if (ar.succeeded()) {
        List<String> cookieHeaders = ar.result().cookies();
        if (cookieHeaders == null) {
          wrapped.handle(ar);
          return;
        }
        cookieHeaders.forEach(header -> {
          Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
          if (cookie != null) {
            if (cookie.domain() == null) {
              // Set the domain if missing, because we need to send cookies
              // only to the domains we received them from.
              cookie.setDomain(request.virtualHost != null ? request.virtualHost : request.host);
            }
            if (cookieStore instanceof InternalCookieStore) {
              ((InternalCookieStore) cookieStore).put(cookie);
            } else {
              cookieStore.put(cookie.name(), cookie.value(), cookie.domain(), cookie.path(), cookie.maxAge(), cookie.isSecure());
            }
          }
        });
      }
      wrapped.handle(ar);
    };
  }

}
