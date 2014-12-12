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

import io.vertx.core.Handler;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface SessionHandler extends Handler<RoutingContext> {

  public static final String DEFAULT_SESSION_COOKIE_NAME = "apex.session";
  public static final long DEFAULT_COOKIE_MAX_AGE = 30 * 60; // 30 minutes
  public static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 ; // 30 minutes

  static SessionHandler sessionHandler(String sessionCookieName, long cookieMaxAge, long sessionTimeout,
                                       SessionStore sessionStore) {
    return new SessionHandlerImpl(sessionCookieName, cookieMaxAge, sessionTimeout, sessionStore);
  }

  static SessionHandler sessionHandler(SessionStore sessionStore) {
    return new SessionHandlerImpl(DEFAULT_SESSION_COOKIE_NAME, DEFAULT_COOKIE_MAX_AGE, DEFAULT_SESSION_TIMEOUT, sessionStore);
  }

  @Override
  void handle(RoutingContext context);
}
