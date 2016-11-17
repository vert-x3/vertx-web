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

import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.UserSessionHandler;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class UserSessionHandlerImpl implements UserSessionHandler {

  private static final String SESSION_USER_HOLDER_KEY = "__vertx.userHolder";

  private final AuthProvider authProvider;

  public UserSessionHandlerImpl(AuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    Session session = routingContext.session();
    if (session != null) {
      User user = null;
      UserHolder holder = session.get(SESSION_USER_HOLDER_KEY);
      if (holder != null) {
        RoutingContext prevContext = holder.context;
        if (prevContext != null) {
          user = prevContext.user();
        } else if (holder.user != null) {
          user = holder.user;
          user.setAuthProvider(authProvider);
          holder.context = routingContext;
          holder.user = null;
        }
        holder.context = routingContext;
      } else {
        // only at the time we are writing the header we should store the user to the session
        routingContext.addHeadersEndHandler(v -> {
          // during the request the user might have been removed
          if (routingContext.user() != null) {
            session.put(SESSION_USER_HOLDER_KEY, new UserHolder(routingContext));
          }
        });
      }
      if (user != null) {
        routingContext.setUser(user);
      }
    }
    routingContext.next();
  }

}
