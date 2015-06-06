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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.util.Iterator;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class RoutingContextImplBase implements RoutingContext {

  private static final Logger log = LoggerFactory.getLogger(RoutingContextImplBase.class);

  protected final String mountPoint;
  protected final HttpServerRequest request;
  protected Iterator<RouteImpl> iter;
  protected RouteImpl currentRoute;

  protected RoutingContextImplBase(String mountPoint, HttpServerRequest request, Iterator<RouteImpl> iter) {
    this.mountPoint = mountPoint;
    this.request = request;
    this.iter = iter;
  }

  @Override
  public String mountPoint() {
    return mountPoint;
  }

  @Override
  public Route currentRoute() {
    return currentRoute;
  }

  protected boolean iterateNext() {
    boolean failed = failed();
    while (iter.hasNext()) {
      RouteImpl route = iter.next();
      if (route.matches(this, mountPoint(), failed)) {
        if (log.isTraceEnabled()) log.trace("Route matches: " + route);
        try {
          currentRoute = route;
          if (log.isTraceEnabled()) log.trace("Calling the " + (failed ? "failure" : "") + " handler");
          if (failed) {
            route.handleFailure(this);
          } else {
            route.handleContext(this);
          }
        } catch (Throwable t) {
          if (log.isTraceEnabled()) log.trace("Throwable thrown from handler", t);
          if (!failed) {
            if (log.isTraceEnabled()) log.trace("Failing the routing");
            fail(t);
          } else {
            // Failure in handling failure!
            if (log.isTraceEnabled()) log.trace("Failure in handling failure");
            unhandledFailure(-1, t, route.router());
          }
        } finally {
          currentRoute = null;
        }
        return true;
      }
    }
    return false;
  }


  protected void unhandledFailure(int statusCode, Throwable failure, RouterImpl router) {
    int code = statusCode != -1 ? statusCode : 500;
    response().setStatusCode(code);
    response().end(response().getStatusMessage());
    if (failure != null) {
      if (router.exceptionHandler() != null) {
        router.exceptionHandler().handle(failure);
      } else {
        log.error("Unexpected exception in route", failure);
      }
    }
  }
}
