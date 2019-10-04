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
 *
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

  private Set<RouteImpl> routes;
  private int orderSequence;
  private Map<Integer, Handler<RoutingContext>> errorHandlers;
  private Handler<Router> modifiedHandler;


  public RouterState(RouterImpl router) {
    this.router = router;
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
    RouterState newState = copy();
    newState.routes = new TreeSet<>(routeComparator);
    newState.routes.addAll(routes);
    return newState;
  }

  RouterState addRoute(RouteImpl route) {
    RouterState newState = copy();
    newState.routes = new TreeSet<>(routeComparator);
    if (this.routes != null) {
      newState.routes.addAll(this.routes);
    }
    newState.routes.add(route);
    return newState;
  }

  RouterState clearRoutes() {
    RouterState newState = copy();
    newState.routes = new TreeSet<>(routeComparator);
    return newState;
  }

  RouterState removeRoute(RouteImpl route) {
    RouterState newState = copy();
    newState.routes = new TreeSet<>(routeComparator);
    newState.routes.addAll(this.routes);
    newState.routes.remove(route);
    return newState;
  }

  public int getOrderSequence() {
    return orderSequence;
  }

  RouterState incrementOrderSequence() {
    RouterState newState = copy();
    newState.orderSequence++;
    return newState;
  }

  RouterState setOrderSequence(int orderSequence) {
    RouterState newState = copy();
    newState.orderSequence = orderSequence;
    return newState;
  }

  public Map<Integer, Handler<RoutingContext>> getErrorHandlers() {
    return errorHandlers;
  }

  RouterState setErrorHandlers(Map<Integer, Handler<RoutingContext>> errorHandlers) {
    RouterState newState = copy();
    newState.errorHandlers = new HashMap<>(errorHandlers);
    return newState;
  }

  Handler<RoutingContext> getErrorHandler(int errorCode) {
    if (errorHandlers != null) {
      return errorHandlers.get(errorCode);
    }
    return null;
  }

  RouterState putErrorHandler(int errorCode, Handler<RoutingContext> errorHandler) {
    RouterState newState = copy();
    newState.errorHandlers = this.errorHandlers == null ? new HashMap<>() : new HashMap<>(errorHandlers);
    newState.errorHandlers.put(errorCode, errorHandler);
    return newState;
  }

  public Handler<Router> getModifiedHandler() {
    return modifiedHandler;
  }

  public RouterState setModifiedHandler(Handler<Router> modifiedHandler) {
    RouterState newState = copy();
    newState.modifiedHandler = modifiedHandler;
    return newState;
  }

  RouterState copy() {
    RouterState newState = new RouterState(this.router);

    newState.routes = this.routes;
    newState.orderSequence = this.orderSequence;
    newState.errorHandlers = this.errorHandlers;
    newState.modifiedHandler = this.modifiedHandler;

    return newState;
  }
}
