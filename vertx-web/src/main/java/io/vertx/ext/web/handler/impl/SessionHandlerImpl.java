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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.impl.SessionInternal;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SessionHandlerImpl implements SessionHandler {

  public static final String SESSION_USER_HOLDER_KEY = "__vertx.userHolder";
  public static final String SESSION_FLUSHED_KEY = "__vertx.session-flushed";
  public static final String SESSION_STOREUSER_KEY = "__vertx.session-storeuser";

  private static final Logger LOG = LoggerFactory.getLogger(SessionHandlerImpl.class);

  private final SessionStore sessionStore;

  private String sessionCookieName = DEFAULT_SESSION_COOKIE_NAME;
  private String sessionCookiePath = DEFAULT_SESSION_COOKIE_PATH;
  private long sessionTimeout = DEFAULT_SESSION_TIMEOUT;
  private boolean nagHttps = DEFAULT_NAG_HTTPS;
  private boolean sessionCookieSecure = DEFAULT_COOKIE_SECURE_FLAG;
  private boolean sessionCookieHttpOnly = DEFAULT_COOKIE_HTTP_ONLY_FLAG;
  private int minLength = DEFAULT_SESSIONID_MIN_LENGTH;
  private boolean lazySession = DEFAULT_LAZY_SESSION;
  private long cookieMaxAge = -1;

  private boolean cookieless;
  private CookieSameSite cookieSameSite;

  public SessionHandlerImpl(SessionStore sessionStore) {
    this.sessionStore = sessionStore;
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
  public SessionHandler setCookieMaxAge(long cookieMaxAge) {
    this.cookieMaxAge = cookieMaxAge;
    return this;
  }

  @Override
  @Deprecated
  public SessionHandler setAuthProvider(AuthProvider authProvider) {
    return this;
  }

  @Override
  public SessionHandler setCookieless(boolean cookieless) {
    this.cookieless = cookieless;
    return this;
  }

  @Override
  public SessionHandler flush(RoutingContext context, Handler<AsyncResult<Void>> handler) {
    return flush(context, false, false, handler);
  }

  @Override
  public SessionHandler flush(RoutingContext context, boolean ignoreStatus, Handler<AsyncResult<Void>> handler) {
    return flush(context, false, ignoreStatus, handler);
  }

  /**
   * Ensure that the cookie properties are always set the same way
   * on generation and on update.
   *
   * @param cookie the cookie to set
   */
  private void setCookieProperties(Cookie cookie) {
    cookie.setPath(sessionCookiePath);
    cookie.setSecure(sessionCookieSecure);
    cookie.setHttpOnly(sessionCookieHttpOnly);
    cookie.setSameSite(cookieSameSite);
    // set max age if user requested it - else it's a session cookie
    if (cookieMaxAge >= 0) {
      cookie.setMaxAge(cookieMaxAge);
    }
  }

  private SessionHandler flush(RoutingContext context, boolean skipCrc, boolean ignoreStatus, Handler<AsyncResult<Void>> handler) {
    boolean sessionUsed = context.isSessionAccessed();
    Session session = context.session();
    if (!session.isDestroyed()) {
      final int currentStatusCode = context.response().getStatusCode();
      // Store the session (only and only if there was no error)
      if (ignoreStatus || (currentStatusCode >= 200 && currentStatusCode < 400)) {
        // store the current user into the session
        Boolean storeUser = context.get(SESSION_STOREUSER_KEY);
        if (storeUser != null && storeUser) {
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

          if (cookieless) {
            // restore defaults
            session.setAccessed();
          } else {
            // the session cookie needs to be updated to the new id
            final Cookie cookie = sessionCookie(context, session);
            // restore defaults
            session.setAccessed();
            cookie.setValue(session.value());
            setCookieProperties(cookie);
          }

          // we must invalidate the old id
          sessionStore.delete(session.oldId(), delete -> {
            if (delete.failed()) {
              handler.handle(Future.failedFuture(delete.cause()));
            } else {
              // we must wait for the result of the previous call in order to save the new one
              sessionStore.put(session, put -> {
                if (put.failed()) {
                  handler.handle(Future.failedFuture(put.cause()));
                } else {
                  context.put(SESSION_FLUSHED_KEY, true);
                  if (session instanceof SessionInternal) {
                    ((SessionInternal) session).flushed(skipCrc);
                  }
                  handler.handle(Future.succeededFuture());
                }
              });
            }
          });
        } else if (!lazySession || sessionUsed) {
          if (!cookieless) {
            // if lazy mode activated, no need to store the session nor to create the session cookie if not used.
            sessionCookie(context, session);
          }
          session.setAccessed();
          sessionStore.put(session, put -> {
            if (put.failed()) {
              handler.handle(Future.failedFuture(put.cause()));
            } else {
              context.put(SESSION_FLUSHED_KEY, true);
              if (session instanceof SessionInternal) {
                ((SessionInternal) session).flushed(skipCrc);
              }
              handler.handle(Future.succeededFuture());
            }
          });
        }
      }
    } else {
      if (!cookieless) {
        // invalidate the cookie as the session has been destroyed
        context.removeCookie(sessionCookieName);
      }
      // if the session was regenerated in the request
      // the old id must also be removed
      if (session.isRegenerated()) {
        sessionStore.delete(session.oldId(), delete -> {
          if (delete.failed()) {
            handler.handle(Future.failedFuture(delete.cause()));
          } else {
            // delete from the storage
            sessionStore.delete(session.id(), delete2 -> {
              if (delete2.failed()) {
                handler.handle(Future.failedFuture(delete2.cause()));
              } else {
                context.put(SESSION_FLUSHED_KEY, true);
                handler.handle(Future.succeededFuture());
              }
            });
          }
        });
      } else {
        // delete from the storage
        sessionStore.delete(session.id(), delete -> {
          if (delete.failed()) {
            handler.handle(Future.failedFuture(delete.cause()));
          } else {
            context.put(SESSION_FLUSHED_KEY, true);
            handler.handle(Future.succeededFuture());
          }
        });
      }
    }
    return this;
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (nagHttps && LOG.isDebugEnabled()) {
      String uri = request.absoluteURI();
      if (!uri.startsWith("https:")) {
        LOG.debug(
          "Using session cookies without https could make you susceptible to session hijacking: " + uri);
      }
    }

    // Look for existing session id
    String sessionID = getSessionId(context);
    if (sessionID != null && sessionID.length() > minLength) {
      // before starting any potential async operation here
      // pause parsing the request body. The reason is that
      // we don't want to loose the body or protocol upgrades
      // for async operations
      final boolean parseEnded = request.isEnded();
      if (!parseEnded) {
        request.pause();
      }
      // we passed the OWASP min length requirements
      getSession(context.vertx(), sessionID, res -> {
        if (!parseEnded && !request.headers().contains(HttpHeaders.UPGRADE, HttpHeaders.WEBSOCKET, true)) {
          request.resume();
        }
        if (res.succeeded()) {
          Session session = res.result();
          if (session != null) {
            context.setSession(session);
            // attempt to load the user from the session
            UserHolder holder = session.get(SESSION_USER_HOLDER_KEY);
            if (holder != null) {
              holder.refresh(context);
            } else {
              // signal we must store the user to link it to the
              // session as it wasn't found
              context.put(SESSION_STOREUSER_KEY, true);
            }
            addStoreSessionHandler(context);
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
    } else {
      // requirements were not met, so a anonymous session is created.
      createNewSession(context);
      context.next();
    }
  }

  public Session newSession(RoutingContext context) {
    Session session = sessionStore.createSession(sessionTimeout, minLength);
    context.setSession(session);
    if (!cookieless) {
      context.removeCookie(sessionCookieName, false);
    }
    // it's a new session we must store the user too otherwise it won't be linked
    context.put(SESSION_STOREUSER_KEY, true);
    flush(context, true, true, flush -> {
      if (flush.failed()) {
        log.warn("Failed to flush the session to the underlying store", flush.cause());
      }
    });
    return session;
  }

  private String getSessionId(RoutingContext  context) {
    if (cookieless) {
      // cookieless sessions store the session on the path or the request
      // a session is identified by a sequence of characters between braces
      String path = context.normalizedPath();
      int s = -1;
      int e = -1;
      for (int i = 0; i < path.length(); i++) {
        if (path.charAt(i) == '(') {
          s = i + 1;
          continue;
        }
        if (path.charAt(i) == ')') {
          // if not open parenthesis yet
          // this is a false end, continue looking
          if (s != -1) {
            e = i;
            break;
          }
        }
      }
      if (s != -1 && e != -1 && s < e) {
        return path.substring(s, e);
      }
    } else {
      Cookie cookie = context.getCookie(sessionCookieName);
      if (cookie != null) {
        // Look up sessionId
        return cookie.getValue();
      }
    }

    return null;
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

  private void addStoreSessionHandler(RoutingContext context) {
    context.addHeadersEndHandler(v -> {
      // skip flush if we already flushed
      Boolean flushed = context.get(SESSION_FLUSHED_KEY);
      if (flushed == null || !flushed) {
        flush(context, true, false, flush -> {
          if (flush.failed()) {
            LOG.warn("Failed to flush the session to the underlying store", flush.cause());
          }
        });
      }
    });
  }

  private void createNewSession(RoutingContext context) {
    Session session = sessionStore.createSession(sessionTimeout, minLength);
    context.setSession(session);
    if (!cookieless) {
      context.removeCookie(sessionCookieName, false);
    }
    // it's a new session we must store the user too otherwise it won't be linked
    context.put(SESSION_STOREUSER_KEY, true);
    addStoreSessionHandler(context);
  }

  private Cookie sessionCookie(final RoutingContext context, final Session session) {
    Cookie cookie = context.getCookie(sessionCookieName);
    if (cookie != null) {
      return cookie;
    }
    cookie = Cookie.cookie(sessionCookieName, session.value());
    setCookieProperties(cookie);
    context.addCookie(cookie);
    return cookie;
  }
}
