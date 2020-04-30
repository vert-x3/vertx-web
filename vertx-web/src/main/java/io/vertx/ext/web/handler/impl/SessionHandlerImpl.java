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
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SessionHandlerImpl implements SessionHandler {

  private static final String SESSION_USER_HOLDER_KEY = "__vertx.userHolder";

  private static final Logger log = LoggerFactory.getLogger(SessionHandlerImpl.class);

  private final SessionStore sessionStore;
  private String sessionCookieName;
  private String sessionCookiePath;
  private long sessionTimeout;
  private boolean nagHttps;
  private boolean sessionCookieSecure;
  private boolean sessionCookieHttpOnly;
  private int minLength;
  private CookieSameSite cookieSameSite;
  private boolean lazySession;

  public SessionHandlerImpl(String sessionCookieName, String sessionCookiePath, long sessionTimeout, boolean nagHttps,
                            boolean sessionCookieSecure, boolean sessionCookieHttpOnly, int minLength, boolean lazySession,
                            SessionStore sessionStore) {
    this.sessionCookieName = sessionCookieName;
    this.sessionCookiePath = sessionCookiePath;
    this.sessionTimeout = sessionTimeout;
    this.nagHttps = nagHttps;
    this.sessionStore = sessionStore;
    this.sessionCookieSecure = sessionCookieSecure;
    this.sessionCookieHttpOnly = sessionCookieHttpOnly;
    this.minLength = minLength;
    this.lazySession = lazySession;
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
  public SessionHandler setSessionCookiePath(String sessionCookiePath) {
    this.sessionCookiePath = sessionCookiePath;
    return this;
  }

  @Override
  public SessionHandler setMinLength(int minLength) {
    this.minLength = minLength;
    return this;
  }

  @Override
  public SessionHandler setCookieSameSite(CookieSameSite policy) {
    this.cookieSameSite = policy;
    return this;
  }

  @Override
  public SessionHandler setLazySession(boolean lazySession) {
    this.lazySession = lazySession;
    return this;
  }

  @Override
  @Deprecated
  public SessionHandler setAuthProvider(AuthProvider authProvider) {
    return this;
  }

  @Override
  public void handle(RoutingContext context) {
    if (nagHttps && log.isDebugEnabled()) {
      String uri = context.request().absoluteURI();
      if (!uri.startsWith("https:")) {
        log.debug(
          "Using session cookies without https could make you susceptible to session hijacking: " + uri);
      }
    }

    // Look for existing session cookie
    Cookie cookie = context.getCookie(sessionCookieName);
    if (cookie != null) {
      // Look up session
      String sessionID = cookie.getValue();
      if (sessionID != null && sessionID.length() > minLength) {
        // we passed the OWASP min length requirements
        getSession(context.vertx(), sessionID, res -> {
          if (res.succeeded()) {
            Session session = res.result();
            if (session != null) {
              context.setSession(session);
              // attempt to load the user from the session
              UserHolder holder = session.get(SESSION_USER_HOLDER_KEY);
              if (holder != null) {
                holder.refresh(context);
              }
              addStoreSessionHandler(context, holder == null);
            } else {
              // Cannot find session - either it timed out, or was explicitly destroyed at the
              // server side on a
              // previous request.

              // OWASP clearly states that we shouldn't recreate the session as it allows
              // session fixation.
              // create a new anonymous session.
              createNewSession(context);
            }
          } else {
            context.fail(res.cause());
          }
          context.next();
        });
        return;
      }
    }
    // requirements were not met, so a anonymous session is created.
    createNewSession(context);
    context.next();
  }

  private void getSession(Vertx vertx, String sessionID, Handler<AsyncResult<Session>> resultHandler) {
    doGetSession(vertx, System.currentTimeMillis(), sessionID, resultHandler);
  }

  private void doGetSession(Vertx vertx, long startTime, String sessionID, Handler<AsyncResult<Session>> resultHandler) {
    sessionStore.get(sessionID, res -> {
      if (res.succeeded()) {
        if (res.result() == null) {
          // Can't find it so retry. This is necessary for clustered sessions as it can
          // take sometime for the session
          // to propagate across the cluster so if the next request for the session comes
          // in quickly at a different
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

  private void addStoreSessionHandler(RoutingContext context, boolean storeUser) {
    context.addHeadersEndHandler(v -> {
      boolean sessionUsed = context.isSessionAccessed();
      Session session = context.session();
      if (!session.isDestroyed()) {
        final int currentStatusCode = context.response().getStatusCode();
        // Store the session (only and only if there was no error)
        if (currentStatusCode >= 200 && currentStatusCode < 400) {
          // store the current user into the session
          if (storeUser) {
            // during the request the user might have been removed
            if (context.user() != null) {
              session.put(SESSION_USER_HOLDER_KEY, new UserHolder(context));
            }
          }

          if (session.isRegenerated()) {
            // this means that a session id has been changed, usually it means a session
            // upgrade
            // (e.g.: anonymous to authenticated) or that the security requirements have
            // changed
            // see:
            // https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Session_ID_Life_Cycle

            // the session cookie needs to be updated to the new id
            final Cookie cookie = sessionCookie(context, session);
            // restore defaults
            session.setAccessed();
            cookie
              .setValue(session.value())
              .setPath(sessionCookiePath)
              .setSecure(sessionCookieSecure)
              .setHttpOnly(sessionCookieHttpOnly);

            // we must invalidate the old id
            sessionStore.delete(session.oldId(), delete -> {
              if (delete.failed()) {
                log.error("Failed to delete previous session", delete.cause());
              } else {
                // we must wait for the result of the previous call in order to save the new one
                sessionStore.put(session, res -> {
                  if (res.failed()) {
                    log.error("Failed to store session", res.cause());
                  }
                });
              }
            });
          } else if (! lazySession || sessionUsed) {
            // if lazy mode activated, no need to store the session nor to create the session cookie if not used.
            sessionCookie(context, session);
            session.setAccessed();
            sessionStore.put(session, res -> {
              if (res.failed()) {
                log.error("Failed to store session", res.cause());
              }
            });
          }
        }
      } else {
        // invalidate the cookie as the session has been destroyed
        context.removeCookie(sessionCookieName);

        // if the session was regenerated in the request
        // the old id must also be removed
        if (session.isRegenerated()) {
          sessionStore.delete(session.oldId(), res -> {
            if (res.failed()) {
              log.error("Failed to delete old session", res.cause());
            }
          });
        }
        // delete from the storage
        sessionStore.delete(session.id(), res -> {
          if (res.failed()) {
            log.error("Failed to delete session", res.cause());
          }
        });
      }
    });
  }

  private void createNewSession(RoutingContext context) {
    Session session = sessionStore.createSession(sessionTimeout, minLength);
    context.setSession(session);
    context.removeCookie(sessionCookieName, false);
    addStoreSessionHandler(context, true);
  }

  private Cookie sessionCookie(final RoutingContext context, final Session session){
    Cookie cookie = context.getCookie(sessionCookieName);
    if(cookie != null) {
      return cookie;
    }
    cookie = Cookie.cookie(sessionCookieName, session.value());
    cookie.setPath(sessionCookiePath);
    cookie.setSecure(sessionCookieSecure);
    cookie.setHttpOnly(sessionCookieHttpOnly);
    cookie.setSameSite(cookieSameSite);
    // Don't set max age - it's a session cookie
    context.addCookie(cookie);
    return cookie;
  }
}
