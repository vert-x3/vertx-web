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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class AuthHandlerImpl implements AuthHandler {

  private static final Logger log = LoggerFactory.getLogger(AuthHandlerImpl.class);

  protected final AuthProvider authProvider;
  protected final Set<String> authorities = new HashSet<>();

  public AuthHandlerImpl(AuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public AuthHandler addAuthority(String authority) {
    authorities.add(authority);
    return this;
  }

  @Override
  public AuthHandler addAuthorities(Set<String> authorities) {
    this.authorities.addAll(authorities);
    return this;
  }

  protected void authorise(User user, RoutingContext context) {
    int requiredcount = authorities.size();
    if (requiredcount > 0) {
      AtomicInteger count = new AtomicInteger();
      AtomicBoolean sentFailure = new AtomicBoolean();

      Handler<AsyncResult<Boolean>> authHandler = res -> {
        if (res.succeeded()) {
          if (res.result()) {
            if (count.incrementAndGet() == requiredcount) {
              // Has all required authorities
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
      for (String authority: authorities) {
        user.isAuthorised(authority, authHandler);
      }
    } else {
      // No auth required
      context.next();
    }
  }


}
