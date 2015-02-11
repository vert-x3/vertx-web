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
import io.vertx.rxjava.ext.auth.AuthService;

/**
 * An auth handler that's used to handle auth by redirecting user to a custom login page.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * NOTE: This class has been automatically generated from the original non RX-ified interface using Vert.x codegen.
 */

public class RedirectAuthHandler extends AuthHandler {

  final io.vertx.ext.apex.handler.RedirectAuthHandler delegate;

  public RedirectAuthHandler(io.vertx.ext.apex.handler.RedirectAuthHandler delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create a handler
   *
   * @param authService  the auth service to use
   * @return the handler
   */
  public static AuthHandler create(AuthService authService) {
    AuthHandler ret= AuthHandler.newInstance(io.vertx.ext.apex.handler.RedirectAuthHandler.create((io.vertx.ext.auth.AuthService) authService.getDelegate()));
    return ret;
  }

  /**
   * Create a handler
   *
   * @param authService  the auth service to use
   * @param loginRedirectURL  the url to redirect the user to
   * @return the handler
   */
  public static AuthHandler create(AuthService authService, String loginRedirectURL) {
    AuthHandler ret= AuthHandler.newInstance(io.vertx.ext.apex.handler.RedirectAuthHandler.create((io.vertx.ext.auth.AuthService) authService.getDelegate(), loginRedirectURL));
    return ret;
  }

  /**
   * Create a handler
   *
   * @param authService  the auth service to use
   * @param loginRedirectURL  the url to redirect the user to
   * @param returnURLParam  the name of param used to store return url information in session
   * @return the handler
   */
  public static AuthHandler create(AuthService authService, String loginRedirectURL, String returnURLParam) {
    AuthHandler ret= AuthHandler.newInstance(io.vertx.ext.apex.handler.RedirectAuthHandler.create((io.vertx.ext.auth.AuthService) authService.getDelegate(), loginRedirectURL, returnURLParam));
    return ret;
  }


  public static RedirectAuthHandler newInstance(io.vertx.ext.apex.handler.RedirectAuthHandler arg) {
    return new RedirectAuthHandler(arg);
  }
}
