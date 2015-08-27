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

package io.vertx.groovy.ext.web.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * Handler that will filter requests based on the request Host name.
*/
@CompileStatic
public class VirtualHostHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.VirtualHostHandler delegate;
  public VirtualHostHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.VirtualHostHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a handler
   * @param hostname 
   * @param handler 
   * @return the handler
   */
  public static VirtualHostHandler create(String hostname, Handler<RoutingContext> handler) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.web.handler.VirtualHostHandler.create(hostname, new Handler<io.vertx.ext.web.RoutingContext>() {
      public void handle(io.vertx.ext.web.RoutingContext event) {
        handler.handle(new io.vertx.groovy.ext.web.RoutingContext(event));
      }
    }), io.vertx.groovy.ext.web.handler.VirtualHostHandler.class);
    return ret;
  }
}
