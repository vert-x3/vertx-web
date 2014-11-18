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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.core.FailureRoutingContext;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RoutingContextImpl implements RoutingContext, FailureRoutingContext {

  private static final Logger log = LoggerFactory.getLogger(RoutingContextImpl.class);

  private final RouterImpl router;
  private final HttpServerRequest request;
  private final RoutingContextImpl parent;
  private final Iterator<RouteImpl> iter;
  private final Map<String, Object> contextData;
  private final Throwable failure;
  private final int statusCode;
  private final Map<String, Object> data = new HashMap<>();
  private int matchCount;

  RoutingContextImpl(RouterImpl router, RoutingContextImpl parent, Iterator<RouteImpl> iter) {
    this(router, parent.request, parent, iter, parent.contextData, null, -1);
  }

  RoutingContextImpl(RouterImpl router, HttpServerRequest request, Iterator<RouteImpl> iter) {
    this(router, request, null, iter, new HashMap<>(), null, -1);
  }

  RoutingContextImpl(RouterImpl router, RoutingContextImpl ctx, Iterator<RouteImpl> iter, Throwable t,
                     int errorCode) {
    this(router, ctx.request, ctx.parent, iter, ctx.contextData, t, errorCode);
  }

  private RoutingContextImpl(RouterImpl router, HttpServerRequest request, RoutingContextImpl parent,
                             Iterator<RouteImpl> iter, Map<String, Object> contextData, Throwable failure,
                             int statusCode) {
    this.router = router;
    this.request = request;
    this.parent = parent;
    this.iter = iter;
    this.contextData = contextData;
    this.failure = failure;
    this.statusCode = statusCode;
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
  public void next() {
    boolean failed = failure != null || statusCode != -1;
    setOnContext(this);
    try {
      RoutingContextImpl topmost = getTopMostContext();

      while (iter.hasNext()) {
        RouteImpl route = iter.next();
        if (route.matches(request, failed)) {
          topmost.matchCount++;
          try {
            if (failed) {
              route.handleFailure(this);
            } else {
              route.handleContext(this);
            }
          } catch (Throwable t) {
            router.handleFailure(t, -1, this);
          }
          return;
        }
      }

      if (parent != null) {
        if (matchCount == 0) {
          // Nothing was matched in this context, but there's a parent, so decrement the match count
          // as it was previously incremented for this sub router
          topmost.matchCount--;
        }
        parent.next();
      } else {
        if (topmost.matchCount == 0) {
          // Got to end of route chain but nothing matched
          if (failed) {
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
    } finally {
      setOnContext(null);
    }
  }

  @Override
  public void fail(int statusCode) {
    router.handleFailure(null, statusCode, this);
  }

  @Override
  public void put(String key, Object obj) {
    data.put(key, obj);
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

  private RoutingContextImpl getTopMostContext() {
    if (parent == null) {
      return this;
    } else {
      return parent.getTopMostContext();
    }
  }

  private void setOnContext(RoutingContext rc) {
    Context ctx = Vertx.currentContext();
    if (rc == null) {
      ctx.remove(ROUTING_CONTEXT_KEY);
    } else {
      ctx.put(ROUTING_CONTEXT_KEY, rc);
    }
  }

  private static final String DEFAULT_404 =
    "<html><body><h1>Resource not found</h1></body></html>";

  private static final String UNHANDLED_FAILURE =
    "<html><body><h1>Ooops! Something went wrong</h1></body></html>";

}
