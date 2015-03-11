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

package io.vertx.groovy.ext.apex.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.RoutingContext
import java.util.Set
import io.vertx.core.Handler
/**
 * Base interface for auth handlers.
 * <p>
 * An auth handler allows your application to provide authentication/authorisation support.
 * <p>
 * Auth handler requires a link to be on the routing chain before it.
*/
@CompileStatic
public interface AuthHandler extends Handler<RoutingContext> {
  public Object getDelegate();
  void handle(RoutingContext arg0);
  AuthHandler addRole(String role);
  AuthHandler addPermission(String permission);
  AuthHandler addRoles(Set<String> roles);
  AuthHandler addPermissions(Set<String> permissions);

  static final java.util.function.Function<io.vertx.ext.apex.handler.AuthHandler, AuthHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.AuthHandler arg -> new AuthHandlerImpl(arg);
  };
}

@CompileStatic
class AuthHandlerImpl implements AuthHandler {
  final def io.vertx.ext.apex.handler.AuthHandler delegate;
  public AuthHandlerImpl(io.vertx.ext.apex.handler.AuthHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.apex.RoutingContext)arg0.getDelegate());
  }
  /**
   * Add a required role for this auth handler
   * @param role the role
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addRole(String role) {
    ((io.vertx.ext.apex.handler.AuthHandler) this.delegate).addRole(role);
    return this;
  }
  /**
   * Add a required permission for this auth handler
   * @param permission the permission
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addPermission(String permission) {
    ((io.vertx.ext.apex.handler.AuthHandler) this.delegate).addPermission(permission);
    return this;
  }
  /**
   * Add a set of required roles for this auth handler
   * @param roles the set of roles
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addRoles(Set<String> roles) {
    ((io.vertx.ext.apex.handler.AuthHandler) this.delegate).addRoles(roles);
    return this;
  }
  /**
   * Add a set of required permissions for this auth handler
   * @param permissions the set of permissions
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addPermissions(Set<String> permissions) {
    ((io.vertx.ext.apex.handler.AuthHandler) this.delegate).addPermissions(permissions);
    return this;
  }
}
