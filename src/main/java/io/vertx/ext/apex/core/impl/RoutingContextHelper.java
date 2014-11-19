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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.apex.core.RoutingContext;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RoutingContextHelper {

  public static final String ROUTING_CONTEXT_KEY = RoutingContext.class.getName();

  public static RoutingContext getContext() {
    Context ctx = Vertx.currentContext();
    if (ctx == null) {
      throw new IllegalStateException("You are not in a Vert.x context");
    }
    RoutingContext rc = ctx.get(ROUTING_CONTEXT_KEY);
    if (rc == null) {
      throw new IllegalStateException("You are not in a Handler<RoutingContext>");
    }
    return rc;
  }

  public static void setOnContext(RoutingContext rc) {
    Context ctx = Vertx.currentContext();
    if (rc == null) {
      ctx.remove(ROUTING_CONTEXT_KEY);
    } else {
      ctx.put(ROUTING_CONTEXT_KEY, rc);
    }
  }

}
