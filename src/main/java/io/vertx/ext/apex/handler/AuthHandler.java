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

package io.vertx.ext.apex.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;

import java.util.Set;

/**
 *
 * Auth handler requires a {@link io.vertx.ext.apex.handler.SessionHandler} to be on the routing chain before it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface AuthHandler extends Handler<RoutingContext> {

  AuthHandler addRole(String role);

  AuthHandler addPermission(String permission);

  AuthHandler addRoles(Set<String> roles);

  AuthHandler addPermissions(Set<String> permissions);
}
