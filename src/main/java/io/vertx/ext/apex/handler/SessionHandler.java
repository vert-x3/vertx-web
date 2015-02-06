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
 * A handler that maintains a {@link io.vertx.ext.apex.Session} for each browser session.
 * <p>
 * It looks up the session for each request based on a session cookie which contains a session ID. It stores the session
 * when the response is ended in the session store.
 * <p>
 * The session is available on the routing context with {@link io.vertx.ext.apex.RoutingContext#session()}.
 * <p>
 * The session handler requires a {@link io.vertx.ext.apex.handler.CookieHandler} to be on the routing chain before it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface SessionHandler extends Handler<RoutingContext> {

  /**
   * Default name of session cookie
   */
  static final String DEFAULT_SESSION_COOKIE_NAME = "apex.session";

  /**
   * Default time, in ms, that a session lasts for without being accessed before expiring.
   */
  static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

  /**
   * Default of whether a nagging log warning should be written if the session handler is accessed over HTTP, not
   * HTTPS
   */
  static final boolean DEFAULT_NAG_HTTPS = true;

  /**
   * Create a session handler
   *
   * @param sessionStore  the session store
   * @return the handler
   */
  static SessionHandler create(SessionStore sessionStore) {
    return new SessionHandlerImpl(DEFAULT_SESSION_COOKIE_NAME, DEFAULT_SESSION_TIMEOUT, DEFAULT_NAG_HTTPS, sessionStore);
  }

  /**
   * Set the session timeout
   *
   * @param timeout  the timeout, in ms.
   * @return a reference to this, so the API can be used fluently
   */
  SessionHandler setSessionTimeout(long timeout);

  /**
   * Set whether a nagging log warning should be written if the session handler is accessed over HTTP, not
   * HTTPS
   * @param nag  true to nag
   * @return a reference to this, so the API can be used fluently
   */
  SessionHandler setNagHttps(boolean nag);

  /**
   * Set the session cookie name
   *
   * @param sessionCookieName  the session cookie name
   * @return a reference to this, so the API can be used fluently
   */
  SessionHandler setSessionCookieName(String sessionCookieName);

  @Override
  void handle(RoutingContext context);
}
