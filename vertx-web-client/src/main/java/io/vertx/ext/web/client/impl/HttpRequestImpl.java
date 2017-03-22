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
package io.vertx.ext.web.client.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.spi.BodyStream;

import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class HttpRequestImpl<T> implements HttpRequest<T> {

  private final HttpClient client;
  private final WebClientOptions options;
  private MultiMap params;
  private HttpMethod method;
  private int port;
  private String host;
  private String uri;
  private MultiMap headers;
  private long timeout = -1;
  private BodyCodec<T> codec;
  private boolean followRedirects;
  private boolean ssl;

  HttpRequestImpl(HttpClient client, HttpMethod method, boolean ssl, int port, String host, String uri, BodyCodec<T> codec, WebClientOptions options) {
    this.client = client;
    this.method = method;
    this.codec = codec;
    this.port = port;
    this.host = host;
    this.uri = uri;
    this.ssl = ssl;
    this.followRedirects = options.isFollowRedirects();
    this.options = options;
    if (options.isUserAgentEnabled()) {
      headers = new CaseInsensitiveHeaders().add(HttpHeaders.USER_AGENT, options.getUserAgent());
    }
  }

  private HttpRequestImpl(HttpRequestImpl<T> other) {
    this.client = other.client;
    this.options = other.options;
    this.method = other.method;
    this.port = other.port;
    this.host = other.host;
    this.timeout = other.timeout;
    this.uri = other.uri;
    this.headers = other.headers != null ? new CaseInsensitiveHeaders().addAll(other.headers) : null;
    this.params = other.params != null ? new CaseInsensitiveHeaders().addAll(other.params) : null;
    this.codec = other.codec;
    this.followRedirects = other.followRedirects;
  }

  @Override
  public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
    codec = (BodyCodec<T>) responseCodec;
    return (HttpRequest<U>) this;
  }

  @Override
  public HttpRequest<T> method(HttpMethod value) {
    method = value;
    return this;
  }

  @Override
  public HttpRequest<T> port(int value) {
    port = value;
    return this;
  }

  @Override
  public HttpRequest<T> host(String value) {
    host = value;
    return this;
  }

  @Override
  public HttpRequest<T> uri(String value) {
    params = null;
    uri = value;
    return this;
  }

  @Override
  public HttpRequest<T> putHeader(String name, String value) {
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
  public HttpRequest<T> ssl(boolean value) {
    ssl = value;
    return this;
  }

  @Override
  public HttpRequest<T> timeout(long value) {
    timeout = value;
    return this;
  }

  @Override
  public HttpRequest<T> addQueryParam(String paramName, String paramValue) {
    queryParams().add(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> setQueryParam(String paramName, String paramValue) {
    queryParams().set(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> followRedirects(boolean value) {
    followRedirects = value;
    return this;
  }

  @Override
  public MultiMap queryParams() {
    if (params == null) {
      params = new CaseInsensitiveHeaders();
    }
    if (params.isEmpty()) {
      int idx = uri.indexOf('?');
      if (idx >= 0) {
        QueryStringDecoder dec = new QueryStringDecoder(uri);
        dec.parameters().forEach((name, value) -> params.add(name, value));
        uri = uri.substring(0, idx);
      }
    }
    return params;
  }

  @Override
  public HttpRequest<T> copy() {
    return new HttpRequestImpl<T>(this);
  }

  @Override
  public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send(null, body, handler);
  }

  @Override
  public void send(Handler<AsyncResult<HttpResponse<T>>> handler) {
    send(null, null, handler);
  }

  @Override
  public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send(null, body, handler);
  }

  @Override
  public void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send("application/json", body,handler);
  }

  @Override
  public void sendJson(Object body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send("application/json", body, handler);
  }

  @Override
  public void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send("application/x-www-form-urlencoded", body, handler);
  }

  private void send(String contentType, Object body, Handler<AsyncResult<HttpResponse<T>>> handler) {

    Future<HttpClientResponse> responseFuture = Future.<HttpClientResponse>future().setHandler(ar -> {
      if (ar.succeeded()) {
        HttpClientResponse resp = ar.result();
        Future<HttpResponse<T>> fut = Future.future();
        fut.setHandler(handler);
        resp.exceptionHandler(err -> {
          if (!fut.isComplete()) {
            fut.fail(err);
          }
        });
        resp.pause();
        codec.create(ar2 -> {
          resp.resume();
          if (ar2.succeeded()) {
            BodyStream<T> stream = ar2.result();
            stream.exceptionHandler(err -> {
              if (!fut.isComplete()) {
                fut.fail(err);
              }
            });
            resp.endHandler(v -> {
              if (!fut.isComplete()) {
                stream.end();
                if (stream.result().succeeded()) {
                  fut.complete(new HttpResponseImpl<>(resp, null, stream.result().result()));
                } else {
                  fut.fail(stream.result().cause());
                }
              }
            });
            Pump responsePump = Pump.pump(resp, stream);
            responsePump.start();
          } else {
            handler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });

    HttpClientRequest req;
    String requestURI;
    if (params != null && params.size() > 0) {
      QueryStringEncoder enc = new QueryStringEncoder(uri);
      params.forEach(param -> {
        enc.addParam(param.getKey(), param.getValue());
      });
      requestURI = enc.toString();
    } else {
      requestURI = uri;
    }
    if (ssl != options.isSsl()) {
      req = client.request(method, new RequestOptions().setSsl(ssl).setHost(host).setPort(port).setURI(requestURI));
    } else {
      req = client.request(method, port, host, requestURI);
    }
    req.setFollowRedirects(followRedirects);
    if (headers != null) {
      req.headers().addAll(headers);
    }
    req.exceptionHandler(err -> {
      if (!responseFuture.isComplete()) {
        responseFuture.fail(err);
      }
    });
    req.handler(resp -> {
      if (!responseFuture.isComplete()) {
        responseFuture.complete(resp);
      }
    });
    if (timeout > 0) {
      req.setTimeout(timeout);
    }
    if (body != null) {
      if (contentType != null) {
        String prev = req.headers().get(HttpHeaders.CONTENT_TYPE);
        if (prev == null) {
          req.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
        } else {
          contentType = prev;
        }
      }
      if (body instanceof ReadStream<?>) {
        ReadStream<Buffer> stream = (ReadStream<Buffer>) body;
        if (headers == null || !headers.contains(HttpHeaders.CONTENT_LENGTH)) {
          req.setChunked(true);
        }
        Pump pump = Pump.pump(stream, req);
        stream.exceptionHandler(err -> {
          req.reset();
          if (!responseFuture.isComplete()) {
            responseFuture.fail(err);
          }
        });
        stream.endHandler(v -> {
          pump.stop();
          req.end();
        });
        pump.start();
      } else {
        Buffer buffer;
        if (body instanceof Buffer) {
          buffer = (Buffer) body;
        } else if (body instanceof MultiMap) {
          try {
            MultiMap attributes = (MultiMap) body;
            boolean multipart = "multipart/form-data".equals(contentType);
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpMethod.POST, "/");
            HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(request, multipart);
            for (Map.Entry<String, String> attribute : attributes) {
              encoder.addBodyAttribute(attribute.getKey(), attribute.getValue());
            }
            encoder.finalizeRequest();
            for (String headerName : request.headers().names()) {
              req.putHeader(headerName, request.headers().get(headerName));
            }
            if (encoder.isChunked()) {
              buffer = Buffer.buffer();
              while (true) {
                HttpContent chunk = encoder.readChunk(new UnpooledByteBufAllocator(false));
                ByteBuf content = chunk.content();
                if (content.readableBytes() == 0) {
                  break;
                }
                buffer.appendBuffer(Buffer.buffer(content));
              }
            } else {
              ByteBuf content = request.content();
              buffer = Buffer.buffer(content);
            }
          } catch (Exception e) {
            throw new VertxException(e);
          }
        } else if (body instanceof JsonObject) {
          buffer = Buffer.buffer(((JsonObject)body).encode());
        } else {
          buffer = Buffer.buffer(Json.encode(body));
        }
        req.end(buffer);
      }
    } else {
      req.end();
    }
  }
}
