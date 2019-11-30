/*
 * Copyright 2019 Red Hat, Inc.
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
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.*;

/**
 * This class encapsulates the router state, all mutations are atomic and return a new state with the mutation.
 * <p>
 * This class is thread-safe
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
final class RouterState {

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

  private final RouterImpl router;

  private final Set<RouteImpl> routes;
  private final int orderSequence;
  private final Map<Integer, Handler<RoutingContext>> errorHandlers;
  private final Handler<Router> modifiedHandler;

  public RouterState(RouterImpl router, Set<RouteImpl> routes, int orderSequence, Map<Integer, Handler<RoutingContext>> errorHandlers, Handler<Router> modifiedHandler) {
    this.router = router;
    this.routes = routes;
    this.orderSequence = orderSequence;
    this.errorHandlers = errorHandlers;
    this.modifiedHandler = modifiedHandler;
  }

  public RouterState(RouterImpl router) {
    this(
      router,
      null,
      0,
      null,
      null);
  }

  public RouterImpl router() {
    return router;
  }

  public Set<RouteImpl> getRoutes() {
    if (routes == null) {
      return Collections.emptySet();
    }
    return routes;
  }

  RouterState setRoutes(Set<RouteImpl> routes) {
    RouterState newState = new RouterState(
      this.router,
      new TreeSet<>(routeComparator),
      this.orderSequence,
      this.errorHandlers,
      this.modifiedHandler);

    newState.routes.addAll(routes);
    return newState;
  }

  RouterState addRoute(RouteImpl route) {
    Set<RouteImpl> routes = new TreeSet<>(routeComparator);
    if (this.routes != null) {
      routes.addAll(this.routes);
    }
    routes.add(route);

    return new RouterState(
      this.router,
      routes,
      this.orderSequence,
      this.errorHandlers,
      this.modifiedHandler);
  }

  RouterState clearRoutes() {
    return new RouterState(
      this.router,
      new TreeSet<>(routeComparator),
      this.orderSequence,
      this.errorHandlers,
      this.modifiedHandler);
  }

  RouterState removeRoute(RouteImpl route) {
    Set<RouteImpl> routes = new TreeSet<>(routeComparator);
    if (this.routes != null) {
      routes.addAll(this.routes);
    }
    routes.remove(route);

    return new RouterState(
      this.router,
      routes,
      this.orderSequence,
      this.errorHandlers,
      this.modifiedHandler);
  }

  public int getOrderSequence() {
    return orderSequence;
  }

  RouterState incrementOrderSequence() {
    return new RouterState(
      this.router,
      this.routes,
      this.orderSequence + 1,
      this.errorHandlers,
      this.modifiedHandler);
  }

  RouterState setOrderSequence(int orderSequence) {
    return new RouterState(
      this.router,
      this.routes,
      orderSequence,
      this.errorHandlers,
      this.modifiedHandler);
  }

  public Map<Integer, Handler<RoutingContext>> getErrorHandlers() {
    return errorHandlers;
  }

  RouterState setErrorHandlers(Map<Integer, Handler<RoutingContext>> errorHandlers) {
    return new RouterState(
      this.router,
      this.routes,
      this.orderSequence,
      errorHandlers,
      this.modifiedHandler);
  }

  Handler<RoutingContext> getErrorHandler(int errorCode) {
    if (errorHandlers != null) {
      return errorHandlers.get(errorCode);
    }
    return null;
  }

  RouterState putErrorHandler(int errorCode, Handler<RoutingContext> errorHandler) {
    RouterState newState = new RouterState(
      this.router,
      this.routes,
      this.orderSequence,
      this.errorHandlers == null ? new HashMap<>() : new HashMap<>(errorHandlers),
      this.modifiedHandler);

    newState.errorHandlers.put(errorCode, errorHandler);
    return newState;
  }

  public Handler<Router> getModifiedHandler() {
    return modifiedHandler;
  }

  public RouterState setModifiedHandler(Handler<Router> modifiedHandler) {
    return new RouterState(
      this.router,
      this.routes,
      this.orderSequence,
      this.errorHandlers,
      modifiedHandler);
  }

  @Override
  public String toString() {
    return "RouterState{" +
      "routes=" + routes +
      ", orderSequence=" + orderSequence +
      ", errorHandlers=" + errorHandlers +
      ", modifiedHandler=" + modifiedHandler +
      '}';
  }
}
