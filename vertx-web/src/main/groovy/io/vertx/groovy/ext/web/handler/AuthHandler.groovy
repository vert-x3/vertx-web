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
import io.vertx.core.json.JsonObject
import java.util.Set
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * Base interface for auth handlers.
 * <p>
 * An auth handler allows your application to provide authentication/authorisation support.
 * <p>
 * Auth handler requires a {@link io.vertx.groovy.ext.web.handler.SessionHandler} to be on the routing chain before it.
*/
@CompileStatic
public interface AuthHandler extends Handler<RoutingContext> {
  public Object getDelegate();
  void handle(RoutingContext arg0);
  AuthHandler addAuthority(String authority);
  AuthHandler addAuthorities(Set<String> authorities);
}

@CompileStatic
class AuthHandlerImpl implements AuthHandler {
  private final def io.vertx.ext.web.handler.AuthHandler delegate;
  public AuthHandlerImpl(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.AuthHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) delegate).handle(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  /**
   * Add a required authority for this auth handler
   * @param authority the authority
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthority(String authority) {
    ((io.vertx.ext.web.handler.AuthHandler) delegate).addAuthority(authority);
    return this;
  }
  /**
   * Add a set of required authorities for this auth handler
   * @param authorities the set of authorities
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addAuthorities(Set<String> authorities) {
    ((io.vertx.ext.web.handler.AuthHandler) delegate).addAuthorities(authorities != null ? (Set)authorities.collect({it}) as Set : null);
    return this;
  }
}
