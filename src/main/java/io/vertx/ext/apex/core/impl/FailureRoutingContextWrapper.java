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
public class FailureRoutingContextWrapper extends RoutingContextWrapper {

  private FailureRoutingContext inner;

  public FailureRoutingContextWrapper(HttpServerRequest request, Iterator<RouteImpl> iter,
                                      FailureRoutingContext inner) {
    super(request, iter, inner);
    this.inner = inner;
  }

  @Override
  public Throwable failure() {
    return inner.failure();
  }

  @Override
  public int statusCode() {
    return inner.statusCode();
  }
}
