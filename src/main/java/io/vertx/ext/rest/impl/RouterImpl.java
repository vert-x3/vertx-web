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

package io.vertx.ext.rest.impl;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.rest.Route;
import io.vertx.ext.rest.Router;
import io.vertx.ext.rest.RoutingContext;

import java.util.ArrayList;
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

  private final Set<RouteImpl> routes =
    new ConcurrentSkipListSet<>((RouteImpl o1, RouteImpl o2) -> Integer.compare(o1.order(), o2.order()));

  private final AtomicInteger orderSequence = new AtomicInteger();

  @Override
  public void accept(HttpServerRequest request) {
    new RoutingContextImpl(this, request, routes.iterator()).next();
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
  public void handle(RoutingContext ctx) {
    new RoutingContextImpl(this, (RoutingContextImpl)ctx, routes.iterator()).next();
  }

  void handleFailure(Throwable t, RoutingContextImpl ctx) {
    new RoutingContextImpl(this, ctx, routes.iterator(), t).next();
  }

  void add(RouteImpl route) {
    routes.add(route);
  }

  void remove(RouteImpl route) {
    routes.remove(route);
  }
}
