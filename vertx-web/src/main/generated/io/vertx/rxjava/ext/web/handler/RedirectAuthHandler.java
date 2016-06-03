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
import rx.Observable;
import java.util.Set;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.auth.AuthProvider;

/**
 * An auth handler that's used to handle auth by redirecting user to a custom login page.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.RedirectAuthHandler original} non RX-ified interface using Vert.x codegen.
 */

public class RedirectAuthHandler implements AuthHandler {

  final io.vertx.ext.web.handler.RedirectAuthHandler delegate;

  public RedirectAuthHandler(io.vertx.ext.web.handler.RedirectAuthHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    delegate.handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }

  /**
   * Add a required authority for this auth handler
   * @param authority the authority
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthority(String authority) { 
    delegate.addAuthority(authority);
    return this;
  }

  /**
   * Add a set of required authorities for this auth handler
   * @param authorities the set of authorities
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthorities(Set<String> authorities) { 
    delegate.addAuthorities(authorities);
    return this;
  }

  /**
   * Create a handler
   * @param authProvider the auth service to use
   * @return the handler
   */
  public static AuthHandler create(AuthProvider authProvider) { 
    AuthHandler ret = AuthHandler.newInstance(io.vertx.ext.web.handler.RedirectAuthHandler.create((io.vertx.ext.auth.AuthProvider)authProvider.getDelegate()));
    return ret;
  }

  /**
   * Create a handler
   * @param authProvider the auth service to use
   * @param loginRedirectURL the url to redirect the user to
   * @return the handler
   */
  public static AuthHandler create(AuthProvider authProvider, String loginRedirectURL) { 
    AuthHandler ret = AuthHandler.newInstance(io.vertx.ext.web.handler.RedirectAuthHandler.create((io.vertx.ext.auth.AuthProvider)authProvider.getDelegate(), loginRedirectURL));
    return ret;
  }

  /**
   * Create a handler
   * @param authProvider the auth service to use
   * @param loginRedirectURL the url to redirect the user to
   * @param returnURLParam the name of param used to store return url information in session
   * @return the handler
   */
  public static AuthHandler create(AuthProvider authProvider, String loginRedirectURL, String returnURLParam) { 
    AuthHandler ret = AuthHandler.newInstance(io.vertx.ext.web.handler.RedirectAuthHandler.create((io.vertx.ext.auth.AuthProvider)authProvider.getDelegate(), loginRedirectURL, returnURLParam));
    return ret;
  }


  public static RedirectAuthHandler newInstance(io.vertx.ext.web.handler.RedirectAuthHandler arg) {
    return arg != null ? new RedirectAuthHandler(arg) : null;
  }
}
