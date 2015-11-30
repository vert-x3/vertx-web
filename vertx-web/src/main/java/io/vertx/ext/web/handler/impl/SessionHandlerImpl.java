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
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SessionHandlerImpl implements SessionHandler {

  private static final Logger log = LoggerFactory.getLogger(SessionHandlerImpl.class);

  private final SessionStore sessionStore;
  private String sessionCookieName;
  private long sessionTimeout;
  private boolean nagHttps;
  private boolean sessionCookieSecure;
  private boolean sessionCookieHttpOnly;

  public SessionHandlerImpl(String sessionCookieName, long sessionTimeout, boolean nagHttps, boolean sessionCookieSecure, boolean sessionCookieHttpOnly, SessionStore sessionStore) {
    this.sessionCookieName = sessionCookieName;
    this.sessionTimeout = sessionTimeout;
    this.nagHttps = nagHttps;
    this.sessionStore = sessionStore;
    this.sessionCookieSecure = sessionCookieSecure;
    this.sessionCookieHttpOnly = sessionCookieHttpOnly;
  }

  @Override
  public SessionHandler setSessionTimeout(long timeout) {
    this.sessionTimeout = timeout;
    return this;
  }

  @Override
  public SessionHandler setNagHttps(boolean nag) {
    this.nagHttps = nag;
    return this;
  }

  @Override
  public SessionHandler setCookieSecureFlag(boolean secure) {
    this.sessionCookieSecure = secure;
    return this;
  }

  @Override
  public SessionHandler setCookieHttpOnlyFlag(boolean httpOnly) {
    this.sessionCookieHttpOnly = httpOnly;
    return this;
  }

  @Override
  public SessionHandler setSessionCookieName(String sessionCookieName) {
    this.sessionCookieName = sessionCookieName;
    return this;
  }

  @Override
  public void handle(RoutingContext context) {
    context.response().ended();

    if (nagHttps) {
      String uri = context.request().absoluteURI();
      if (!uri.startsWith("https:")) {
        log.warn("Using session cookies without https could make you susceptible to session hijacking: " + uri);
      }
    }

    // Look for existing session cookie
    Cookie cookie = context.getCookie(sessionCookieName);
    if (cookie != null) {
      // Look up session
      String sessionID = cookie.getValue();
      getSession(context.vertx(), sessionID, res -> {
        if (res.succeeded()) {
          Session session = res.result();
          if (session != null) {
            context.setSession(session);
            session.setAccessed();
            addStoreSessionHandler(context);
          } else {
            // Cannot find session - either it timed out, or was explicitly destroyed at the server side on a
            // previous request.
            // Either way, we create a new one.
            createNewSession(context);
          }
        } else {
          context.fail(res.cause());
        }
        context.next();
      });
    } else {
      createNewSession(context);
      context.next();
    }
  }

  private void getSession(Vertx vertx, String sessionID, Handler<AsyncResult<Session>> resultHandler) {
    doGetSession(vertx, System.currentTimeMillis(), sessionID, resultHandler);
  }

  private void doGetSession(Vertx vertx, long startTime, String sessionID, Handler<AsyncResult<Session>> resultHandler) {
    sessionStore.get(sessionID, res -> {
      if (res.succeeded()) {
        if (res.result() == null) {
          // Can't find it so retry. This is necessary for clustered sessions as it can take sometime for the session
          // to propagate across the cluster so if the next request for the session comes in quickly at a different
          // node there is a possibility it isn't available yet.
          long retryTimeout = sessionStore.retryTimeout();
          if (retryTimeout > 0 && System.currentTimeMillis() - startTime < retryTimeout) {
            vertx.setTimer(5, v -> doGetSession(vertx, startTime, sessionID, resultHandler));
            return;
          }
        }
      }
      resultHandler.handle(res);
    });
  }

  private void addStoreSessionHandler(RoutingContext context) {
    context.addHeadersEndHandler(v -> {
      Session session = context.session();
      if (!session.isDestroyed()) {
        // Store the session
        session.setAccessed();
        sessionStore.put(session, res -> {
          if (res.failed()) {
            log.error("Failed to store session", res.cause());
          }
        });
      } else {
        sessionStore.delete(session.id(), res -> {
          if (res.failed()) {
            log.error("Failed to delete session", res.cause());
          }
        });
      }
    });
  }

  private void createNewSession(RoutingContext context) {
    Session session = sessionStore.createSession(sessionTimeout);
    context.setSession(session);
    Cookie cookie = Cookie.cookie(sessionCookieName, session.id());
    cookie.setPath("/");
    cookie.setSecure(sessionCookieSecure);
    cookie.setHttpOnly(sessionCookieHttpOnly);
    // Don't set max age - it's a session cookie
    context.addCookie(cookie);
    addStoreSessionHandler(context);
  }
}
