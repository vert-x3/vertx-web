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
import io.vertx.ext.apex.handler.impl.SessionHandlerImpl;
import io.vertx.ext.apex.sstore.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface SessionHandler extends Handler<RoutingContext> {

  static final String DEFAULT_SESSION_COOKIE_NAME = "apex.session";
  static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
  static final boolean DEFAULT_NAG_HTTPS = true;

  static SessionHandler create(String sessionCookieName, long sessionTimeout, boolean nagHttps,
                               SessionStore sessionStore) {
    return new SessionHandlerImpl(sessionCookieName, sessionTimeout, nagHttps, sessionStore);
  }

  static SessionHandler create(SessionStore sessionStore) {
    return new SessionHandlerImpl(DEFAULT_SESSION_COOKIE_NAME, DEFAULT_SESSION_TIMEOUT, DEFAULT_NAG_HTTPS, sessionStore);
  }

  @Override
  void handle(RoutingContext context);
}
