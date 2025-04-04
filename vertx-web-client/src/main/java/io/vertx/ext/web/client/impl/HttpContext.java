/*
 * Copyright 2022 Red Hat, Inc.
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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.internal.http.HttpClientInternal;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pipe;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.spi.CacheStore;
import io.vertx.ext.web.codec.spi.BodyStream;
import io.vertx.ext.web.multipart.MultipartForm;

import java.util.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpContext<T> {

  private final HttpClientInternal client;
  private final WebClientOptions options;
  private final List<Handler<HttpContext<?>>> interceptors;
  private final ContextInternal context;
  private final PromiseInternal<HttpResponse<T>> promise;
  private HttpRequestImpl<T> request;
  private Object body;
  private String contentType;
  private Map<String, Object> attrs;
  private int interceptorIdx;
  private boolean invoking;
  private boolean invokeNext;
  private ClientPhase phase;
  private RequestOptions requestOptions;
  private HttpClientRequest clientRequest;
  private HttpClientResponse clientResponse;
  private HttpResponse<T> response;
  private Throwable failure;
  private int redirects;
  private List<String> redirectedLocations = Collections.emptyList();
  private CacheStore privateCacheStore;

  HttpContext(ContextInternal context, HttpClientInternal client, WebClientOptions options, List<Handler<HttpContext<?>>> interceptors, PromiseInternal<HttpResponse<T>> promise) {
    this.context = context;
    this.client = client;
    this.options = options;
    this.interceptors = interceptors;
    this.promise = promise;
  }

  /**
   * @return a duplicate of this context
   */
  public HttpContext<T> duplicate() {
    return new HttpContext<>(context, client, options, interceptors, promise);
  }

  public Future<HttpResponse<T>> future() {
    return promise.future();
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
   * @return the current request options
   */
  public RequestOptions requestOptions() {
    return requestOptions;
  }

  public void requestOptions(RequestOptions requestOptions) {
    this.requestOptions = requestOptions;
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
   * @return all traced redirects
   */
  public List<String> redirectedLocations() {
    return redirectedLocations;
  }

  /**
   * @return the private cache store set by {@link io.vertx.ext.web.client.WebClientSession}, or
   * null if this is not a session client.
   */
  public CacheStore privateCacheStore() {
    return privateCacheStore;
  }

  /**
   * Set the private cache store.
   *
   * @param cacheStore the cache store
   * @return a reference to this, so the API can be used fluently
   */
  public HttpContext<T> privateCacheStore(CacheStore cacheStore) {
    this.privateCacheStore = cacheStore;
    return this;
  }

  /**
   * Prepare the HTTP request, this executes the {@link ClientPhase#PREPARE_REQUEST} phase:
   * <ul>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Execute the {@link ClientPhase#CREATE_REQUEST} phase</li>
   * </ul>
   */
  public void prepareRequest(HttpRequest<T> request, String contentType, Object body) {
    this.request = (HttpRequestImpl<T>) request;
    this.contentType = contentType;
    this.body = body;
    fire(ClientPhase.PREPARE_REQUEST);
  }

  /**
   * Create the HTTP request, this executes the {@link ClientPhase#CREATE_REQUEST} phase:
   * <ul>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Create the {@link HttpClientRequest}</li>
   * </ul>
   */
  public void createRequest(RequestOptions requestOptions) {
    this.requestOptions = requestOptions;
    fire(ClientPhase.CREATE_REQUEST);
  }

  /**
   * Send the HTTP request, this executes the {@link ClientPhase#SEND_REQUEST} phase:
   * <ul>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Send the actual request</li>
   * </ul>
   */
  public void sendRequest(HttpClientRequest clientRequest) {
    this.clientRequest = clientRequest;
    fire(ClientPhase.SEND_REQUEST);
  }

  /**
   * Follow the redirect, this executes the {@link ClientPhase#FOLLOW_REDIRECT} phase:
   * <ul>
   *   <li>Traverse the interceptor chain</li>
   *   <li>Send the redirect request</li>
   * </ul>
   */
  private void handleFollowRedirect() {
    fire(ClientPhase.CREATE_REQUEST);
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
    int maxRedirects = request.followRedirects() ? client.options().getMaxRedirects() : 0;
    this.clientResponse = clientResponse;
    if (redirects < maxRedirects && sc >= 300 && sc < 400) {
      redirects++;
      Future<RequestOptions> next = client.redirectHandler().apply(clientResponse);
      if (next != null) {
        if (redirectedLocations.isEmpty()) {
          redirectedLocations = new ArrayList<>();
        }
        redirectedLocations.add(clientResponse.getHeader(HttpHeaders.LOCATION));
        next.onComplete(ar -> {
          if (ar.succeeded()) {
            RequestOptions options = ar.result();
            requestOptions = options;
            fire(ClientPhase.FOLLOW_REDIRECT);
          } else {
            fail(ar.cause());
          }
        });
        return;
      }
    }
    this.clientResponse = clientResponse;
    fire(ClientPhase.RECEIVE_RESPONSE);
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
   * Fire a client execution phase.
   *
   * When an interception phase is in progress, the current phase is interrupted and
   * a new interception phase begins.
   *
   * @param phase the phase to execute
   */
  private void fire(ClientPhase phase) {
    Objects.requireNonNull(phase);
    this.phase = phase;
    this.interceptorIdx = 0;
    if (invoking) {
      this.invokeNext = true;
    } else {
      next();
    }
  }

  /**
   * Call the next interceptor in the chain.
   */
  public void next() {
    if (invoking) {
      invokeNext = true;
    } else {
      while (interceptorIdx < interceptors.size()) {
        Handler<HttpContext<?>> interceptor = interceptors.get(interceptorIdx);
        invoking = true;
        interceptorIdx++;
        try {
          interceptor.handle(this);
        } catch (Exception e ) {
          // Internal failure => directly dispatch a failure without the interceptor stack
          // that could lead to infinite failures
          failure = e;
          invokeNext = false;
          phase = ClientPhase.FAILURE;
          break;
        } finally {
          invoking = false;
        }
        if (!invokeNext) {
          return;
        }
        invokeNext = false;
      }
      interceptorIdx = 0;
      execute();
    }
  }

  private void execute() {
    switch (phase) {
      case PREPARE_REQUEST:
        handlePrepareRequest();
        break;
      case CREATE_REQUEST:
        handleCreateRequest();
        break;
      case SEND_REQUEST:
        handleSendRequest();
        break;
      case FOLLOW_REDIRECT:
        handleFollowRedirect();
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
    HttpClientRequest req = clientRequest;
    if (req != null) {
      clientRequest = null;
      req.reset();
    }
    if (body != null) {
      if (body instanceof Pipe) {
        ((Pipe<?>)body).close();
      }
      body = null;
    }
    promise.tryFail(failure);
  }

  private void handleDispatchResponse() {
    promise.tryComplete(response);
  }

  private void handlePrepareRequest() {
    redirects = 0;
    RequestOptions requestOptions;
    try {
      requestOptions = request.buildRequestOptions();
    } catch (Exception e) {
      fail(e);
      return;
    }
    if (contentType != null) {
      String prev = requestOptions.getHeaders().get(HttpHeaders.CONTENT_TYPE);
      if (prev == null) {
        requestOptions.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
      } else {
        contentType = prev;
      }
    }
    if (body instanceof Pipe) {
      //
    } else if (body instanceof MultipartForm) {
      ClientForm form;
      try {
        boolean multipart = "multipart/form-data".equals(contentType);
        if (multipart) {
          ClientMultipartForm multipartForm = ClientMultipartForm.multipartForm();
          multipartForm.mixed(request.multipartMixed());
          form = multipartForm;
        } else {
          form = ClientForm.form();
        }
        form.charset(((MultipartForm)body).getCharset());
        ((MultipartForm)body).forEach(part -> {
          if (part.isAttribute()) {
            form.attribute(part.name(), part.value());
          } else {
            ClientMultipartForm multipartForm = (ClientMultipartForm) form;
            if (part.isText()) {
              if (part.pathname() != null) {
                multipartForm.textFileUpload(part.name(), part.filename(), part.mediaType(), part.pathname());
              } else {
                multipartForm.textFileUpload(part.name(), part.filename(), part.mediaType(), part.content());
              }
            } else {
              if (part.pathname() != null) {
                multipartForm.binaryFileUpload(part.name(), part.filename(), part.mediaType(), part.pathname());
              } else {
                multipartForm.binaryFileUpload(part.name(), part.filename(), part.mediaType(), part.content());
              }
            }
          }
        });
        this.body = form;
      } catch (Exception e) {
        fail(e);
        return;
      }
    } else if (body == null && "application/json".equals(contentType)) {
      body = Buffer.buffer("null");
    } else if (body instanceof JsonObject) {
      body = ((JsonObject) body).toBuffer();
    } else if (body != null && !(body instanceof Buffer)) {
      body = Json.encodeToBuffer(body);
    }

    if (body instanceof Buffer) {
      Buffer buffer = (Buffer) body;
      requestOptions.putHeader(HttpHeaders.CONTENT_LENGTH, "" + buffer.length());
    }

    createRequest(requestOptions);
  }

  private void handleCreateRequest() {
    client.request(requestOptions)
      .onComplete(ar1 -> {
        if (ar1.succeeded()) {
          sendRequest(ar1.result());
        } else {
          fail(ar1.cause());
        }
      });
  }

  private void handleReceiveResponse() {
    BodyStream<T> stream;
    try {
      stream = request.bodyCodec().stream();
    } catch (Exception e) {
      fail(e);
      return;
    }
    HttpClientResponse resp = clientResponse;
    resp
      .pipeTo(stream)
      .compose(v -> stream.result())
      .map(result -> new HttpResponseImpl<>(
        resp.version(),
        resp.statusCode(),
        resp.statusMessage(),
        resp.headers(),
        resp.trailers(),
        resp.cookies(),
        result,
        redirectedLocations
      )).onComplete(ar -> {
        if (ar.succeeded()) {
          dispatchResponse(ar.result());
        } else {
          fail(ar.cause());
        }
      });
  }

  private void handleSendRequest() {
    clientRequest.response().onComplete(ar -> {
      clientRequest = null;
      if (ar.succeeded()) {
        receiveResponse(ar.result().pause());
      } else {
        fail(ar.cause());
      }
    });
    doSendRequest(clientRequest);
  }

  private void doSendRequest(HttpClientRequest request) {
    Object bodyToSend = body;
    if (bodyToSend != null) {
      body = null;
      if (bodyToSend instanceof Pipe) {
        Pipe<Buffer> pipe = (Pipe<Buffer>) bodyToSend;
        if (this.request.headers == null || !this.request.headers.contains(HttpHeaders.CONTENT_LENGTH)) {
          request.setChunked(true);
        }
        pipe.endOnFailure(false);
        pipe.to(request).onComplete(ar2 -> {
          if (ar2.failed()) {
            request.reset(0L, ar2.cause());
          }
        });
      } else if (bodyToSend instanceof ClientForm) {
        request.send((ClientForm) bodyToSend);
      } else {
        Buffer buffer = (Buffer) bodyToSend;
        request.send(buffer);
      }
    } else {
      request.send();
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
