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
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.impl.SockJSImpl;

/**
 *
 * A handler that allows you to handle SockJS connections from clients.
 * <p>
 * We currently support version 0.3.3 of the SockJS protocol, which can be found in
 * <a href="https://github.com/sockjs/sockjs-protocol/tree/v0.3.3">this tag:</a>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface SockJSHandler extends Handler<RoutingContext> {

  /**
   * Create a SockJS handler
   *
   * @param vertx  the Vert.x instance
   * @return the handler
   */
  static SockJSHandler create(Vertx vertx) {
    return create(vertx, new SockJSHandlerOptions());
  }

  /**
   * Create a SockJS handler
   *
   * @param vertx  the Vert.x instance
   * @param options  options to configure the handler
   * @return the handler
   */
  static SockJSHandler create(Vertx vertx, SockJSHandlerOptions options) {
    return new SockJSImpl(vertx, options);
  }

  /**
   * Set a SockJS socket handler. This handler will be called with a SockJS socket whenever a SockJS connection
   * is made from a client
   *
   * @param handler  the handler
   * @return a router to be mounted on an existing router
   */
  Router socketHandler(Handler<SockJSSocket> handler);

  /**
   * Bridge the SockJS handler to the Vert.x event bus. This basically installs a built-in SockJS socket handler
   * which takes SockJS traffic and bridges it to the event bus, thus allowing you to extend the server-side
   * Vert.x event bus to browsers
   *
   * @param bridgeOptions  options to configure the bridge with
   * @return a router to be mounted on an existing router
   */
  default Router bridge(SockJSBridgeOptions bridgeOptions) {
    return bridge(null, bridgeOptions, null);
  }

  /**
   * Like {@link io.vertx.ext.web.handler.sockjs.SockJSHandler#bridge(SockJSBridgeOptions)} but specifying a handler
   * that will receive bridge events.
   * @param authorizationProvider authorization provider to be used on the bridge
   * @param bridgeOptions  options to configure the bridge with
   * @param bridgeEventHandler  handler to receive bridge events
   * @return a router to be mounted on an existing router
   */
  Router bridge(AuthorizationProvider authorizationProvider, SockJSBridgeOptions bridgeOptions, Handler<BridgeEvent> bridgeEventHandler);

  /**
   * Like {@link io.vertx.ext.web.handler.sockjs.SockJSHandler#bridge(SockJSBridgeOptions)} but specifying a handler
   * that will receive bridge events.
   * @param bridgeOptions  options to configure the bridge with
   * @param bridgeEventHandler  handler to receive bridge events
   * @return a router to be mounted on an existing router
   */
  default Router bridge(SockJSBridgeOptions bridgeOptions, Handler<BridgeEvent> bridgeEventHandler) {
    return bridge(null, bridgeOptions, bridgeEventHandler);
  }

  /**
   * @deprecated mount the router as a sub-router instead. This method will not properly handle errors.
   * @param routingContext the routing context
   */
  @Deprecated
  void handle(RoutingContext routingContext);
}
