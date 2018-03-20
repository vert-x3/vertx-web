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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * This class is thread-safe
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RouterImpl implements Router {

  private static final Comparator<RouteImpl> routeComparator = (RouteImpl o1, RouteImpl o2) -> {
    // we keep a set of handlers ordered by its "order" property
    final int compare = Integer.compare(o1.order(), o2.order());
    // since we are defining the comparator to order the set we must be careful because the set
    // will use the comparator to compare the identify of the handlers and if they are the same order
    // are assumed to be the same comparator and therefore removed from the set.

    // if the 2 routes being compared by its order have the same order property value,
    // then do a more expensive equality check and if and only if the are the same we
    // do return 0, meaning same order and same identity.
    if (compare == 0) {
      if (o1.equals(o2)) {
        return 0;
      }
      // otherwise we return higher so if 2 routes have the same order the second one will be considered
      // higher so it is added after the first.
      return 1;
    }
    return compare;
  };


  private static final Logger log = LoggerFactory.getLogger(RouterImpl.class);

  private final Vertx vertx;
  private final Set<RouteImpl> routes = new ConcurrentSkipListSet<>(routeComparator);

  public RouterImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  private final AtomicInteger orderSequence = new AtomicInteger();
  private Handler<Throwable> exceptionHandler;

  @Override
  public void handle(HttpServerRequest request) {
    if (log.isTraceEnabled()) log.trace("Router: " + System.identityHashCode(this) +
      " accepting request " + request.method() + " " + request.absoluteURI());
    new RoutingContextImpl(null, this, request, routes).next();
  }

  @Override
  public Route route() {
    return new RouteImpl(this, orderSequence.getAndIncrement());
  }

  @Override
  public Route route(HttpMethod method, String path) {
    return new RouteImpl(this, orderSequence.getAndIncrement(), method, path);
  }

  @Override
  public Route route(String path) {
    return new RouteImpl(this, orderSequence.getAndIncrement(), path);
  }

  @Override
  public Route routeWithRegex(HttpMethod method, String regex) {
    return new RouteImpl(this, orderSequence.getAndIncrement(), method, regex, true);
  }

  @Override
  public Route routeWithRegex(String regex) {
    return new RouteImpl(this, orderSequence.getAndIncrement(), regex, true);
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
    return new ArrayList<>(routes);
  }

  @Override
  public Router clear() {
    routes.clear();
    return this;
  }

  @Override
  public void handleContext(RoutingContext ctx) {
    new RoutingContextWrapper(getAndCheckRoutePath(ctx), ctx.request(), routes, ctx).next();
  }

  @Override
  public void handleFailure(RoutingContext ctx) {
    new RoutingContextWrapper(getAndCheckRoutePath(ctx), ctx.request(), routes, ctx).next();
  }

  @Override
  public Router mountSubRouter(String mountPoint, Router subRouter) {
    if (mountPoint.endsWith("*")) {
      throw new IllegalArgumentException("Don't include * when mounting subrouter");
    }
    if (mountPoint.contains(":")) {
      throw new IllegalArgumentException("Can't use patterns in subrouter mounts");
    }
    route(mountPoint + "*").handler(subRouter::handleContext).failureHandler(subRouter::handleFailure);
    return this;
  }

  @Override
  public synchronized Router exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  void add(RouteImpl route) {
    routes.add(route);
  }

  void remove(RouteImpl route) {
    routes.remove(route);
  }

  Vertx vertx() {
    return vertx;
  }

  Iterator<RouteImpl> iterator() {
    return routes.iterator();
  }

  Handler<Throwable> exceptionHandler() {
    return exceptionHandler;
  }

  private String getAndCheckRoutePath(RoutingContext ctx) {
    Route currentRoute = ctx.currentRoute();
    String path = currentRoute.getPath();
    if (path == null) {
      throw new IllegalStateException("Sub routers must be mounted on constant paths (no regex or patterns)");
    }
    return path;
  }


}
