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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.core.impl.RoutingContextHelper;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface RoutingContext {

  public static RoutingContext getContext() {
    return RoutingContextHelper.getContext();
  }

  @CacheReturn
  HttpServerRequest request();

  @CacheReturn
  HttpServerResponse response();

  void next();

  void fail(int statusCode);

  @GenIgnore
  void fail(Throwable throwable);

  void put(String key, Object obj);

  <T> T get(String key);

  Vertx vertx();

  // Some Helper methods

  void addHeadersEndHandler(Handler<Void> handler);

  boolean removeHeadersEndHandler(Handler<Void> handler);

  void addBodyEndHandler(Handler<Void> handler);

  boolean removeBodyEndHandler(Handler<Void> handler);

  void setHandled(boolean handled);

  void unhandled();

  boolean failed();

  String mountPoint();

  Route currentRoute();

}
