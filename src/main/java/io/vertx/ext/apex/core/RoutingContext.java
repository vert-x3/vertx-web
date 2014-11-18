/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.apex.core;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface RoutingContext {

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

  @CacheReturn
  HttpServerRequest request();

  @CacheReturn
  HttpServerResponse response();

  void next();

  void fail(int statusCode);

  void put(String key, Object obj);

  <T> T get(String key);

  Vertx vertx();

}
