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
import io.vertx.groovy.ext.auth.AuthProvider
/**
 * An auth handler that provides HTTP Basic Authentication support.
*/
@CompileStatic
public class BasicAuthHandler implements AuthHandler {
  private final def io.vertx.ext.web.handler.BasicAuthHandler delegate;
  public BasicAuthHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.BasicAuthHandler) delegate;
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
  /**
   * Create a basic auth handler
   * @param authProvider the auth provider to use
   * @return the auth handler
   */
  public static AuthHandler create(AuthProvider authProvider) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.BasicAuthHandler.create(authProvider != null ? (io.vertx.ext.auth.AuthProvider)authProvider.getDelegate() : null), io.vertx.groovy.ext.web.handler.AuthHandlerImpl.class);
    return ret;
  }
  /**
   * Create a basic auth handler, specifying realm
   * @param authProvider the auth service to use
   * @param realm the realm to use
   * @return the auth handler
   */
  public static AuthHandler create(AuthProvider authProvider, String realm) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.BasicAuthHandler.create(authProvider != null ? (io.vertx.ext.auth.AuthProvider)authProvider.getDelegate() : null, realm), io.vertx.groovy.ext.web.handler.AuthHandlerImpl.class);
    return ret;
  }
}
