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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class RoutingContextImplBase implements RoutingContext {

  private static final Logger LOG = LoggerFactory.getLogger(RoutingContextImplBase.class);

  private final Set<RouteImpl> routes;

  protected final String mountPoint;
  protected Iterator<RouteImpl> iter;
  protected RouteState currentRoute;
  private AtomicInteger currentRouteNextHandlerIndex;
  private AtomicInteger currentRouteNextFailureHandlerIndex;
  // When Route#matches executes, if it returns != 0 this flag is configured
  // to write the correct status code at the end of routing process
  int matchFailure;
  // the current path matched string
  int matchRest = -1;
  boolean matchNormalized;

  RoutingContextImplBase(String mountPoint, Set<RouteImpl> routes) {
    this.mountPoint = mountPoint;
    this.routes = routes;
    this.iter = routes.iterator();
    this.currentRouteNextHandlerIndex = new AtomicInteger(0);
    this.currentRouteNextFailureHandlerIndex = new AtomicInteger(0);
    resetMatchFailure();
  }

  @Override
  public String mountPoint() {
    return mountPoint;
  }

  @Override
  public Route currentRoute() {
    if (currentRoute == null) {
      return null;
    }
    return currentRoute.getRoute();
  }

  int currentRouteNextHandlerIndex() {
    return currentRouteNextHandlerIndex.intValue();
  }

  int currentRouteNextFailureHandlerIndex() {
    return currentRouteNextFailureHandlerIndex.intValue();
  }

  void restart() {
    this.iter = routes.iterator();
    currentRoute = null;
    next();
  }

  boolean iterateNext() {
    boolean failed = failed();
    if (currentRoute != null) { // Handle multiple handlers inside route object
      try {
        if (!failed && currentRoute.hasNextContextHandler(this)) {
          currentRouteNextHandlerIndex.incrementAndGet();
          resetMatchFailure();
          currentRoute.handleContext(this);
          return true;
        } else if (failed && currentRoute.hasNextFailureHandler(this)) {
          currentRouteNextFailureHandlerIndex.incrementAndGet();
          currentRoute.handleFailure(this);
          return true;
        }
      } catch (Throwable t) {
        handleInHandlerRuntimeFailure(currentRoute.getRouter(), failed, t);
        return true;
      }
    }
    // Search for more handlers
    while (iter.hasNext()) {
      // state is locked at this moment
      RouteState routeState = iter.next().state();

      currentRouteNextHandlerIndex.set(0);
      currentRouteNextFailureHandlerIndex.set(0);
      try {
        int matchResult = routeState.matches(this, mountPoint(), failed);
        if (matchResult == 0) {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Route matches: " + routeState);
          }
          resetMatchFailure();
          try {
            currentRoute = routeState;
            if (LOG.isTraceEnabled()) {
              LOG.trace("Calling the " + (failed ? "failure" : "") + " handler");
            }
            if (failed && currentRoute.hasNextFailureHandler(this)) {
              currentRouteNextFailureHandlerIndex.incrementAndGet();
              routeState.handleFailure(this);
            } else if (currentRoute.hasNextContextHandler(this)) {
              currentRouteNextHandlerIndex.incrementAndGet();
              routeState.handleContext(this);
            } else {
              continue;
            }
          } catch (Throwable t) {
            handleInHandlerRuntimeFailure(routeState.getRouter(), failed, t);
          }
          return true;
        } else if (matchResult != 404) {
          this.matchFailure = matchResult;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        if (LOG.isTraceEnabled()) {
          LOG.trace("IllegalArgumentException thrown during iteration", e);
        }
        // Failure in matches algorithm (If the exception is instanceof IllegalArgumentException probably is a QueryStringDecoder error!)
        if (!this.response().ended()) {
          unhandledFailure((e instanceof IllegalArgumentException) ? 400 : -1, e, routeState.getRouter());
        }
        return true;
      }
    }
    return false;
  }

  private void handleInHandlerRuntimeFailure(RouterImpl router, boolean failed, Throwable t) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Throwable thrown from handler", t);
    }
    if (!failed) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Failing the routing");
      }
      fail(t);
    } else {
      // Failure in handling failure!
      if (LOG.isTraceEnabled()) {
        LOG.trace("Failure in handling failure");
      }
      unhandledFailure(-1, t, router);
    }
  }


  protected void unhandledFailure(int statusCode, Throwable failure, RouterImpl router) {
    int code = statusCode != -1 ?
      statusCode :
      (failure instanceof HttpStatusException) ?
        ((HttpStatusException) failure).getStatusCode() :
        500;
    Handler<RoutingContext> errorHandler = router.getErrorHandlerByStatusCode(code);
    if (errorHandler != null) {
      try {
        errorHandler.handle(this);
      } catch (Throwable t) {
        LOG.error("Error in error handler", t);
      }
    }
    if (!response().ended() && !response().closed()) {
      try {
        response().setStatusCode(code);
      } catch (IllegalArgumentException e) {
        // means that there are invalid chars in the status message
        response()
            .setStatusMessage(HttpResponseStatus.valueOf(code).reasonPhrase())
            .setStatusCode(code);
      }
      response().end(response().getStatusMessage());
    }
  }

  private void resetMatchFailure() {
    this.matchFailure = 404;
  }
}
