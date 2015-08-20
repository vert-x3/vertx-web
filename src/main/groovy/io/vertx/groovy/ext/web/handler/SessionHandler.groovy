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
import io.vertx.groovy.ext.web.sstore.SessionStore
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * A handler that maintains a {@link io.vertx.groovy.ext.web.Session} for each browser session.
 * <p>
 * It looks up the session for each request based on a session cookie which contains a session ID. It stores the session
 * when the response is ended in the session store.
 * <p>
 * The session is available on the routing context with {@link io.vertx.groovy.ext.web.RoutingContext#session}.
 * <p>
 * The session handler requires a {@link io.vertx.groovy.ext.web.handler.CookieHandler} to be on the routing chain before it.
*/
@CompileStatic
public class SessionHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.SessionHandler delegate;
  public SessionHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.SessionHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a session handler
   * @param sessionStore the session store
   * @return the handler
   */
  public static SessionHandler create(SessionStore sessionStore) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.web.handler.SessionHandler.create((io.vertx.ext.web.sstore.SessionStore)sessionStore.getDelegate()), io.vertx.groovy.ext.web.handler.SessionHandler.class);
    return ret;
  }
  /**
   * Set the session timeout
   * @param timeout the timeout, in ms.
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandler setSessionTimeout(long timeout) {
    this.delegate.setSessionTimeout(timeout);
    return this;
  }
  /**
   * Set whether a nagging log warning should be written if the session handler is accessed over HTTP, not
   * HTTPS
   * @param nag true to nag
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandler setNagHttps(boolean nag) {
    this.delegate.setNagHttps(nag);
    return this;
  }
  /**
   * Sets whether the 'secure' flag should be set for the session cookie. When set this flag instructs browsers to only
   * send the cookie over HTTPS.
   * @param secure true to set the secure flag on the cookie
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandler setCookieSecureFlag(boolean secure) {
    this.delegate.setCookieSecureFlag(secure);
    return this;
  }
  /**
   * Sets whether the 'HttpOnly' flag should be set for the session cookie. When set this flag instructs browsers to
   * prevent Javascript access to the the cookie. Used as a line of defence against the most common XSS attacks.
   * @param httpOnly true to set the HttpOnly flag on the cookie
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandler setCookieHttpOnlyFlag(boolean httpOnly) {
    this.delegate.setCookieHttpOnlyFlag(httpOnly);
    return this;
  }
  /**
   * Set the session cookie name
   * @param sessionCookieName the session cookie name
   * @return a reference to this, so the API can be used fluently
   */
  public SessionHandler setSessionCookieName(String sessionCookieName) {
    this.delegate.setSessionCookieName(sessionCookieName);
    return this;
  }
}
