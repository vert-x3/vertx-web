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

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.*;
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
import io.vertx.ext.web.multipart.MultipartForm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpContext<T> {

  private final Context context;
  private final Handler<AsyncResult<HttpResponse<T>>> handler;
  private final HttpRequestImpl request;
  private Object body;
  private String contentType;
  private Map<String, Object> attrs;
  private Iterator<Handler<HttpContext<?>>> it;
  private ClientEventType eventType;
  private HttpClientRequest clientRequest;
  private HttpClientResponse clientResponse;
  private HttpResponse<T> response;
  private Throwable failure;

  public HttpContext(Context context,
                     HttpRequest request,
                     String contentType,
                     Object body,
                     Handler<AsyncResult<HttpResponse<T>>> handler) {
    this.context = context;
    this.request = (HttpRequestImpl)request;
    this.contentType = contentType;
    this.body = body;
    this.handler = handler;
  }

  public HttpClientRequest clientRequest() {
    return clientRequest;
  }

  public HttpClientResponse clientResponse() {
    return clientResponse;
  }

  public ClientEventType eventType() {
    return eventType;
  }

  public HttpRequest<T> request() {
    return request;
  }

  public HttpResponse<T> response() {
    return response;
  }

  public HttpContext response(HttpResponse<T> response) {
    this.response = response;
    return this;
  }

  public String contentType() {
    return contentType;
  }

  public HttpContext contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  /**
   * @return the body to send
   */
  public Object body() {
    return body;
  }

  /**
   * Change the body to send
   * @param body the new body
   * @return a reference to this, so the API can be used fluently
   */
  public HttpContext body(Object body) {
    this.body = body;
    return this;
  }

  /**
   * @return the failure, only for {@link ClientEventType#FAILURE}
   */
  public Throwable failure() {
    return failure;
  }

  /**
   * Send the HTTP request, the context will traverse all interceptors. Any interceptor chain on the context
   * will be reset.
   */
  public void prepareRequest() {
    fire(ClientEventType.PREPARE_REQUEST);
  }

  public void sendRequest() {
    fire(ClientEventType.SEND_REQUEST);
  }

  public void receiveResponse() {
    fire(ClientEventType.RECEIVE_RESPONSE);
  }

  public void dispatchResponse() {
    fire(ClientEventType.DISPATCH_RESPONSE);
  }

  /**
   * Fail the current exchange.
   *
   * @param cause the failure cause
   * @return {@code true} if the failure can be dispatched
   */
  public boolean fail(Throwable cause) {
    if (eventType == ClientEventType.FAILURE) {
      // Already processing a failure
      return false;
    }
    failure = cause;
    fire(ClientEventType.FAILURE);
    return true;
  }

  /**
   * Call the next interceptor in the chain.
   */
  public void next() {
    if (it.hasNext()) {
      Handler<HttpContext<?>> next = it.next();
      next.handle(this);
    } else {
      exec();
    }
  }

  private void fire(ClientEventType type) {
    eventType = type;
    it = request.client.interceptors.iterator();
    next();
  }

  private void exec() {
    switch (eventType) {
      case PREPARE_REQUEST:
        handlePrepareRequest();
        break;
      case SEND_REQUEST:
        handleSendRequest();
        break;
      case RECEIVE_RESPONSE:
        handleReceiveResponse();
        break;
      case DISPATCH_RESPONSE:
        handleDispatchResponse();
        break;
      case FAILURE:
        handleFailure();
        break;
    }
  }

  private void handleFailure() {
    handler.handle(Future.failedFuture(failure));
  }

  private void handleDispatchResponse() {
    handler.handle(Future.succeededFuture(response));
  }

  private void handlePrepareRequest() {
    HttpClientRequest req;
    String requestURI;
    if (request.queryParams() != null && request.queryParams().size() > 0) {
      QueryStringEncoder enc = new QueryStringEncoder(request.uri);
      request.queryParams().forEach(param -> enc.addParam(param.getKey(), param.getValue()));
      requestURI = enc.toString();
    } else {
      requestURI = request.uri;
    }
    int port = request.port;
    String host = request.host;
    if (request.ssl != request.options.isSsl()) {
      req = request.client.client.request(request.method, new RequestOptions().setSsl(request.ssl).setHost(host).setPort
        (port)
        .setURI
          (requestURI));
    } else {
      if (request.protocol != null && !request.protocol.equals("http") && !request.protocol.equals("https")) {
        // we have to create an abs url again to parse it in HttpClient
        try {
          URI uri = new URI(request.protocol, null, host, port, requestURI, null, null);
          req = request.client.client.requestAbs(request.method, uri.toString());
        } catch (URISyntaxException ex) {
          fail(ex);
          return;
        }
      } else {
        req = request.client.client.request(request.method, port, host, requestURI);
      }
    }
    if (request.virtualHost != null) {
      String virtalHost = request.virtualHost;
      if (port != 80) {
        virtalHost += ":" + port;
      }
      req.setHost(virtalHost);
    }
    req.setFollowRedirects(request.followRedirects);
    if (request.headers != null) {
      req.headers().addAll(request.headers);
    }
    clientRequest = req;
    sendRequest();
  }

  private void handleReceiveResponse() {
    HttpClientResponse resp = clientResponse;
    Context context = Vertx.currentContext();
    Future<HttpResponse<T>> fut = Future.future();
    fut.setHandler(r -> {
      // We are running on a context (the HTTP client mandates it)
      context.runOnContext(v -> {
        if (r.succeeded()) {
          response = r.result();
          dispatchResponse();
        } else {
          fail(r.cause());
        }
      });
    });
    resp.exceptionHandler(err -> {
      if (!fut.isComplete()) {
        fut.fail(err);
      }
    });
    ((BodyCodec<T>)request.codec).create(ar2 -> {
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
              fut.complete(new HttpResponseImpl<T>(
                resp.version(),
                resp.statusCode(),
                resp.statusMessage(),
                resp.headers(),
                resp.trailers(),
                resp.cookies(),
                stream.result().result()));
            } else {
              fut.fail(stream.result().cause());
            }
          }
        });
        Pump responsePump = Pump.pump(resp, stream);
        responsePump.start();
      } else {
        fail(ar2.cause());
      }
    });
  }

  private void handleSendRequest() {
    Future<HttpClientResponse> responseFuture = Future.<HttpClientResponse>future().setHandler(ar -> {
      if (ar.succeeded()) {
        HttpClientResponse resp = ar.result();
        resp.pause();
        clientResponse = resp;
        receiveResponse();
      } else {
        fail(ar.cause());
      }
    });
    HttpClientRequest req = clientRequest;
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
      if (body instanceof MultiMap) {
        MultipartForm parts = MultipartForm.create();
        MultiMap attributes = (MultiMap) body;
        for (Map.Entry<String, String> attribute : attributes) {
          parts.attribute(attribute.getKey(), attribute.getValue());
        }
        body = parts;
      }
      if (body instanceof MultipartForm) {
        MultipartFormUpload multipartForm;
        try {
          boolean multipart = "multipart/form-data".equals(contentType);
          multipartForm = new MultipartFormUpload(context,  (MultipartForm) this.body, multipart);
          this.body = multipartForm;
        } catch (Exception e) {
          responseFuture.tryFail(e);
          return;
        }
        request.headers().addAll(multipartForm.headers());
        for (String headerName : request.headers().names()) {
          req.putHeader(headerName, request.headers().get(headerName));
        }
        multipartForm.run();
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
          // Notify before closing the connection otherwise the future could be failed with connection closed exception
          responseFuture.tryFail(err);
          req.reset();
        });
        stream.endHandler(v -> {
          req.exceptionHandler(responseFuture::tryFail);
          req.end();
          pump.stop();
        });
        pump.start();
        stream.resume();
      } else {
        Buffer buffer;
        if (body instanceof Buffer) {
          buffer = (Buffer) body;
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
