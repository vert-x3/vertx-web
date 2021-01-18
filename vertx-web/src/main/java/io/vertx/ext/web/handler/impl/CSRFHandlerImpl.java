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
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.impl.Origin;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
  private long timeout = SessionHandler.DEFAULT_SESSION_TIMEOUT;

  private Origin origin;
  private boolean httpOnly;

  public CSRFHandlerImpl(final Vertx vertx, final String secret) {
    try {
      if (secret.length() <= 8) {
        log.warn("CSRF secret is very short (<= 8 bytes)");
      }
      random = VertxContextPRNG.current(vertx);
      mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CSRFHandler setOrigin(String origin) {
    this.origin = Origin.parse(origin);
    return this;
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
  public CSRFHandler setCookieHttpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly;
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

  private String generateAndStoreToken(RoutingContext ctx) {
    byte[] salt = new byte[32];
    random.nextBytes(salt);

    String saltPlusToken = BASE64.encodeToString(salt) + "." + System.currentTimeMillis();
    String signature = BASE64.encodeToString(mac.doFinal(saltPlusToken.getBytes(StandardCharsets.US_ASCII)));

    final String token = saltPlusToken + "." + signature;
    // a new token was generated add it to the cookie
    ctx.addCookie(
      Cookie.cookie(cookieName, token)
        .setPath(cookiePath)
        .setHttpOnly(httpOnly)
        // it's not an option to change the same site policy
        .setSameSite(CookieSameSite.STRICT));

    return token;
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

  /**
   * Check if a string is null or empty (including containing only spaces)
   *
   * @param s Source string
   * @return TRUE if source string is null or empty (including containing only spaces)
   */
  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  private static long parseLong(String s) {
    if (isBlank(s)) {
      return -1;
    }

    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      log.trace("Invalid Token format", e);
      // fallback as the token is expired
      return -1;
    }
  }

  private boolean isValidOrigin(RoutingContext  ctx) {
    /* Verifying Same Origin with Standard Headers */
    if (origin != null) {
      //Try to get the source from the "Origin" header
      String source = ctx.request().getHeader(HttpHeaders.ORIGIN);
      if (isBlank(source)) {
        //If empty then fallback on "Referer" header
        source = ctx.request().getHeader(HttpHeaders.REFERER);
        //If this one is empty too then we trace the event and we block the request (recommendation of the article)...
        if (isBlank(source)) {
          log.trace("ORIGIN and REFERER request headers are both absent/empty");
          return false;
        }
      }

      //Compare the source against the expected target origin
      if (!origin.sameOrigin(source)) {
        //One the part do not match so we trace the event and we block the request
        log.trace("Protocol/Host/Port do not fully match");
        return false;
      }
    }
    // no configured origin or origin is valid
    return true;
  }

  private boolean isValidRequest(RoutingContext ctx) {

    /* Verifying CSRF token using "Double Submit Cookie" approach */
    final Cookie cookie = ctx.getCookie(cookieName);

    String header = ctx.request().getHeader(headerName);
    if (header == null) {
      // fallback to form attributes
      header = ctx.request().getFormAttribute(headerName);
    }

    // both the header and the cookie must be present, not null and not empty
    if (header == null || cookie == null || isBlank(header) || isBlank(cookie.getValue())) {
      log.trace("Token provided via HTTP Header/Form is absent/empty");
      return false;
    }

    //Verify that token from header and one from cookie are the same
    if (!header.equals(cookie.getValue())) {
      log.trace("Token provided via HTTP Header and via Cookie are not equal");
      return false;
    }

    if (ctx.session() != null) {
      Session session = ctx.session();

      // get the token from the session
      String sessionToken = session.get(headerName);
      if (sessionToken != null) {
        // attempt to parse the value
        int idx = sessionToken.indexOf('/');
        if (idx != -1 && session.id() != null && session.id().equals(sessionToken.substring(0, idx))) {
          String challenge = sessionToken.substring(idx + 1);
          // the challenge must match the user-agent input
          if (!challenge.equals(header)) {
            log.trace("Token has been used or is outdated");
            return false;
          }
        } else {
          log.trace("Token has been issued for a different session");
          return false;
        }
      } else {
        log.trace("No Token has been added to the session");
        return false;
      }
    }

    String[] tokens = header.split("\\.");
    if (tokens.length != 3) {
      return false;
    }

    byte[] saltPlusToken = (tokens[0] + "." + tokens[1]).getBytes(StandardCharsets.US_ASCII);

    synchronized (mac) {
      saltPlusToken = mac.doFinal(saltPlusToken);
    }

    String signature = BASE64.encodeToString(saltPlusToken);

    if(!signature.equals(tokens[2])) {
      log.trace("Token signature does not match");
      return false;
    }

    // this token has been used and we discard it to avoid replay attacks
    if (ctx.session() != null) {
      ctx.session().remove(headerName);
    }

    final long ts = parseLong(tokens[1]);

    if (ts == -1) {
      return false;
    }

    // validate validity
    return !(System.currentTimeMillis() > ts + timeout);
  }

  @Override
  public void handle(RoutingContext ctx) {

    if (nagHttps) {
      String uri = ctx.request().absoluteURI();
      if (uri != null && !uri.startsWith("https:")) {
        log.trace("Using session cookies without https could make you susceptible to session hijacking: " + uri);
      }
    }

    HttpMethod method = ctx.request().method();
    Session session = ctx.session();

    // if we're being strict with the origin
    // ensure that they are always valid
    if (!isValidOrigin(ctx)) {
      ctx.fail(403);
      return;
    }

    switch (method.name()) {
      case "GET":
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
            String[] parts = sessionToken.split("\\.");
            final long ts = parseLong(parts[1]);

            if (ts == -1) {
              // fallback as the token is expired
              token = generateAndStoreToken(ctx);
            } else {
              if (!(System.currentTimeMillis() > ts + timeout)) {
                // we're still on the same session, no need to regenerate the token
                // also note that the token isn't expired, so it can be reused
                token = sessionToken;
                // in this case specifically we don't issue the token as it is unchanged
                // the user agent still has it from the previous interaction.
              } else {
                // fallback as the token is expired
                token = generateAndStoreToken(ctx);
              }
            }
          }
        }
        // put the token in the context for users who prefer to render the token directly on the HTML
        ctx.put(headerName, token);
        ctx.next();
        break;
      case "POST":
      case "PUT":
      case "DELETE":
      case "PATCH":
        if (isValidRequest(ctx)) {
          // it matches, so refresh the token to avoid replay attacks
          token = generateAndStoreToken(ctx);
          // put the token in the context for users who prefer to
          // render the token directly on the HTML
          ctx.put(headerName, token);
          ctx.next();
        } else {
          ctx.fail(403);
        }
        break;
      default:
        // ignore other methods
        ctx.next();
        break;
    }
  }
}
