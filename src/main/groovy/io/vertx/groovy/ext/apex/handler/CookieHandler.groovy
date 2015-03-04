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

package io.vertx.groovy.ext.apex.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.RoutingContext
import io.vertx.core.Handler
/**
 * A handler which decodes cookies from the request, makes them available in the {@link io.vertx.groovy.ext.apex.RoutingContext}
 * and writes them back in the response.
*/
@CompileStatic
public class CookieHandler {
  final def io.vertx.ext.apex.handler.CookieHandler delegate;
  public CookieHandler(io.vertx.ext.apex.handler.CookieHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a cookie handler
   * @return the cookie handler
   */
  public static CookieHandler create() {
    def ret= CookieHandler.FACTORY.apply(io.vertx.ext.apex.handler.CookieHandler.create());
    return ret;
  }
  public void handle(RoutingContext event) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext)event.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.CookieHandler, CookieHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.CookieHandler arg -> new CookieHandler(arg);
  };
}
