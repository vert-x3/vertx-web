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
import io.vertx.groovy.ext.web.Route
import java.util.Set
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.groovy.ext.auth.oauth2.OAuth2Auth
/**
 * An auth handler that provides OAuth2 Authentication support. This handler is suitable for AuthCode flows.
*/
@CompileStatic
public class OAuth2AuthHandler implements AuthHandler {
  private final def io.vertx.ext.web.handler.OAuth2AuthHandler delegate;
  public OAuth2AuthHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.OAuth2AuthHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) delegate).handle(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  public AuthHandler addAuthority(String arg0) {
    ((io.vertx.ext.web.handler.AuthHandler) delegate).addAuthority(arg0);
    return this;
  }
  public AuthHandler addAuthorities(Set<String> arg0) {
    ((io.vertx.ext.web.handler.AuthHandler) delegate).addAuthorities(arg0 != null ? (Set)arg0.collect({it}) as Set : null);
    return this;
  }
  /**
   * Create a OAuth2 auth handler
   * @param authProvider the auth provider to use
   * @param uri 
   * @return the auth handler
   */
  public static OAuth2AuthHandler create(OAuth2Auth authProvider, String uri) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.OAuth2AuthHandler.create(authProvider != null ? (io.vertx.ext.auth.oauth2.OAuth2Auth)authProvider.getDelegate() : null, uri), io.vertx.groovy.ext.web.handler.OAuth2AuthHandler.class);
    return ret;
  }
  /**
   * Build the authorization URL.
   * @param redirectURL where is the callback mounted.
   * @param state state opaque token to avoid forged requests
   * @return the redirect URL
   */
  public String authURI(String redirectURL, String state) {
    def ret = delegate.authURI(redirectURL, state);
    return ret;
  }
  /**
   * add the callback handler to a given route.
   * @param route a given route e.g.: `/callback`
   * @return self
   */
  public OAuth2AuthHandler setupCallback(Route route) {
    delegate.setupCallback(route != null ? (io.vertx.ext.web.Route)route.getDelegate() : null);
    return this;
  }
}
