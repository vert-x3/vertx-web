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

package io.vertx.groovy.ext.apex.addons;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.core.RoutingContext
import io.vertx.groovy.ext.apex.core.SessionStore
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class SessionHandler {
  final def io.vertx.ext.apex.addons.SessionHandler delegate;
  public SessionHandler(io.vertx.ext.apex.addons.SessionHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static SessionHandler sessionHandler(String sessionCookieName, long sessionTimeout, boolean nagHttps, SessionStore sessionStore) {
    def ret= SessionHandler.FACTORY.apply(io.vertx.ext.apex.addons.SessionHandler.sessionHandler(sessionCookieName, sessionTimeout, nagHttps, (io.vertx.ext.apex.core.SessionStore)sessionStore.getDelegate()));
    return ret;
  }
  public static SessionHandler sessionHandler(SessionStore sessionStore) {
    def ret= SessionHandler.FACTORY.apply(io.vertx.ext.apex.addons.SessionHandler.sessionHandler((io.vertx.ext.apex.core.SessionStore)sessionStore.getDelegate()));
    return ret;
  }
  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.core.RoutingContext)context.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.SessionHandler, SessionHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.SessionHandler arg -> new SessionHandler(arg);
  };
}
