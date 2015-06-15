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
import io.vertx.groovy.ext.auth.AuthProvider
/**
 *
 * This handler should be used if you want to store the User object in the Session so it's available between
 * different requests, without you having re-authenticate each time.
 *
 * It requires that the session handler is already present on previous matching routes.
 *
 * It requires an Auth provider so, if the user is deserialized from a clustered session it knows which Auth provider
 * to associate the session with.
*/
@CompileStatic
public class UserSessionHandler implements Handler<RoutingContext> {
  final def io.vertx.ext.web.handler.UserSessionHandler delegate;
  public UserSessionHandler(io.vertx.ext.web.handler.UserSessionHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a new handler
   * @param authProvider The auth provider to use
   * @return the handler
   */
  public static UserSessionHandler create(AuthProvider authProvider) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.web.handler.UserSessionHandler.create((io.vertx.ext.auth.AuthProvider)authProvider.getDelegate()), io.vertx.ext.web.handler.UserSessionHandler.class, io.vertx.groovy.ext.web.handler.UserSessionHandler.class);
    return ret;
  }
}
