/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.apex.handler.sockjs;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.RoutingContext
import io.vertx.groovy.ext.apex.Router
import io.vertx.groovy.core.Vertx
import io.vertx.ext.apex.handler.sockjs.SockJSHandlerOptions
import io.vertx.core.Handler
import io.vertx.ext.apex.handler.sockjs.BridgeOptions
/**
 *
 * A handler that allows you to handle SockJS connections from clients.
 * <p>
 * We currently support version 0.3.3 of the SockJS protocol, which can be found in
 * <a href="https://github.com/sockjs/sockjs-protocol/tree/v0.3.3">this tag:</a>
*/
@CompileStatic
public class SockJSHandler implements Handler<RoutingContext> {
  final def io.vertx.ext.apex.handler.sockjs.SockJSHandler delegate;
  public SockJSHandler(io.vertx.ext.apex.handler.sockjs.SockJSHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.apex.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a SockJS handler
   * @param vertx the Vert.x instance
   * @return the handler
   */
  public static SockJSHandler create(Vertx vertx) {
    def ret= new io.vertx.groovy.ext.apex.handler.sockjs.SockJSHandler(io.vertx.ext.apex.handler.sockjs.SockJSHandler.create((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }
  /**
   * Create a SockJS handler
   * @param vertx the Vert.x instance
   * @param options options to configure the handler (see <a href="../../../../../../../../../cheatsheet/SockJSHandlerOptions.html">SockJSHandlerOptions</a>)
   * @return the handler
   */
  public static SockJSHandler create(Vertx vertx, Map<String, Object> options) {
    def ret= new io.vertx.groovy.ext.apex.handler.sockjs.SockJSHandler(io.vertx.ext.apex.handler.sockjs.SockJSHandler.create((io.vertx.core.Vertx)vertx.getDelegate(), options != null ? new io.vertx.ext.apex.handler.sockjs.SockJSHandlerOptions(new io.vertx.core.json.JsonObject(options)) : null));
    return ret;
  }
  /**
   * Install SockJS test applications on a router - used when running the SockJS test suite
   * @param router the router to install on
   * @param vertx the Vert.x instance
   */
  public static void installTestApplications(Router router, Vertx vertx) {
    io.vertx.ext.apex.handler.sockjs.SockJSHandler.installTestApplications((io.vertx.ext.apex.Router)router.getDelegate(), (io.vertx.core.Vertx)vertx.getDelegate());
  }
  /**
   * Set a SockJS socket handler. This handler will be called with a SockJS socket whenever a SockJS connection
   * is made from a client
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  public SockJSHandler socketHandler(Handler<SockJSSocket> handler) {
    this.delegate.socketHandler(new Handler<io.vertx.ext.apex.handler.sockjs.SockJSSocket>() {
      public void handle(io.vertx.ext.apex.handler.sockjs.SockJSSocket event) {
        handler.handle(new io.vertx.groovy.ext.apex.handler.sockjs.SockJSSocket(event));
      }
    });
    return this;
  }
  /**
   * Bridge the SockJS handler to the Vert.x event bus. This basically installs a built-in SockJS socket handler
   * which takes SockJS traffic and bridges it to the event bus, thus allowing you to extend the server-side
   * Vert.x event bus to browsers
   * @param bridgeOptions options to configure the bridge with (see <a href="../../../../../../../../../cheatsheet/BridgeOptions.html">BridgeOptions</a>)
   * @return a reference to this, so the API can be used fluently
   */
  public SockJSHandler bridge(Map<String, Object> bridgeOptions = [:]) {
    this.delegate.bridge(bridgeOptions != null ? new io.vertx.ext.apex.handler.sockjs.BridgeOptions(new io.vertx.core.json.JsonObject(bridgeOptions)) : null);
    return this;
  }
  public SockJSHandler bridge(Map<String, Object> bridgeOptions = [:], Handler<BridgeEvent> bridgeEventHandler) {
    this.delegate.bridge(bridgeOptions != null ? new io.vertx.ext.apex.handler.sockjs.BridgeOptions(new io.vertx.core.json.JsonObject(bridgeOptions)) : null, new Handler<io.vertx.ext.apex.handler.sockjs.BridgeEvent>() {
      public void handle(io.vertx.ext.apex.handler.sockjs.BridgeEvent event) {
        bridgeEventHandler.handle(new io.vertx.groovy.ext.apex.handler.sockjs.BridgeEvent(event));
      }
    });
    return this;
  }
}
