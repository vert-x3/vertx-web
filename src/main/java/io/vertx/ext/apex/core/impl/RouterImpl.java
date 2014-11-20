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

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.apex.core.FailureRoutingContext;
import io.vertx.ext.apex.core.Route;
import io.vertx.ext.apex.core.Router;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * This class is thread-safe
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RouterImpl implements Router {

  private final Vertx vertx;
  private final Set<RouteImpl> routes =
    new ConcurrentSkipListSet<>((RouteImpl o1, RouteImpl o2) -> Integer.compare(o1.order(), o2.order()));

  public RouterImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  private final AtomicInteger orderSequence = new AtomicInteger();

  @Override
  public void accept(HttpServerRequest request) {
    new RoutingContextImpl(null, this, request, routes.iterator()).next();
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
    Route currentRoute = ctx.currentRoute();
    new RoutingContextWrapper(currentRoute.getPath(), ctx.request(), routes.iterator(), ctx).next();
  }

  @Override
  public void handleFailure(FailureRoutingContext ctx) {
    Route currentRoute = ctx.currentRoute();
    new FailureRoutingContextWrapper(currentRoute.getPath(), ctx.request(), routes.iterator(), ctx).next();
  }

  @Override
  public void mountSubRouter(String mountPoint, Router subRouter) {
    route(mountPoint).handler(subRouter::handleContext).failureHandler(subRouter::handleFailure);
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

}
