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
 * <p>
 * The session handler requires a {@link CookieHandler} to be on the routing
 * chain before it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface SessionHandler extends Handler<RoutingContext> {

	/**
	 * Default name of session cookie
	 */
	String DEFAULT_SESSION_COOKIE_NAME = "vertx-web.session";

	/**
	 * Default path of session cookie
	 */
	String DEFAULT_SESSION_COOKIE_PATH = "/";

	/**
	 * Default time, in ms, that a session lasts for without being accessed before
	 * expiring.
	 */
	long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

	/**
	 * Default of whether a nagging log warning should be written if the session
	 * handler is accessed over HTTP, not HTTPS
	 */
	boolean DEFAULT_NAG_HTTPS = true;

	/**
	 * Default of whether the cookie has the HttpOnly flag set More info:
	 * https://www.owasp.org/index.php/HttpOnly
	 */
	boolean DEFAULT_COOKIE_HTTP_ONLY_FLAG = false;

	/**
	 * Default of whether the cookie has the 'secure' flag set to allow transmission
	 * over https only. More info: https://www.owasp.org/index.php/SecureFlag
	 */
	boolean DEFAULT_COOKIE_SECURE_FLAG = false;

	/**
	 * Default min length for a session id. More info:
	 * https://www.owasp.org/index.php/Session_Management_Cheat_Sheet
	 */
	int DEFAULT_SESSIONID_MIN_LENGTH = 16;

	/**
	 * Create a session handler
	 *
	 * @param sessionStore the session store
	 * @return the handler
	 */
	static SessionHandler create(SessionStore sessionStore) {
		return new SessionHandlerImpl(DEFAULT_SESSION_COOKIE_NAME, DEFAULT_SESSION_COOKIE_PATH, DEFAULT_SESSION_TIMEOUT,
				DEFAULT_NAG_HTTPS, DEFAULT_COOKIE_SECURE_FLAG, DEFAULT_COOKIE_HTTP_ONLY_FLAG,
				DEFAULT_SESSIONID_MIN_LENGTH, sessionStore);
	}

	/**
	 * Set the session timeout
	 *
	 * @param timeout the timeout, in ms.
	 * @return a reference to this, so the API can be used fluently
	 */
	@Fluent
	SessionHandler setSessionTimeout(long timeout);

	/**
	 * Set whether a nagging log warning should be written if the session handler is
	 * accessed over HTTP, not HTTPS
	 * 
	 * @param nag true to nag
	 * @return a reference to this, so the API can be used fluently
	 */
	@Fluent
	SessionHandler setNagHttps(boolean nag);

	/**
	 * Sets whether the 'secure' flag should be set for the session cookie. When set
	 * this flag instructs browsers to only send the cookie over HTTPS. Note that
	 * this will probably stop your sessions working if used without HTTPS (e.g. in
	 * development).
	 *
	 * @param secure true to set the secure flag on the cookie
	 * @return a reference to this, so the API can be used fluently
	 */
	@Fluent
	SessionHandler setCookieSecureFlag(boolean secure);

	/**
	 * Sets whether the 'HttpOnly' flag should be set for the session cookie. When
	 * set this flag instructs browsers to prevent Javascript access to the the
	 * cookie. Used as a line of defence against the most common XSS attacks.
	 *
	 * @param httpOnly true to set the HttpOnly flag on the cookie
	 * @return a reference to this, so the API can be used fluently
	 */
	@Fluent
	SessionHandler setCookieHttpOnlyFlag(boolean httpOnly);

	/**
	 * Set the session cookie name
	 *
	 * @param sessionCookieName the session cookie name
	 * @return a reference to this, so the API can be used fluently
	 */
	@Fluent
	SessionHandler setSessionCookieName(String sessionCookieName);

	/**
	 * Set the session cookie path
	 *
	 * @param sessionCookiePath the session cookie path
	 * @return a reference to this, so the API can be used fluently
	 */
	@Fluent
	SessionHandler setSessionCookiePath(String sessionCookiePath);

	/**
	 * Set expected session id minimum length.
	 *
	 * @param minLength the session id minimal length
	 * @return a reference to this, so the API can be used fluently
	 */
	@Fluent
	SessionHandler setMinLength(int minLength);
}
