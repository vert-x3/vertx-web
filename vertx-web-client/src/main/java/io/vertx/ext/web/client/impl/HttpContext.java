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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.codec.spi.BodyStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpContext {

  private final Handler<AsyncResult<HttpResponse<Object>>> responseHandler;
  private final HttpRequestImpl request;
  private final Object body;
  private String contentType;
  private Map<String, Object> attrs;
  private Handler<AsyncResult<HttpResponse<Object>>> currentResponseHandler;
  private Iterator<Handler<HttpContext>> it;

  public HttpContext(HttpRequest request,
                     String contentType,
                     Object body,
                     Handler<AsyncResult<HttpResponse<Object>>> responseHandler) {
    this.request = (HttpRequestImpl)request;
    this.contentType = contentType;
    this.body = body;
    this.responseHandler = responseHandler;
  }

  /**
   * Send the HTTP request, the context will traverse all interceptors. Any interceptor chain on the context
   * will be reset.
   */
  public void interceptAndSend() {
    it = request.client.interceptors.iterator();
    currentResponseHandler = responseHandler;
    next();
  }

  public HttpRequest request() {
    return request;
  }

  public String contentType() {
    return contentType;
  }

  public Object body() {
    return body;
  }

  public Handler<AsyncResult<HttpResponse<Object>>> getResponseHandler() {
    return currentResponseHandler;
  }

  public void setResponseHandler(Handler<AsyncResult<HttpResponse<Object>>> responseHandler) {
    this.currentResponseHandler = responseHandler;
  }

  /**
   * Call the next interceptor in the chain or send the request when the end of the chain is reached.
   */
  public void next() {
    if (it.hasNext()) {
      Handler<HttpContext> next = it.next();
      next.handle(this);
    } else {
      sendRequest();
    }
  }

  private void sendRequest() {
    Future<HttpClientResponse> responseFuture = Future.<HttpClientResponse>future().setHandler(ar -> {
      Context context = Vertx.currentContext();
      if (ar.succeeded()) {
        HttpClientResponse resp = ar.result();
        Future<HttpResponse<Object>> fut = Future.future();
        fut.setHandler(r -> {
          // We are running on a context (the HTTP client mandates it)
          context.runOnContext(v -> currentResponseHandler.handle(r));
        });
        resp.exceptionHandler(err -> {
          if (!fut.isComplete()) {
            fut.fail(err);
          }
        });
        resp.pause();
        ((BodyCodec<Object>)request.codec).create(ar2 -> {
          resp.resume();
          if (ar2.succeeded()) {
            BodyStream<Object> stream = ar2.result();
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
            currentResponseHandler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        currentResponseHandler.handle(Future.failedFuture(ar.cause()));
      }
    });

    HttpClientRequest req;
    String requestURI;
    if (request.queryParams() != null && request.queryParams().size() > 0) {
      QueryStringEncoder enc = new QueryStringEncoder(request.uri);
      request.queryParams().forEach(param -> enc.addParam(param.getKey(), param.getValue()));
      requestURI = enc.toString();
    } else {
      requestURI = request.uri;
    }
    if (request.ssl != request.options.isSsl()) {
      req = request.client.client.request(request.method, new RequestOptions().setSsl(request.ssl).setHost(request.host).setPort
              (request.port)
              .setURI
                      (requestURI));
    } else {
      if (request.protocol != null && !request.protocol.equals("http") && !request.protocol.equals("https")) {
        // we have to create an abs url again to parse it in HttpClient
        try {
          URI uri = new URI(request.protocol, null, request.host, request.port, requestURI, null, null);
          req = request.client.client.requestAbs(request.method, uri.toString());
        } catch (URISyntaxException ex) {
          currentResponseHandler.handle(Future.failedFuture(ex));
          return;
        }
      } else {
        req = request.client.client.request(request.method, request.port, request.host, requestURI);
      }
    }
    req.setFollowRedirects(request.followRedirects);
    if (request.headers != null) {
      req.headers().addAll(request.headers);
    }
    req.handler(responseFuture::tryComplete);
    if (request.timeout > 0) {
      req.setTimeout(request.timeout);
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
        if (request.headers == null || !request.headers.contains(HttpHeaders.CONTENT_LENGTH)) {
          req.setChunked(true);
        }
        Pump pump = Pump.pump(stream, req);
        req.exceptionHandler(err -> {
          pump.stop();
          stream.endHandler(null);
          stream.resume();
          responseFuture.tryFail(err);
        });
        stream.exceptionHandler(err -> {
          req.reset();
          responseFuture.tryFail(err);
        });
        stream.endHandler(v -> {
          req.exceptionHandler(responseFuture::tryFail);
          req.end();
          pump.stop();
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
        req.exceptionHandler(responseFuture::tryFail);
        req.end(buffer);
      }
    } else {
      req.exceptionHandler(responseFuture::tryFail);
      req.end();
    }
  }

  public <T> T get(String key) {
    return attrs != null ? (T) attrs.get(key) : null;
  }

  public HttpContext set(String key, Object value) {
    if (value == null) {
      if (attrs != null) {
        attrs.remove(key);
      }
    } else {
      if (attrs == null) {
        attrs = new HashMap<>();
      }
      attrs.put(key, value);
    }
    return this;
  }
}
