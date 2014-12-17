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

package io.vertx.ext.apex.addons.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.addons.AuthHandler;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.Session;
import io.vertx.ext.auth.AuthService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class AuthHandlerImpl implements AuthHandler {

  private static final Logger log = LoggerFactory.getLogger(AuthHandlerImpl.class);

  protected final AuthService authService;
  protected final Set<String> roles = new HashSet<>();
  protected final Set<String> permissions = new HashSet<>();

  public AuthHandlerImpl(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public AuthHandler addRole(String role) {
    roles.add(role);
    return this;
  }

  @Override
  public AuthHandler addPermission(String permission) {
    permissions.add(permission);
    return null;
  }

  @Override
  public AuthHandler addRoles(Set<String> roles) {
    this.roles.addAll(roles);
    return this;
  }

  @Override
  public AuthHandler addPermissions(Set<String> permissions) {
    this.permissions.addAll(permissions);
    return this;
  }

  protected void authorise(RoutingContext context) {
    Session session = context.session();
    int requiredcount = (!permissions .isEmpty() ? 1 : 0) + (!roles.isEmpty() ? 1: 0);
    if (requiredcount > 0) {
      AtomicInteger count = new AtomicInteger();
      AtomicBoolean sentFailure = new AtomicBoolean();

      Handler<AsyncResult<Boolean>> authHandler = res -> {
        if (res.succeeded()) {
          if (res.result()) {
            if (count.incrementAndGet() == requiredcount) {
              // Has all required roles and permissions
              context.next();
            }
          } else {
            if (sentFailure.compareAndSet(false, true)) {
              context.fail(403);
            }
          }
        } else {
          context.fail(res.cause());
        }
      };

      if (!permissions.isEmpty()) {
        authService.hasPermissions(session.getPrincipal(), permissions, authHandler);
      }
      if (!roles.isEmpty()) {
        authService.hasRoles(session.getPrincipal(), roles, authHandler);
      }
    } else {
      // No auth required
      context.next();
    }
  }


}
