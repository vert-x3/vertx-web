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
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * A handler which decodes cookies from the request, makes them available in the {@link io.vertx.groovy.ext.web.RoutingContext}
 * and writes them back in the response.
*/
@CompileStatic
public class CookieHandler implements Handler<RoutingContext> {
  final def io.vertx.ext.web.handler.CookieHandler delegate;
  public CookieHandler(io.vertx.ext.web.handler.CookieHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a cookie handler
   * @return the cookie handler
   */
  public static CookieHandler create() {
    def ret= new io.vertx.groovy.ext.web.handler.CookieHandler(io.vertx.ext.web.handler.CookieHandler.create());
    return ret;
  }
}
