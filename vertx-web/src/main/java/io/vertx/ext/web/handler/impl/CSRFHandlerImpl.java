/*
 * Copyright 2015 Red Hat, Inc.
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

import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.SessionHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class CSRFHandlerImpl implements CSRFHandler {

  private static final Logger log = LoggerFactory.getLogger(CSRFHandlerImpl.class);

  private static final Base64.Encoder BASE64 = Base64.getMimeEncoder();

  private final VertxContextPRNG random;
  private final Mac mac;

  private boolean nagHttps;
  private String cookieName = DEFAULT_COOKIE_NAME;
  private String cookiePath = DEFAULT_COOKIE_PATH;
  private String headerName = DEFAULT_HEADER_NAME;
  private String responseBody = DEFAULT_RESPONSE_BODY;
  private long timeout = SessionHandler.DEFAULT_SESSION_TIMEOUT;

  public CSRFHandlerImpl(final Vertx vertx, final String secret) {
    try {
      random = VertxContextPRNG.current(vertx);
      mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CSRFHandler setCookieName(String cookieName) {
    this.cookieName = cookieName;
    return this;
  }

  @Override
  public CSRFHandler setCookiePath(String cookiePath) {
    this.cookiePath = cookiePath;
    return this;
  }

  @Override
  public CSRFHandler setHeaderName(String headerName) {
    this.headerName = headerName;
    return this;
  }

  @Override
  public CSRFHandler setTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  @Override
  public CSRFHandler setNagHttps(boolean nag) {
    this.nagHttps = nag;
    return this;
  }

  @Override
  public CSRFHandler setResponseBody(String responseBody) {
    this.responseBody = responseBody;
    return this;
  }

  private String generateAndStoreToken(RoutingContext ctx) {
    byte[] salt = new byte[32];
    random.nextBytes(salt);

    String saltPlusToken = BASE64.encodeToString(salt) + "." + System.currentTimeMillis();
    String signature = BASE64.encodeToString(mac.doFinal(saltPlusToken.getBytes()));

    final String token = saltPlusToken + "." + signature;
    // a new token was generated add it to the cookie
    ctx.addCookie(Cookie.cookie(cookieName, token).setPath(cookiePath));

    return token;
  }

  private boolean validateRequest(RoutingContext ctx) {

    final Cookie cookie = ctx.getCookie(cookieName);

    if (cookie == null) {
      // quick abort
      return false;
    }

    // the challenge may be stored on the session already
    // in this case there we don't trust the user agent for it's
    // header or form value
    String challenge = getTokenFromSession(ctx);
    boolean invalidateSessionToken = false;

    if (challenge == null) {
      // fallback to header
      challenge = ctx.request().getHeader(headerName);
      if (challenge == null) {
        // fallback to form parameter
        challenge = ctx.request().getFormAttribute(headerName);
      }
    } else {
      // if the token is provided by the session object
      // we flag that we should invalidate it in case of success otherwise
      // we will allow replay attacks
      invalidateSessionToken = true;
    }

    // both the challenge and the cookie must be present, not null and equal
    if (challenge == null || !challenge.equals(cookie.getValue())) {
      return false;
    }

    String[] tokens = challenge.split("\\.");
    if (tokens.length != 3) {
      return false;
    }

    byte[] saltPlusToken = (tokens[0] + "." + tokens[1]).getBytes();
    synchronized (mac) {
      saltPlusToken = mac.doFinal(saltPlusToken);
    }
    String signature = BASE64.encodeToString(saltPlusToken);

    if(!signature.equals(tokens[2])) {
      return false;
    }

    try {
      // validate validity
      if (!(System.currentTimeMillis() > Long.parseLong(tokens[1]) + timeout)) {
        if (invalidateSessionToken) {
          // this token has been used and we discard it to avoid replay attacks
          ctx.session().remove(headerName);
        }
        return true;
      } else {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }
  }

  protected void forbidden(RoutingContext ctx) {
    final int statusCode = 403;
    if (responseBody != null) {
      ctx.response()
        .setStatusCode(statusCode)
        .end(responseBody);
    } else {
      ctx.fail(new HttpStatusException(statusCode, ERROR_MESSAGE));
    }
  }

  private String getTokenFromSession(RoutingContext ctx) {
    Session session = ctx.session();
    if (session == null) {
      return null;
    }
    // get the token from the session
    String sessionToken = session.get(headerName);
    if (sessionToken != null) {
      // attempt to parse the value
      int idx = sessionToken.indexOf('/');
      if (idx != -1 && session.id() != null && session.id().equals(sessionToken.substring(0, idx))) {
        return sessionToken.substring(idx + 1);
      }
    }
    // fail
    return null;
  }

  @Override
  public void handle(RoutingContext ctx) {

    if (nagHttps) {
      String uri = ctx.request().absoluteURI();
      if (uri != null && !uri.startsWith("https:")) {
        log.warn("Using session cookies without https could make you susceptible to session hijacking: " + uri);
      }
    }

    HttpMethod method = ctx.request().method();
    Session session = ctx.session();

    switch (method) {
      case GET:
        final String token;
        if (session == null) {
          // if there's no session to store values, tokens are issued on every request
          token = generateAndStoreToken(ctx);
        } else {
          // get the token from the session, this also considers the fact
          // that the token might be invalid as it was issued for a previous session id
          // session id's change on session upgrades (unauthenticated -> authenticated; role change; etc...)
          String sessionToken = getTokenFromSession(ctx);
          // when there's no token in the session, then we behave just like when there is no session
          // create a new token, but we also store it in the session for the next runs
          if (sessionToken == null) {
            token = generateAndStoreToken(ctx);
            // storing will include the session id too. The reason is that if a session is upgraded
            // we don't want to allow the token to be valid anymore
            session.put(headerName, session.id() + "/" + token);
          } else {
            // we're still on the same session, no need to regenerate the token
            token = sessionToken;
            // in this case specifically we don't issue the token as it is unchanged
            // the user agent still has it from the previous interaction.
          }
        }
        // put the token in the context for users who prefer to render the token directly on the HTML
        ctx.put(headerName, token);
        ctx.next();
        break;
      case POST:
      case PUT:
      case DELETE:
      case PATCH:
        if (validateRequest(ctx)) {
          ctx.next();
        } else {
          forbidden(ctx);
        }
        break;
      default:
        // ignore other methods
        ctx.next();
        break;
    }
  }
}
