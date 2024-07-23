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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.SessionHandlerImpl;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * A handler that maintains a {@link io.vertx.ext.web.Session} for each browser
 * session.
 * <p>
 * It looks up the session for each request based on a session cookie which
 * contains a session ID. It stores the session when the response is ended in
 * the session store.
 * <p>
 * The session is available on the routing context with
 * {@link RoutingContext#session()}.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface SessionHandler extends PlatformHandler {

	/**
   * Create a session handler with default options.
	 *
	 * @param sessionStore the session store
	 * @return the handler
	 */
	static SessionHandler create(SessionStore sessionStore) {
    return create(sessionStore, new SessionHandlerOptions());
	}

  /**
   * Create a session handler.
   *
   * @param sessionStore the session store
   * @param options the session handler options
   * @return the handler
   */
  static SessionHandler create(SessionStore sessionStore, SessionHandlerOptions options) {
    return new SessionHandlerImpl(sessionStore, options);
  }

  /**
   * Flush a context session earlier to the store, this will allow the end user to have full control on the event of
   * a failure at the store level. Once a session is flushed no automatic save will be performed at end of request.
   *
   * @param ctx the current context
   * @return a future signaled with the asynchronous response.
   */
  default Future<Void> flush(RoutingContext ctx) {
    return flush(ctx, false);
  }

  /**
   * Flush a context session earlier to the store, this will allow the end user to have full control on the event of
   * a failure at the store level. Once a session is flushed no automatic save will be performed at end of request.
   *
   * @param ctx the current context
   * @param ignoreStatus flush regardless of response status code
   * @return a future signaled with the asynchronous response.
   */
  Future<Void> flush(RoutingContext ctx, boolean ignoreStatus);

  /**
   * Create a new session
   *
   * @param context the routing context
   * @return the session
   */
  Session newSession(RoutingContext context);

  /**
   * Set the user for the session
   *
   * @param context the routing context
   * @param user the user
   * @return future that will be called when complete, or a failure
   */
  Future<Void> setUser(RoutingContext context, User user);
}
