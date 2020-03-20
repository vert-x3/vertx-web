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

package io.vertx.ext.web.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.*;

/**
 * This class is thread-safe
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RouterImpl implements Router {

  private static final Logger log = LoggerFactory.getLogger(RouterImpl.class);

  private final Vertx vertx;

  private volatile RouterState state;

  public RouterImpl(Vertx vertx) {
    this.vertx = vertx;
    this.state = new RouterState(this);
  }

  @Override
  public void handle(HttpServerRequest request) {
    if (log.isTraceEnabled()) {
      log.trace("Router: " + System.identityHashCode(this) + " accepting request " + request.method() + " " + request.absoluteURI());
    }
    new RoutingContextImpl(null, this, request, state.getRoutes()).next();
  }

  @Override
  public synchronized Route route() {
    state = state.incrementOrderSequence();
    return new RouteImpl(this, state.getOrderSequence());
  }

  @Override
  public synchronized Route route(HttpMethod method, String path) {
    state = state.incrementOrderSequence();
    return new RouteImpl(this, state.getOrderSequence(), method, path);
  }

  @Override
  public synchronized Route route(String path) {
    state = state.incrementOrderSequence();
    return new RouteImpl(this, state.getOrderSequence(), path);
  }

  @Override
  public synchronized Route routeWithRegex(HttpMethod method, String regex) {
    state = state.incrementOrderSequence();
    return new RouteImpl(this, state.getOrderSequence(), method, regex, true);
  }

  @Override
  public synchronized Route routeWithRegex(String regex) {
    state = state.incrementOrderSequence();
    return new RouteImpl(this, state.getOrderSequence(), regex, true);
  }

  @Override
  public Route get() {
    return route().method(HttpMethod.GET);
  }

  @Override
  public Route get(String path) {
    return route(HttpMethod.GET, path);
  }

  @Override
  public Route getWithRegex(String path) {
    return route().method(HttpMethod.GET).pathRegex(path);
  }

  @Override
  public Route head() {
    return route().method(HttpMethod.HEAD);
  }

  @Override
  public Route head(String path) {
    return route(HttpMethod.HEAD, path);
  }

  @Override
  public Route headWithRegex(String path) {
    return route().method(HttpMethod.HEAD).pathRegex(path);
  }

  @Override
  public Route options() {
    return route().method(HttpMethod.OPTIONS);
  }

  @Override
  public Route options(String path) {
    return route(HttpMethod.OPTIONS, path);
  }

  @Override
  public Route optionsWithRegex(String path) {
    return route().method(HttpMethod.OPTIONS).pathRegex(path);
  }

  @Override
  public Route put() {
    return route().method(HttpMethod.PUT);
  }

  @Override
  public Route put(String path) {
    return route(HttpMethod.PUT, path);
  }

  @Override
  public Route putWithRegex(String path) {
    return route().method(HttpMethod.PUT).pathRegex(path);
  }

  @Override
  public Route post() {
    return route().method(HttpMethod.POST);
  }

  @Override
  public Route post(String path) {
    return route(HttpMethod.POST, path);
  }

  @Override
  public Route postWithRegex(String path) {
    return route().method(HttpMethod.POST).pathRegex(path);
  }

  @Override
  public Route delete() {
    return route().method(HttpMethod.DELETE);
  }

  @Override
  public Route delete(String path) {
    return route(HttpMethod.DELETE, path);
  }

  @Override
  public Route deleteWithRegex(String path) {
    return route().method(HttpMethod.DELETE).pathRegex(path);
  }

  @Override
  public Route trace() {
    return route().method(HttpMethod.TRACE);
  }

  @Override
  public Route trace(String path) {
    return route(HttpMethod.TRACE, path);
  }

  @Override
  public Route traceWithRegex(String path) {
    return route().method(HttpMethod.TRACE).pathRegex(path);
  }

  @Override
  public Route connect() {
    return route().method(HttpMethod.CONNECT);
  }

  @Override
  public Route connect(String path) {
    return route(HttpMethod.CONNECT, path);
  }

  @Override
  public Route connectWithRegex(String path) {
    return route().method(HttpMethod.CONNECT).pathRegex(path);
  }

  @Override
  public Route patch() {
    return route().method(HttpMethod.PATCH);
  }

  @Override
  public Route patch(String path) {
    return route(HttpMethod.PATCH, path);
  }

  @Override
  public Route patchWithRegex(String path) {
    return route().method(HttpMethod.PATCH).pathRegex(path);
  }

  @Override
  public List<Route> getRoutes() {
    return new ArrayList<>(state.getRoutes());
  }

  @Override
  public synchronized Router clear() {
    state = state.clearRoutes();
    return this;
  }

  @Override
  public void handleContext(RoutingContext ctx) {
    new RoutingContextWrapper(getAndCheckRoutePath(ctx), state.getRoutes(), ctx).next();
  }

  @Override
  public void handleFailure(RoutingContext ctx) {
    new RoutingContextWrapper(getAndCheckRoutePath(ctx), state.getRoutes(), ctx).next();
  }

  @Override
  public synchronized Router modifiedHandler(Handler<Router> handler) {
    if (state.getModifiedHandler() == null) {
      state = state.setModifiedHandler(handler);
    } else {
      // chain the handler
      final Handler<Router> previousHandler = state.getModifiedHandler();
      state = state.setModifiedHandler(router -> {
        try {
          previousHandler.handle(router);
        } catch (RuntimeException e) {
          log.error("Router modified notification failed", e);
        }
        // invoke the next
        try {
          handler.handle(router);
        } catch (RuntimeException e) {
          log.error("Router modified notification failed", e);
        }
      });
    }
    return this;
  }

  @Override
  public synchronized Router allowForward(AllowForwardHeaders allowForwardHeaders) {
    state = state.setAllowForward(allowForwardHeaders);
    return this;
  }

  public AllowForwardHeaders getAllowForward() {
    return state.getAllowForward();
  }

  @Override
  public Router mountSubRouter(String mountPoint, Router subRouter) {
    if (mountPoint.endsWith("*")) {
      throw new IllegalArgumentException("Don't include * when mounting a sub router");
    }

    route(mountPoint + "*")
      .subRouter(subRouter);

    return this;
  }

  @Deprecated
  @Override
  public synchronized Router exceptionHandler(Handler<Throwable> exceptionHandler) {
    if (exceptionHandler != null) {
      this.errorHandler(500, routingContext -> exceptionHandler.handle(routingContext.failure()));
    }
    return this;
  }

  @Override
  public synchronized Router errorHandler(int statusCode, Handler<RoutingContext> errorHandler) {
    state = state.putErrorHandler(statusCode, errorHandler);
    return this;
  }

  synchronized void add(RouteImpl route) {
    state = state.addRoute(route);
    // notify the listeners as the routes are changed
    if (state.getModifiedHandler() != null) {
      state.getModifiedHandler().handle(this);
    }
  }

  synchronized void remove(RouteImpl route) {
    state = state.removeRoute(route);
    // notify the listeners as the routes are changed
    if (state.getModifiedHandler() != null) {
      state.getModifiedHandler().handle(this);
    }
  }

  Vertx vertx() {
    return vertx;
  }

  Iterator<RouteImpl> iterator() {
    return state.getRoutes().iterator();
  }

  Handler<RoutingContext> getErrorHandlerByStatusCode(int statusCode) {
    return state.getErrorHandler(statusCode);
  }

  private String getAndCheckRoutePath(RoutingContext routingContext) {
    final RoutingContextImplBase ctx = (RoutingContextImplBase) routingContext;
    final Route route = ctx.currentRoute();

    if (route.getPath() != null && !route.isRegexPath()) {
      return route.getPath();
    } else {
      if (ctx.matchRest != -1) {
        if (ctx.matchNormalized) {
          return ctx.normalizedPath().substring(0, ctx.matchRest);
        } else {
          return ctx.request().path().substring(0, ctx.matchRest);
        }
      } else {
        // failure did not match
        throw new IllegalStateException("Sub routers must be mounted on paths (constant or parameterized)");
      }
    }
  }

  @Override
  public String toString() {
    return "RouterImpl@" + System.identityHashCode(this) +
      "{" +
      "vertx=" + vertx +
      ", state=" + state +
      '}';
  }
}

