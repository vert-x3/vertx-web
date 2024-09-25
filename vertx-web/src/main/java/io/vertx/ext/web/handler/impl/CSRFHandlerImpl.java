/*
 * Copyright 2024 Red Hat, Inc.
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
import io.vertx.core.VertxException;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.prng.VertxContextPRNG;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.impl.Origin;
import io.vertx.ext.web.impl.RoutingContextInternal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static io.vertx.ext.auth.impl.Codec.base64UrlEncode;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class CSRFHandlerImpl implements CSRFHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CSRFHandlerImpl.class);

  private final VertxContextPRNG random;
  private final Mac mac;

  private boolean nagHttps;
  private String cookieName = DEFAULT_COOKIE_NAME;
  private String cookiePath = DEFAULT_COOKIE_PATH;
  private String headerName = DEFAULT_HEADER_NAME;
  private long timeout = SessionHandler.DEFAULT_SESSION_TIMEOUT;

  private Origin origin;
  private boolean httpOnly;
  private boolean cookieSecure;

  public CSRFHandlerImpl(final Vertx vertx, final String secret) {
    try {
      if (secret.length() <= 8) {
        LOG.warn("CSRF secret is very short (<= 8 bytes)");
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
  public CSRFHandler setCookieSecure(boolean secure) {
    this.cookieSecure = secure;
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

    String saltPlusToken = base64UrlEncode(salt) + "." + System.currentTimeMillis();
    String signature = base64UrlEncode(mac.doFinal(saltPlusToken.getBytes(StandardCharsets.US_ASCII)));

    final String token = saltPlusToken + "." + signature;

    Session session = ctx.session();
    if (session != null) {
      // storing will include the session id too. The reason is that if a session is upgraded
      // we don't want to allow the token to be valid anymore
      session.put(headerName, session.id() + "/" + token);
    }

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
      LOG.trace("Invalid Token format", e);
      // fallback as the token is expired
      return -1;
    }
  }

  @Override
  public void handle(RoutingContext ctx) {

    // we need to keep state since we can be called again on reroute
    if (!((RoutingContextInternal) ctx).seenHandler(RoutingContextInternal.CSRF_HANDLER)) {
      ((RoutingContextInternal) ctx).visitHandler(RoutingContextInternal.CSRF_HANDLER);
    } else {
      ctx.next();
      return;
    }

    if (nagHttps && LOG.isTraceEnabled()) {
      String uri = ctx.request().absoluteURI();
      if (uri != null && !uri.startsWith("https:")) {
        LOG.trace("Using session cookies without https could make you susceptible to session hijacking: " + uri);
      }
    }

    // If we're being strict with the origin
    // ensure that they are always valid
    if (!Origin.check(origin, ctx)) {
      ctx.fail(403, new VertxException("Invalid Origin", true));
      return;
    }

    String methodName = ctx.request().method().name();
    switch (methodName) {
      case "HEAD":
      case "GET":
        handleSafeMethod(ctx);
        break;
      case "POST":
      case "PUT":
      case "DELETE":
      case "PATCH":
        handleUnsafeMethod(ctx);
        break;
      default:
        // ignore other methods
        ctx.next();
        break;
    }
  }

  private void handleSafeMethod(RoutingContext ctx) {
    boolean sendCookie = true;
    Session session = ctx.session();
    String token;
    if (session == null) {
      // if there's no session to store values, tokens are issued on every request
      token = generateAndStoreToken(ctx);
    } else {
      // Get the token from the session, this also considers the fact
      // that the token might be invalid as it was issued for a previous session id.
      // Session ids change on session upgrades (unauthenticated -> authenticated; role change; etc...)
      String sessionToken = getTokenFromSession(ctx);
      // When there's no token in the session, then we behave just like when there is no session.
      // Create a new token, but we also store it in the session for the next runs.
      if (sessionToken == null) {
        token = generateAndStoreToken(ctx);
      } else {
        String[] parts = sessionToken.split("\\.");
        final long ts = parseLong(parts[1]);

        if (ts == -1) {
          // fallback as the token is expired
          token = generateAndStoreToken(ctx);
        } else {
          if (!(System.currentTimeMillis() > ts + timeout)) {
            // We're still on the same session, and the token hasn't expired so it can be reused.
            token = sessionToken;
            // If a previous unsafe interaction succeeded, but the response didn't get to the client
            // (e.g. the response headers were sent but not the response body), the session token may be different.
            // In this case, we send it again to synchronize with the client.
            // Otherwise, it's not necessary to send it again.
            Cookie cookie = ctx.request().getCookie(cookieName);
            if (cookie != null && token.equals(cookie.getValue())) {
              sendCookie = false;
            }
          } else {
            // Fallback as the token is expired
            token = generateAndStoreToken(ctx);
          }
        }
      }
    }
    synchronizeWithClient(ctx, token, sendCookie);
    ctx.next();
  }

  private void handleUnsafeMethod(RoutingContext ctx) {
    /* Verifying CSRF token using "Double Submit Cookie" approach */
    final Cookie cookie = ctx.request().getCookie(cookieName);

    String header = ctx.request().getHeader(headerName);
    if (header == null) {
      // fallback to form attributes
      if (ctx.body().available()) {
        header = ctx.request().getFormAttribute(headerName);
      } else {
        ctx.fail(new VertxException("BodyHandler is required to process POST requests", true));
        return;
      }
    }

    // both the header and the cookie must be present, not null and not empty
    if (header == null || cookie == null || isBlank(header)) {
      ctx.fail(403, new IllegalArgumentException("Token provided via HTTP Header/Form is absent/empty"));
      return;
    }

    final String cookieValue = cookie.getValue();

    if (cookieValue == null || isBlank(cookieValue)) {
      ctx.fail(403, new IllegalArgumentException("Token provided via HTTP Header/Form is absent/empty"));
      return;
    }

    final byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
    final byte[] cookieBytes = cookieValue.getBytes(StandardCharsets.UTF_8);

    //Verify that token from header and one from cookie are the same
    if (!MessageDigest.isEqual(headerBytes, cookieBytes)) {
      ctx.fail(403, new IllegalArgumentException("Token provided via HTTP Header and via Cookie are not equal"));
      return;
    }

    final Session session = ctx.session();

    if (session != null) {
      // get the token from the session
      String sessionToken = session.get(headerName);
      if (sessionToken != null) {
        // attempt to parse the value
        int idx = sessionToken.indexOf('/');
        if (idx != -1 && session.id() != null && session.id().equals(sessionToken.substring(0, idx))) {
          String challenge = sessionToken.substring(idx + 1);
          // the challenge must match the user-agent input
          if (!MessageDigest.isEqual(challenge.getBytes(StandardCharsets.UTF_8), headerBytes)) {
            ctx.fail(403, new IllegalArgumentException("Token has been used or is outdated"));
            return;
          }
        } else {
          ctx.fail(403, new IllegalArgumentException("Token has been issued for a different session"));
          return;
        }
      } else {
        ctx.fail(403, new IllegalArgumentException("No Token has been added to the session"));
        return;
      }
    }

    // if the token has expired remove the token from the session so that a new one can be acquired by a fresh GET
    // provided the user is authenticated.
    // We cannot simply remove it before these checks as this will invalidate the token even if the response is never
    // written, requiring the user to GET another token even though the previous was valid
    String[] tokens = header.split("\\.");
    if (tokens.length != 3) {
      if (session != null) {
        session.remove(headerName);
      }
      ctx.fail(403);
      return;
    }

    byte[] saltPlusToken = (tokens[0] + "." + tokens[1]).getBytes(StandardCharsets.US_ASCII);

    synchronized (mac) {
      saltPlusToken = mac.doFinal(saltPlusToken);
    }

    final byte[] signature = base64UrlEncode(saltPlusToken).getBytes(StandardCharsets.US_ASCII);

    if (!MessageDigest.isEqual(signature, tokens[2].getBytes(StandardCharsets.US_ASCII))) {
      ctx.fail(403, new IllegalArgumentException("Token signature does not match"));
      return;
    }

    final long ts = parseLong(tokens[1]);

    if (ts == -1) {
      if (session != null) {
        session.remove(headerName);
      }
      ctx.fail(403);
      return;
    }

    // validate validity
    if (System.currentTimeMillis() > ts + timeout) {
      if (session != null) {
        session.remove(headerName);
      }
      ctx.fail(403, new IllegalArgumentException("CSRF validity expired"));
      return;
    }

    // The token matches, so refresh it to avoid replay attacks
    String token = generateAndStoreToken(ctx);
    synchronizeWithClient(ctx, token, true);
    ctx.next();
  }

  private void synchronizeWithClient(RoutingContext ctx, String token, boolean cookie) {
    if (cookie) {
      ctx.response()
        .addCookie(
          Cookie.cookie(cookieName, token)
            .setPath(cookiePath)
            .setHttpOnly(httpOnly)
            .setSecure(cookieSecure)
            // it's not an option to change the same site policy
            .setSameSite(CookieSameSite.STRICT));
    }
    // Put the token in the context for users who prefer to render the token directly on the HTML
    ctx.put(headerName, token);
  }
}
