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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.impl.cache.CacheManager;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CachedHttpRequestImpl implements HttpRequest<Buffer> {

  private final HttpRequestImpl<Buffer> request;
  private final CacheManager cacheManager;
  private final CachingWebClientOptions options;

  static CachedHttpRequestImpl wrap(HttpRequest<Buffer> request, CacheManager cacheManager, CachingWebClientOptions options) {
    return new CachedHttpRequestImpl(request, cacheManager, options);
  }

  CachedHttpRequestImpl(HttpRequest<Buffer> request, CacheManager cacheManager, CachingWebClientOptions options) {
    this.request = (HttpRequestImpl<Buffer>) request;
    this.cacheManager = cacheManager;
    this.options = options;
  }

  private CachedHttpRequestImpl(CachedHttpRequestImpl other) {
    this.request = other.request;
    this.cacheManager = other.cacheManager;
    this.options = other.options;
  }

  @Override
  public HttpRequest<Buffer> method(HttpMethod value) {
    return request.method(value);
  }

  @Override
  public HttpRequest<Buffer> port(int value) {
    return request.port(value);
  }

  @Override
  public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
    return request.as(responseCodec);
  }

  @Override
  public HttpRequest<Buffer> host(String value) {
    return request.host(value);
  }

  @Override
  public HttpRequest<Buffer> virtualHost(String value) {
    return request.virtualHost(value);
  }

  @Override
  public HttpRequest<Buffer> uri(String value) {
    return request.uri(value);
  }

  @Override
  public HttpRequest<Buffer> putHeaders(MultiMap headers) {
    return request.putHeaders(headers);
  }

  @Override
  public HttpRequest<Buffer> putHeader(String name, String value) {
    return request.putHeader(name, value);
  }

  @Override
  public HttpRequest<Buffer> putHeader(String name, Iterable<String> value) {
    return request.putHeader(name, value);
  }

  @Override
  public MultiMap headers() {
    return request.headers();
  }

  @Override
  public HttpRequest<Buffer> authentication(Credentials credentials) {
    return request.authentication(credentials);
  }

  @Override
  public HttpRequest<Buffer> ssl(Boolean value) {
    return request.ssl(value);
  }

  @Override
  public HttpRequest<Buffer> timeout(long value) {
    return request.timeout(value);
  }

  @Override
  public HttpRequest<Buffer> addQueryParam(String paramName, String paramValue) {
    return addQueryParam(paramName, paramValue);
  }

  @Override
  public HttpRequest<Buffer> setQueryParam(String paramName, String paramValue) {
    return request.setQueryParam(paramName, paramValue);
  }

  @Override
  public HttpRequest<Buffer> followRedirects(boolean value) {
    return request.followRedirects(value);
  }

  @Override
  public HttpRequest<Buffer> expect(ResponsePredicate predicate) {
    return request.expect(predicate);
  }

  @Override
  public MultiMap queryParams() {
    return request.queryParams();
  }

  @Override
  public HttpRequest<Buffer> copy() {
    return new CachedHttpRequestImpl(this);
  }

  @Override
  public HttpRequest<Buffer> multipartMixed(boolean allow) {
    return request.multipartMixed(allow);
  }

  @Override
  public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    send(null, body, handler);
  }

  @Override
  public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    send(null, body, handler);
  }

  @Override
  public void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    send("application/json", body, handler);
  }

  @Override
  public void sendJson(@Nullable Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    send("application/json", body, handler);
  }

  @Override
  public void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    send("application/x-www-form-urlencoded", body, handler);
  }

  @Override
  public void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    send("multipart/form-data", body, handler);
  }

  @Override
  public void send(Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    send(null, null, handler);
  }

  private void send(String contentType, Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    if (options.isCachingEnabled()) {
      sendWithCache(contentType, body, handler);
    } else {
      request.send(contentType, body, handler);
    }
  }

  private void sendWithCache(String contentType, Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    cacheManager.processRequest(request).onComplete(cacheAR -> {
      if (cacheAR.succeeded()) {
        // Cache hit!
        handler.handle(Future.succeededFuture(cacheAR.result()));
      } else {
        // Cache miss
        HttpContext<Buffer> context = request.client.createContext(responseAR -> {
          if (responseAR.succeeded()) {
            cacheManager.processResponse(request, responseAR.result()).onComplete(handler);
          } else {
            handler.handle(responseAR);
          }
        });
        context.prepareRequest(request, contentType, body);
      }
    });
  }
}
