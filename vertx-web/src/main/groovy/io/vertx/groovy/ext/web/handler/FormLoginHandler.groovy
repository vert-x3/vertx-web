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
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
import io.vertx.groovy.ext.auth.AuthProvider
/**
 * Handler that handles login from a form on a custom login page.
 * <p>
 * Used in conjunction with the {@link io.vertx.groovy.ext.web.handler.RedirectAuthHandler}.
*/
@CompileStatic
public class FormLoginHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.FormLoginHandler delegate;
  public FormLoginHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.FormLoginHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) delegate).handle(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  /**
   * Create a handler
   * @param authProvider the auth service to use
   * @return the handler
   */
  public static FormLoginHandler create(AuthProvider authProvider) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.FormLoginHandler.create(authProvider != null ? (io.vertx.ext.auth.AuthProvider)authProvider.getDelegate() : null), io.vertx.groovy.ext.web.handler.FormLoginHandler.class);
    return ret;
  }
  /**
   * Create a handler
   * @param authProvider the auth service to use
   * @param usernameParam the value of the form attribute which will contain the username
   * @param passwordParam the value of the form attribute which will contain the password
   * @param returnURLParam the value of the session attribute which will contain the return url
   * @param directLoggedInOKURL a url to redirect to if the user logs in directly at the url of the form login handler without being redirected here first
   * @return the handler
   */
  public static FormLoginHandler create(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam, String directLoggedInOKURL) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.FormLoginHandler.create(authProvider != null ? (io.vertx.ext.auth.AuthProvider)authProvider.getDelegate() : null, usernameParam, passwordParam, returnURLParam, directLoggedInOKURL), io.vertx.groovy.ext.web.handler.FormLoginHandler.class);
    return ret;
  }
  /**
   * Set the name of the form param used to submit the username
   * @param usernameParam the name of the param
   * @return a reference to this for a fluent API
   */
  public FormLoginHandler setUsernameParam(String usernameParam) {
    delegate.setUsernameParam(usernameParam);
    return this;
  }
  /**
   * Set the name of the form param used to submit the password
   * @param passwordParam the name of the param
   * @return a reference to this for a fluent API
   */
  public FormLoginHandler setPasswordParam(String passwordParam) {
    delegate.setPasswordParam(passwordParam);
    return this;
  }
  /**
   * Set the name of the session attrioute used to specify the return url
   * @param returnURLParam the name of the param
   * @return a reference to this for a fluent API
   */
  public FormLoginHandler setReturnURLParam(String returnURLParam) {
    delegate.setReturnURLParam(returnURLParam);
    return this;
  }
  /**
   * Set the url to redirect to if the user logs in directly at the url of the form login handler
   * without being redirected here first
   * @param directLoggedInOKURL the URL to redirect to
   * @return a reference to this for a fluent API
   */
  public FormLoginHandler setDirectLoggedInOKURL(String directLoggedInOKURL) {
    delegate.setDirectLoggedInOKURL(directLoggedInOKURL);
    return this;
  }
}
