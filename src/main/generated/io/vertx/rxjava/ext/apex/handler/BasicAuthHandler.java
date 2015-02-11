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

package io.vertx.rxjava.ext.apex.handler;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.apex.RoutingContext;
import io.vertx.rxjava.ext.auth.AuthService;

/**
 * An auth handler that provides HTTP Basic Authentication support.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * NOTE: This class has been automatically generated from the original non RX-ified interface using Vert.x codegen.
 */

public class BasicAuthHandler extends AuthHandler {

  final io.vertx.ext.apex.handler.BasicAuthHandler delegate;

  public BasicAuthHandler(io.vertx.ext.apex.handler.BasicAuthHandler delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create a basic auth handler
   *
   * @param authService  the auth service to use
   * @return the auth handler
   */
  public static AuthHandler create(AuthService authService) {
    AuthHandler ret= AuthHandler.newInstance(io.vertx.ext.apex.handler.BasicAuthHandler.create((io.vertx.ext.auth.AuthService) authService.getDelegate()));
    return ret;
  }

  /**
   * Create a basic auth handler, specifying realm
   *
   * @param authService  the auth service to use
   * @param realm  the realm to use
   * @return the auth handler
   */
  public static AuthHandler create(AuthService authService, String realm) {
    AuthHandler ret= AuthHandler.newInstance(io.vertx.ext.apex.handler.BasicAuthHandler.create((io.vertx.ext.auth.AuthService) authService.getDelegate(), realm));
    return ret;
  }

  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext) context.getDelegate());
  }


  public static BasicAuthHandler newInstance(io.vertx.ext.apex.handler.BasicAuthHandler arg) {
    return new BasicAuthHandler(arg);
  }
}
