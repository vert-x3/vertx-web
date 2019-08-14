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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ResponseTimeHandler;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ResponseTimeHandlerImpl implements ResponseTimeHandler {

  private static final CharSequence HEADER_NAME = HttpHeaders.createOptimized("x-response-time");

  @Override
  public void handle(RoutingContext ctx) {
    long start = System.nanoTime();
    ctx.addHeadersEndHandler(v -> {
      long duration = MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS);
      ctx.response().putHeader(HEADER_NAME, duration + "ms");
    });
    ctx.next();
  }
}
