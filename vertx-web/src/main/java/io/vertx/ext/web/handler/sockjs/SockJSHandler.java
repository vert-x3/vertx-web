/*
 * Copyright 2014 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.impl.SockJSImpl;

/**
 * @deprecated Use {@link SockJS} instead.
 */
@VertxGen
@Deprecated
public interface SockJSHandler extends SockJS, Handler<RoutingContext> {

  /**
   * @deprecated Use {@link SockJS#create(Vertx)}
   * Create a SockJS handler
   *
   * @param vertx  the Vert.x instance
   * @return the handler
   */
  @Deprecated
  static SockJSHandler create(Vertx vertx) {
    return new SockJSImpl(vertx, new SockJSHandlerOptions());
  }

  /**
   * @deprecated Use {@link SockJS#create(Vertx, SockJSOptions)}
   * Create a SockJS handler
   *
   * @param vertx  the Vert.x instance
   * @param options  options to configure the handler
   * @return the handler
   */
  @Deprecated
  static SockJSHandler create(Vertx vertx, SockJSHandlerOptions options) {
    return new SockJSImpl(vertx, options);
  }

  /**
   * @deprecated mount the router as a sub-router instead. This method will not properly handle errors.
   * @param routingContext the routing context
   */
  @Override
  @Deprecated
  default void handle(RoutingContext routingContext) {
    throw new UnsupportedOperationException("this handler will not work as expected if there are errors");
  }
}
