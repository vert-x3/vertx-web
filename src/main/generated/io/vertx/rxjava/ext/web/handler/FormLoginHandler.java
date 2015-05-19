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

package io.vertx.rxjava.ext.web.handler;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.core.Handler;
import io.vertx.rxjava.ext.auth.AuthProvider;

/**
 * Handler that handles login from a form on a custom login page.
 * <p>
 * Used in conjunction with the {@link  io.vertx.rxjava.ext.web.handler.RedirectAuthHandler}.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.FormLoginHandler original} non RX-ified interface using Vert.x codegen.
 */

public class FormLoginHandler implements Handler<RoutingContext> {

  final io.vertx.ext.web.handler.FormLoginHandler delegate;

  public FormLoginHandler(io.vertx.ext.web.handler.FormLoginHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    this.delegate.handle((io.vertx.ext.web.RoutingContext) arg0.getDelegate());
  }

  /**
   * Create a handler
   * @param authProvider the auth service to use
   * @return the handler
   */
  public static FormLoginHandler create(AuthProvider authProvider) { 
    FormLoginHandler ret= FormLoginHandler.newInstance(io.vertx.ext.web.handler.FormLoginHandler.create((io.vertx.ext.auth.AuthProvider) authProvider.getDelegate()));
    return ret;
  }

  /**
   * Create a handler
   * @param authProvider the auth service to use
   * @param usernameParam the value of the form attribute which will contain the username
   * @param passwordParam the value of the form attribute which will contain the password
   * @param returnURLParam the value of the form attribute which will contain the return url
   * @return the handler
   */
  public static FormLoginHandler create(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam) { 
    FormLoginHandler ret= FormLoginHandler.newInstance(io.vertx.ext.web.handler.FormLoginHandler.create((io.vertx.ext.auth.AuthProvider) authProvider.getDelegate(), usernameParam, passwordParam, returnURLParam));
    return ret;
  }


  public static FormLoginHandler newInstance(io.vertx.ext.web.handler.FormLoginHandler arg) {
    return new FormLoginHandler(arg);
  }
}
