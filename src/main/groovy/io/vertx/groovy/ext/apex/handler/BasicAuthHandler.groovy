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
import io.vertx.groovy.ext.auth.AuthService
/**
 * An auth handler that provides HTTP Basic Authentication support.
*/
@CompileStatic
public class BasicAuthHandler extends AuthHandler {
  final def io.vertx.ext.apex.handler.BasicAuthHandler delegate;
  public BasicAuthHandler(io.vertx.ext.apex.handler.BasicAuthHandler delegate) {
    super(delegate);
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a basic auth handler
   * @param authService the auth service to use
   * @return the auth handler
   */
  public static AuthHandler create(AuthService authService) {
    def ret= AuthHandler.FACTORY.apply(io.vertx.ext.apex.handler.BasicAuthHandler.create((io.vertx.ext.auth.AuthService)authService.getDelegate()));
    return ret;
  }
  /**
   * Create a basic auth handler, specifying realm
   * @param authService the auth service to use
   * @param realm the realm to use
   * @return the auth handler
   */
  public static AuthHandler create(AuthService authService, String realm) {
    def ret= AuthHandler.FACTORY.apply(io.vertx.ext.apex.handler.BasicAuthHandler.create((io.vertx.ext.auth.AuthService)authService.getDelegate(), realm));
    return ret;
  }
  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext)context.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.BasicAuthHandler, BasicAuthHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.BasicAuthHandler arg -> new BasicAuthHandler(arg);
  };
}
