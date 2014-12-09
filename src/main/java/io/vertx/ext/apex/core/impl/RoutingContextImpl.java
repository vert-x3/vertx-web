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

package io.vertx.ext.apex.core.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.core.Cookie;
import io.vertx.ext.apex.addons.FileUpload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RoutingContextImpl extends RoutingContextImplBase {

  private static final Logger log = LoggerFactory.getLogger(RoutingContextImpl.class);

  private final RouterImpl router;
  private final Map<String, Object> data = new HashMap<>();
  private List<Handler<Void>> headersEndHandlers;
  private List<Handler<Void>> bodyEndHandlers;
  private Throwable failure;
  private int statusCode = -1;
  private boolean handled;
  private boolean prevHandled;
  private String normalisedPath;
  // We use Cookie as the key too so we can return keySet in cookies() without copying
  private Map<Cookie, Cookie> cookies;
  private Buffer body;
  private Set<FileUpload> fileUploads;

  public RoutingContextImpl(String mountPoint, RouterImpl router, HttpServerRequest request, Iterator<RouteImpl> iter) {
    super(mountPoint, request, iter);
    this.router = router;
  }

  @Override
  public HttpServerRequest request() {
    return request;
  }

  @Override
  public HttpServerResponse response() {
    return request.response();
  }

  @Override
  public Throwable failure() {
    return failure;
  }

  @Override
  public int statusCode() {
    return statusCode;
  }

  @Override
  public boolean failed() {
    return failure != null || statusCode != -1;
  }

  @Override
  public void next() {
    if (!iterateNext()) {
      checkHandleNoMatch();
    }
  }

  private void checkHandleNoMatch() {
    if (!handled) {
      // Got to end of route chain but nothing matched
      if (failed()) {
        // Send back FAILURE
        unhandledFailure(statusCode, failure, router);
      } else {
        // Send back default 404
        response().setStatusCode(404);
        response().end(DEFAULT_404);
      }
    }
  }

  @Override
  public void fail(int statusCode) {
    this.statusCode = statusCode;
    doFail();
  }

  @Override
  public void fail(Throwable t) {
    this.failure = t;
    doFail();
  }

  @Override
  public void put(String key, Object obj) {
    data.put(key, obj);
  }

  @Override
  public void setHandled(boolean handled) {
    this.prevHandled = this.handled;
    this.handled = handled;
  }

  // Revert to previous value of handled - basically
  // saying "hasn't been handled by current handler"
  @Override
  public void unhandled() {
    this.handled = prevHandled;
  }

  @Override
  public Vertx vertx() {
    return router.vertx();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    Object obj = data.get(key);
    return (T)obj;
  }

  @Override
  public Map<String, Object> contextData() {
    return data;
  }

  @Override
  public void addHeadersEndHandler(Handler<Void> handler) {
    getHeadersEndHandlers().add(handler);
  }

  @Override
  public boolean removeHeadersEndHandler(Handler<Void> handler) {
    return getHeadersEndHandlers().remove(handler);
  }

  @Override
  public void addBodyEndHandler(Handler<Void> handler) {
    getBodyEndHandlers().add(handler);
  }

  @Override
  public boolean removeBodyEndHandler(Handler<Void> handler) {
    return getBodyEndHandlers().remove(handler);
  }

  @Override
  public String normalisedPath() {
    if (normalisedPath == null) {
      normalisedPath = Utils.normalisePath(request.path());
    }
    return normalisedPath;
  }

  @Override
  public Cookie getCookie(String name) {
    return cookiesMap().get(new LookupCookie(name));
  }

  @Override
  public void addCookie(Cookie cookie) {
    cookiesMap().put(cookie, cookie);
  }

  @Override
  public Cookie removeCookie(String name) {
    return cookiesMap().remove(new LookupCookie(name));
  }

  @Override
  public int cookieCount() {
    return cookiesMap().size();
  }

  @Override
  public Set<Cookie> cookies() {
    return cookiesMap().keySet();
  }

  @Override
  public String getBodyAsString() {
    return body != null ? body.toString() : null;
  }

  @Override
  public String getBodyAsString(String encoding) {
    return body != null ? body.toString(encoding) : null;
  }

  @Override
  public JsonObject getBodyAsJson() {
    return body != null ? new JsonObject(body.toString()) : null;
  }

  @Override
  public Buffer getBody() {
    return body;
  }

  @Override
  public void setBody(Buffer body) {
    this.body = body;
  }

  @Override
  public Set<FileUpload> fileUploads() {
    return getFileUploads();
  }

  private Map<Cookie, Cookie> cookiesMap() {
    if (cookies == null) {
      cookies = new HashMap<>();
    }
    return cookies;
  }

  private Set<FileUpload> getFileUploads() {
    if (fileUploads == null) {
      fileUploads = new HashSet<>();
    }
    return fileUploads;
  }

  private void doFail() {
    handled = prevHandled = false;
    this.iter = router.iterator();
    next();
  }

  private List<Handler<Void>> getHeadersEndHandlers() {
    if (headersEndHandlers == null) {
      headersEndHandlers = new ArrayList<>();
      response().headersEndHandler(v -> headersEndHandlers.forEach(handler -> handler.handle(null)));
    }
    return headersEndHandlers;
  }

  private List<Handler<Void>> getBodyEndHandlers() {
    if (bodyEndHandlers == null) {
      bodyEndHandlers = new ArrayList<>();
      response().bodyEndHandler(v -> bodyEndHandlers.forEach(handler -> handler.handle(null)));
    }
    return bodyEndHandlers;
  }

  private static final String DEFAULT_404 =
    "<html><body><h1>Resource not found</h1></body></html>";

}
