/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.webclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.webclient.BodyCodec;
import io.vertx.webclient.HttpRequest;
import io.vertx.webclient.HttpResponse;
import io.vertx.webclient.spi.BodyStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class HttpRequestImpl implements HttpRequest {

  final HttpClient client;
  HttpMethod method;
  Integer port;
  String host;
  String requestURI;
  MultiMap headers;
  long timeout = -1;

  HttpRequestImpl(HttpClient client, HttpMethod method) {
    this.client = client;
    this.method = method;
  }

  private HttpRequestImpl(HttpRequestImpl other) {
    this.client = other.client;
    this.method = other.method;
    this.port = other.port;
    this.host = other.host;
    this.timeout = other.timeout;
    this.requestURI = other.requestURI;
    this.headers = other.headers != null ? new CaseInsensitiveHeaders().addAll(other.headers) : null;
  }

  @Override
  public HttpRequest method(HttpMethod value) {
    HttpRequestImpl other = new HttpRequestImpl(this);
    other.method = value;
    return other;
  }

  @Override
  public HttpRequest port(int value) {
    HttpRequestImpl other = new HttpRequestImpl(this);
    other.port = value;
    return other;
  }

  @Override
  public HttpRequest host(String value) {
    HttpRequestImpl other = new HttpRequestImpl(this);
    other.host = value;
    return other;
  }

  @Override
  public HttpRequest requestURI(String value) {
    HttpRequestImpl other = new HttpRequestImpl(this);
    other.requestURI = value;
    return other;
  }

  @Override
  public HttpRequest putHeader(String name, String value) {
    HttpRequestImpl other = new HttpRequestImpl(this);
    if (other.headers == null) {
      other.headers = new CaseInsensitiveHeaders();
    }
    other.headers.add(name, value);
    return other;
  }

  @Override
  public HttpRequest timeout(long value) {
    HttpRequestImpl other = new HttpRequestImpl(this);
    other.timeout = value;
    return other;
  }

  @Override
  public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    perform2(null, body, BodyCodec.buffer(), handler);
  }

  @Override
  public void send(Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    perform2(null, null, BodyCodec.buffer(), handler);
  }

  @Override
  public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    perform2(null, body, BodyCodec.buffer(), handler);
  }

  @Override
  public void sendJson(Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler) {
    perform2("application/json", body, BodyCodec.buffer(), handler);
  }

  @Override
  public <R> void send(BodyCodec<R> codec, Handler<AsyncResult<HttpResponse<R>>> handler) {
    perform2(null, null, codec, handler);
  }

  private <R> void perform2(String contentType, Object body, BodyCodec<R> unmarshaller, Handler<AsyncResult<HttpResponse<R>>> handler) {
    perform(contentType, body, ar -> {
      if (ar.succeeded()) {
        HttpClientResponse resp = ar.result();
        Future<HttpResponse<R>> fut = Future.future();
        fut.setHandler(handler);
        resp.exceptionHandler(err -> {
          if (!fut.isComplete()) {
            fut.fail(err);
          }
        });
        resp.bodyHandler(buff -> {
          // Todo : do incremental with a pump
          if (!fut.isComplete()) {
            BodyStream<R> state = unmarshaller.stream();
            state.write(buff);
            state.end();
            if (state.state().succeeded()) {
              fut.complete(new HttpResponseImpl<>(resp, buff, state.state().result()));
            } else {
              fut.fail(state.state().cause());
            }
          }
        });
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private void perform(String contentType, Object body, Handler<AsyncResult<HttpClientResponse>> handler) {
    Future<HttpClientResponse> fut = Future.future();
    HttpClientRequest req = client.request(method, port, host, requestURI);
    if (headers != null) {
      req.headers().addAll(headers);
    }
    req.exceptionHandler(err -> {
      if (!fut.isComplete()) {
        fut.fail(err);
      }
    });
    req.handler(resp -> {
      if (!fut.isComplete()) {
        fut.complete(resp);
      }
    });
    if (timeout > 0) {
      req.setTimeout(timeout);
    }
    if (body != null) {
      if (contentType != null) {
        req.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
      }
      if (body instanceof ReadStream<?>) {
        ReadStream<Buffer> stream = (ReadStream<Buffer>) body;
        if (headers == null || !headers.contains(HttpHeaders.CONTENT_LENGTH)) {
          req.setChunked(true);
        }
        Pump pump = Pump.pump(stream, req);
        stream.exceptionHandler(err -> {
          req.reset();
          if (!fut.isComplete()) {
            fut.fail(err);
          }
        });
        stream.endHandler(v -> {
          pump.stop();
          req.end();
        });
        pump.start();
      } else {
        Buffer buffer = body instanceof Buffer ? (Buffer) body : Buffer.buffer(Json.encode(body));
        req.end(buffer);
      }
    } else {
      req.end();
    }
    fut.setHandler(handler);
  }
}
