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
import io.vertx.ext.apex.core.FailureRoutingContext;

import java.util.Iterator;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class RoutingContextImplBase implements FailureRoutingContext {

  protected final HttpServerRequest request;
  protected Iterator<RouteImpl> iter;

  protected RoutingContextImplBase(HttpServerRequest request, Iterator<RouteImpl> iter) {
    this.request = request;
    this.iter = iter;
  }

  protected boolean iterateNext() {
    boolean failed = failed();
    RoutingContextHelper.setOnContext(this);
    try {
      while (iter.hasNext()) {
        RouteImpl route = iter.next();
        if (route.matches(request, failed)) {
          boolean prevHandled = handled();
          try {
            setHandled(true);
            if (failed) {
              route.handleFailure(this);
            } else {
              route.handleContext(this);
            }
          } catch (Throwable t) {
            // Restore handled
            setHandled(prevHandled);
            fail(t);
          }
          return true;
        }
      }
    } finally {
      RoutingContextHelper.setOnContext(null);
    }
    return false;
  }


}
