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

package io.vertx.ext.apex.handler.sockjs;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.sockjs.impl.SockJSHandlerImpl;

/**
 *
 * A handler that allows you to handle SockJS connections from clients.
 * <p>
 * We currently support version 0.3.3 of the SockJS protocol, which can be found in
 * <a href="https://github.com/sockjs/sockjs-protocol/tree/v0.3.3">this tag:</a>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
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
    return new SockJSHandlerImpl(vertx, new SockJSHandlerOptions());
  }

  /**
   * Create a SockJS handler
   *
   * @param vertx  the Vert.x instance
   * @param options  options to configure the handler
   * @return the handler
   */
  static SockJSHandler create(Vertx vertx, SockJSHandlerOptions options) {
    return new SockJSHandlerImpl(vertx, options);
  }

  /**
   * Install SockJS test applications on a router - used when running the SockJS test suite
   *
   * @param router  the router to install on
   * @param vertx  the Vert.x instance
   */
  static void installTestApplications(Router router, Vertx vertx) {
    SockJSHandlerImpl.installTestApplications(router, vertx);
  }

  /**
   * Set a SockJS socket handler. This handler will be called with a SockJS socket whenever a SockJS connection
   * is made from a client
   *
   * @param handler  the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SockJSHandler socketHandler(Handler<SockJSSocket> handler);

  /**
   * Bridge the SockJS handler to the Vert.x event bus. This basically installs a built-in SockJS socket handler
   * which takes SockJS traffic and bridges it to the event bus, thus allowing you to extend the server-side
   * Vert.x event bus to browsers
   *
   * @param bridgeOptions  options to configure the bridge with
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SockJSHandler bridge(BridgeOptions bridgeOptions);

}
