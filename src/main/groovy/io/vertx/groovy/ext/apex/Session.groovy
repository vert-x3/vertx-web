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

package io.vertx.groovy.ext.apex;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.sstore.SessionStore
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.groovy.ext.auth.AuthService
/**
 * Represents a browser session.
 * <p>
 * Sessions persist between HTTP requests for a single browser session. They are deleted when the browser is closed, or
 * they time-out. Session cookies are used to maintain sessions using a secure UUID.
 * <p>
 * Sessions can be used to maintain data for a browser session, e.g. a shopping basket.
 * <p>
 * The context must have first been routed to a {@link io.vertx.groovy.ext.apex.handler.SessionHandler}
 * for sessions to be available.
*/
@CompileStatic
public class Session {
  final def io.vertx.ext.apex.Session delegate;
  public Session(io.vertx.ext.apex.Session delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * @return The unique ID of the session. This is generated using a random secure UUID.
   * @return 
   */
  public String id() {
    def ret = this.delegate.id();
    return ret;
  }
  /**
   * Put some data in a session
   * @param key the key for the data
   * @param obj the data
   * @return a reference to this, so the API can be used fluently
   */
  public Session put(String key, Object obj) {
    this.delegate.put(key, InternalHelper.unwrapObject(obj));
    return this;
  }
  /**
   * Get some data from the session
   * @param key the key of the data
   * @return the data
   */
  public <T> T get(String key) {
    // This cast is cleary flawed
    def ret = (T) InternalHelper.wrapObject(this.delegate.get(key));
    return ret;
  }
  /**
   * Remove some data from the session
   * @param key the key of the data
   * @return the data that was there or null if none there
   */
  public <T> T remove(String key) {
    // This cast is cleary flawed
    def ret = (T) InternalHelper.wrapObject(this.delegate.remove(key));
    return ret;
  }
  /**
   * @return the time the session was last accessed
   * @return 
   */
  public long lastAccessed() {
    def ret = this.delegate.lastAccessed();
    return ret;
  }
  /**
   * Destroy the session
   */
  public void destroy() {
    this.delegate.destroy();
  }
  /**
   * @return has the session been destroyed?
   * @return 
   */
  public boolean isDestroyed() {
    def ret = this.delegate.isDestroyed();
    return ret;
  }
  /**
   * @return  the login ID of the logged in user (if any). Must be used in conjunction with a
   * {@link io.vertx.groovy.ext.apex.handler.AuthHandler}.
   * @return 
   */
  public String getLoginID() {
    def ret = this.delegate.getLoginID();
    return ret;
  }
  /**
   * @return  true if the user is logged in.
   * @return 
   */
  public boolean isLoggedIn() {
    def ret = this.delegate.isLoggedIn();
    return ret;
  }
  /**
   * Does the logged in user have the specified role?  Information is cached for the lifetime of the session
   * @param role the role
   * @param resultHandler will be called with a result true/false
   */
  public void hasRole(String role, Handler<AsyncResult<Boolean>> resultHandler) {
    this.delegate.hasRole(role, resultHandler);
  }
  /**
   * Does the logged in user have the specified permissions?  Information is cached for the lifetime of the session
   * @param permission the permission
   * @param resultHandler will be called with a result true/false
   */
  public void hasPermission(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
    this.delegate.hasPermission(permission, resultHandler);
  }
  /**
   * Logout the user.
   */
  public void logout() {
    this.delegate.logout();
  }
  /**
   * @return the amount of time in ms, after which the session will expire, if not accessed.
   * @return 
   */
  public long timeout() {
    def ret = this.delegate.timeout();
    return ret;
  }
  /**
   * @return the store for the session
   * @return 
   */
  public SessionStore sessionStore() {
    def ret= SessionStore.FACTORY.apply(this.delegate.sessionStore());
    return ret;
  }
  /**
   * Set the login ID for the session
   * @param loginID the login ID
   */
  public void setLoginID(String loginID) {
    this.delegate.setLoginID(loginID);
  }
  /**
   * Mark the session as being accessed.
   */
  public void setAccessed() {
    this.delegate.setAccessed();
  }
  /**
   * Set the auth service
   * @param authService the auth service
   */
  public void setAuthService(AuthService authService) {
    this.delegate.setAuthService((io.vertx.ext.auth.AuthService)authService.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.Session, Session> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.Session arg -> new Session(arg);
  };
}
