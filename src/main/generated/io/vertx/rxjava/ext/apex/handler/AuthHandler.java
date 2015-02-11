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
import io.vertx.rxjava.ext.apex.RoutingContext;
import java.util.Set;
import io.vertx.core.Handler;

/**
 * Base interface for auth handlers.
 * <p>
 * An auth handler allows your application to provide authentication/authorisation support.
 * <p>
 * Auth handler requires a {@link io.vertx.ext.apex.handler.SessionHandler} to be on the routing chain before it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * NOTE: This class has been automatically generated from the original non RX-ified interface using Vert.x codegen.
 */

public class AuthHandler {

  final io.vertx.ext.apex.handler.AuthHandler delegate;

  public AuthHandler(io.vertx.ext.apex.handler.AuthHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Add a required role for this auth handler
   *
   * @param role  the role
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addRole(String role) {
    this.delegate.addRole(role);
    return this;
  }

  /**
   * Add a required permission for this auth handler
   *
   * @param permission  the permission
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addPermission(String permission) {
    this.delegate.addPermission(permission);
    return this;
  }

  /**
   * Add a set of required roles for this auth handler
   *
   * @param roles  the set of roles
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addRoles(Set<String> roles) {
    this.delegate.addRoles(roles);
    return this;
  }

  /**
   * Add a set of required permissions for this auth handler
   *
   * @param permissions  the set of permissions
   * @return a reference to this, so the API can be used fluently
   */
  public AuthHandler addPermissions(Set<String> permissions) {
    this.delegate.addPermissions(permissions);
    return this;
  }


  public static AuthHandler newInstance(io.vertx.ext.apex.handler.AuthHandler arg) {
    return new AuthHandler(arg);
  }
}
