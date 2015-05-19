/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

/**
 * Base interface for auth handlers.
 * <p>
 * An auth handler allows your application to provide authentication/authorisation support.
 * <p>
 * Auth handler requires a {@link io.vertx.ext.web.handler.SessionHandler} to be on the routing chain before it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen(concrete = false)
public interface AuthHandler extends Handler<RoutingContext> {

  /**
   * Add a required role for this auth handler
   *
   * @param role  the role
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  AuthHandler addRole(String role);

  /**
   * Add a required permission for this auth handler
   *
   * @param permission  the permission
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  AuthHandler addPermission(String permission);

  /**
   * Add a set of required roles for this auth handler
   *
   * @param roles  the set of roles
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  AuthHandler addRoles(Set<String> roles);

  /**
   * Add a set of required permissions for this auth handler
   *
   * @param permissions  the set of permissions
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  AuthHandler addPermissions(Set<String> permissions);
}
