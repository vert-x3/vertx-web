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
import io.vertx.core.http.impl.HttpClientImpl;
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
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpContext<T> {

  private final Context context;
  private final Handler<AsyncResult<HttpResponse<T>>> handler;
  private final HttpClientImpl client;
  private final List<Handler<HttpContext<?>>> interceptors;
  private HttpRequestImpl<T> request;
  private Object body;
  private String contentType;
  private Map<String, Object> attrs;
  private Iterator<Handler<HttpContext<?>>> it;
  private ClientPhase phase;
  private HttpClientRequest clientRequest;
  private HttpClientResponse clientResponse;
  private HttpResponse<T> response;
  private Throwable failure;
  private int redirects;

  HttpContext(Context context, HttpClientImpl client, List<Handler<HttpContext<?>>> interceptors, Handler<AsyncResult<HttpResponse<T>>> handler) {
    this.context = context;
    this.handler = handler;
    this.client = client;
    this.interceptors = interceptors;
  }

  /**
   * @return the underlying client request, only available during {@link ClientPhase#SEND_REQUEST} and after
   */
  public HttpClientRequest clientRequest() {
    return clientRequest;
  }

  /**
   * @return the underlying client request, only available during {@link ClientPhase#RECEIVE_RESPONSE} and after
   */
  public HttpClientResponse clientResponse() {
    return clientResponse;
  }

  /**
   * @return the current event type
   */
  public ClientPhase phase() {
    return phase;
  }

  /**
   * @return the current request object
   */
  public HttpRequest<T> request() {
    return request;
  }

  /**
   * @return the current response object, only available during {@link ClientPhase#DISPATCH_RESPONSE}
   */
  public HttpResponse<T> response() {
    return response;
  }

  public HttpContext<T> response(HttpResponse<T> response) {
    this.response = response;
    return this;
  }

  /**
   * @return the number of followed redirects, this value is initialized to {@code 0} during the prepare phase
   */
  public int redirects() {
    return redirects;
  }

  /**
   * Set the number of followed redirects.
   *
   * @param redirects the new value
   * @return a reference to this, so the API can be used fluently
   */
  public HttpContext<T> redirects(int redirects) {
    this.redirects = redirects;
    return this;
  }

  /**
   * @return the request content type
   */
  public String contentType() {
    return contentType;
  }

  /**
   * @return the body to send
   */
  public Object body() {
    return body;
  }

  /**
   * @return the failure, only for {@link ClientPhase#FAILURE}
   */
  public Throwable failure() {
    return failure;
  }

  /**
   * Prepare the HTTP request, this executes the {@link ClientPhase#PREPARE_REQUEST} phase:
   * <ul>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Execute the {@link ClientPhase#SEND_REQUEST} phase</li>
   * </ul>
   */
  public void prepareRequest(HttpRequest<T> request, String contentType, Object body) {
    this.request = (HttpRequestImpl<T>) request;
    this.contentType = contentType;
    this.body = body;
    fire(ClientPhase.PREPARE_REQUEST);
  }

  /**
   * Send the HTTP request, this executes the {@link ClientPhase#SEND_REQUEST} phase:
   * <ul>
   *   <li>Create the {@link HttpClientRequest}</li>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Send the actual request</li>
   * </ul>
   */
  public void sendRequest(HttpClientRequest clientRequest) {
    this.clientRequest = clientRequest;
    fire(ClientPhase.SEND_REQUEST);
  }

  /**
   * Receive the HTTP response, this executes the {@link ClientPhase#RECEIVE_RESPONSE} phase:
   * <ul>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Execute the {@link ClientPhase#DISPATCH_RESPONSE} phase</li>
   * </ul>
   */
  public void receiveResponse(HttpClientResponse clientResponse) {
    int sc = clientResponse.statusCode();
    int maxRedirects = request.followRedirects ? client.getOptions().getMaxRedirects(): 0;
    if (redirects < maxRedirects && sc >= 300 && sc < 400) {
      redirects++;
      Future<HttpClientRequest> next = client.redirectHandler().apply(clientResponse);
      if (next != null) {
        next.setHandler(ar -> {
          if (ar.succeeded()) {
            sendRequest(ar.result());
          } else {
            fail(ar.cause());
          }
        });
      }
    } else {
      this.clientResponse = clientResponse;
      fire(ClientPhase.RECEIVE_RESPONSE);
    }
  }

  /**
   * Dispatch the HTTP response, this executes the {@link ClientPhase#DISPATCH_RESPONSE} phase:
   * <ul>
   *   <li>Create the {@link HttpResponse}</li>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Deliver the response to the response handler</li>
   * </ul>
   */
  public void dispatchResponse(HttpResponse<T> response) {
    this.response = response;
    fire(ClientPhase.DISPATCH_RESPONSE);
  }

  /**
   * Fail the current HTTP context, this executes the {@link ClientPhase#FAILURE} phase:
   * <ul>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Deliver the failure to the response handler</li>
   * </ul>
   *
   * @param cause the failure cause
   * @return {@code true} if the failure can be dispatched
   */
  public boolean fail(Throwable cause) {
    if (phase == ClientPhase.FAILURE) {
      // Already processing a failure
      return false;
    }
    failure = cause;
    fire(ClientPhase.FAILURE);
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
      it = null;
      execute();
    }
  }

  private void fire(ClientPhase phase) {
    this.phase = phase;
    this.it = interceptors.iterator();
    next();
  }

  private void execute() {
    switch (phase) {
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
      req = client.request(request.method, new RequestOptions().setSsl(request.ssl).setHost(host).setPort
        (port)
        .setURI
          (requestURI));
    } else {
      if (request.protocol != null && !request.protocol.equals("http") && !request.protocol.equals("https")) {
        // we have to create an abs url again to parse it in HttpClient
        try {
          URI uri = new URI(request.protocol, null, host, port, requestURI, null, null);
          req = client.requestAbs(request.method, uri.toString());
        } catch (URISyntaxException ex) {
          fail(ex);
          return;
        }
      } else {
        req = client.request(request.method, port, host, requestURI);
      }
    }
    if (request.virtualHost != null) {
      String virtalHost = request.virtualHost;
      if (port != 80) {
        virtalHost += ":" + port;
      }
      req.setHost(virtalHost);
    }
    redirects = 0;
    if (request.headers != null) {
      req.headers().addAll(request.headers);
    }
    sendRequest(req);
  }

  private void handleReceiveResponse() {
    HttpClientResponse resp = clientResponse;
    Context context = Vertx.currentContext();
    Future<HttpResponse<T>> fut = Future.future();
    fut.setHandler(r -> {
      // We are running on a context (the HTTP client mandates it)
      context.runOnContext(v -> {
        if (r.succeeded()) {
          dispatchResponse(r.result());
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
        receiveResponse(resp);
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
        for (String headerName : request.headers().names()) {
          req.putHeader(headerName, request.headers().get(headerName));
        }
        multipartForm.headers().forEach(header -> {
          req.putHeader(header.getKey(), header.getValue());
        });
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

  public HttpContext<T> set(String key, Object value) {
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
