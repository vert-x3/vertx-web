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
import java.util.Set;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.core.Handler;

/**
 * Base interface for auth handlers.
 * <p>
 * An auth handler allows your application to provide authentication/authorisation support.
 * <p>
 * Auth handler requires a {@link  io.vertx.rxjava.ext.web.handler.SessionHandler} to be on the routing chain before it.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.AuthHandler original} non RX-ified interface using Vert.x codegen.
 */

public interface AuthHandler extends Handler<RoutingContext> {

  Object getDelegate();

  public void handle(RoutingContext arg0);

  /**
   * Add a required authority for this auth handler
   * @param authority the authority
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthority(String authority);

  /**
   * Add a set of required authorities for this auth handler
   * @param authorities the set of authorities
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthorities(Set<String> authorities);


  public static AuthHandler newInstance(io.vertx.ext.web.handler.AuthHandler arg) {
    return new AuthHandlerImpl(arg);
  }
}

class AuthHandlerImpl implements AuthHandler {
  final io.vertx.ext.web.handler.AuthHandler delegate;

  public AuthHandlerImpl(io.vertx.ext.web.handler.AuthHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    this.delegate.handle((io.vertx.ext.web.RoutingContext) arg0.getDelegate());
  }

  /**
   * Add a required authority for this auth handler
   * @param authority the authority
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthority(String authority) { 
    this.delegate.addAuthority(authority);
    return this;
  }

  /**
   * Add a set of required authorities for this auth handler
   * @param authorities the set of authorities
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthorities(Set<String> authorities) { 
    this.delegate.addAuthorities(authorities);
    return this;
  }

}
