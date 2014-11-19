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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

  public RoutingContextImpl(RouterImpl router, HttpServerRequest request, Iterator<RouteImpl> iter) {
    super(request, iter);
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
    if (!handled()) {
      // Got to end of route chain but nothing matched
      if (failed()) {
        // Send back FAILURE
        int code = statusCode != -1 ? statusCode : 500;
        response().setStatusCode(code);
        response().end(UNHANDLED_FAILURE);
        if (failure != null) {
          log.error("Unexpected exception in route", failure);
        }
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
  public boolean handled() {
    return handled;
  }

  @Override
  public void setHandled(boolean handled) {
    this.prevHandled = this.handled;
    this.handled = true;
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

  private static final String UNHANDLED_FAILURE =
    "<html><body><h1>Ooops! Something went wrong</h1></body></html>";

}
