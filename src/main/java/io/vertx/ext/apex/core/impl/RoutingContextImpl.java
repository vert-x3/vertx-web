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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
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
  private final JsonObject data = new JsonObject();
  private int matchCount;

  RoutingContextImpl(RouterImpl router, RoutingContextImpl parent, Iterator<RouteImpl> iter) {
    this(router, parent.request, parent, iter, parent.contextData, null);
  }

  RoutingContextImpl(RouterImpl router, HttpServerRequest request, Iterator<RouteImpl> iter) {
    this(router, request, null, iter, new HashMap<>(), null);
  }

  RoutingContextImpl(RouterImpl router, RoutingContextImpl ctx, Iterator<RouteImpl> iter, Throwable t) {
    this(router, ctx.request, ctx.parent, iter, ctx.contextData, t);
  }

  private RoutingContextImpl(RouterImpl router, HttpServerRequest request, RoutingContextImpl parent,
                             Iterator<RouteImpl> iter, Map<String, Object> contextData, Throwable failure) {
    this.router = router;
    this.request = request;
    this.parent = parent;
    this.iter = iter;
    this.contextData = contextData;
    this.failure = failure;
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
  public void next() {
    boolean failed = failure != null;

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
          router.handleFailure(t, this);
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
          // Send back default 500
          response().setStatusCode(500);
          response().end(DEFAULT_500);
          log.error("Unhandled failure in route", failure);
        } else {
          // Send back default 404
          response().setStatusCode(404);
          response().end(DEFAULT_404);
        }
      }
    }
  }

  @Override
  public JsonObject data() {
    return data;
  }

  private RoutingContextImpl getTopMostContext() {
    if (parent == null) {
      return this;
    } else {
      return parent.getTopMostContext();
    }
  }

  private static final String DEFAULT_404 =
    "<html><body><h1>Resource not found</h1></body></html>";

  private static final String DEFAULT_500 =
    "<html><body><h1>Ooops! Something went wrong</h1></body></html>";

}
