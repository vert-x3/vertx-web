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

package io.vertx.ext.apex.addons.impl;

import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.addons.ResponseTime;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ResponseTimeImpl implements ResponseTime {

  @Override
  public void handle(RoutingContext ctx) {
    long start = System.currentTimeMillis();
    ctx.addHeadersEndHandler(v -> {
      long duration = System.currentTimeMillis() - start;
      ctx.response().putHeader("x-response-time", duration + "ms");
    });
    ctx.next();
  }
}
