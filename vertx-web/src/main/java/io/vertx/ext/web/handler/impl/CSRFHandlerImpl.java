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

import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.SessionHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;


/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class CSRFHandlerImpl implements CSRFHandler {

  private static final Logger log = LoggerFactory.getLogger(CSRFHandlerImpl.class);

  private static final Base64.Encoder BASE64 = Base64.getMimeEncoder();

  private final Random RAND = new SecureRandom();
  private final Mac mac;

  private boolean nagHttps;
  private String cookieName = DEFAULT_COOKIE_NAME;
  private String cookiePath = DEFAULT_COOKIE_PATH;
  private String headerName = DEFAULT_HEADER_NAME;
  private String responseBody = DEFAULT_RESPONSE_BODY;
  private long timeout = SessionHandler.DEFAULT_SESSION_TIMEOUT;

  public CSRFHandlerImpl(final String secret) {
    try {
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

  private String generateToken() {
    byte[] salt = new byte[32];
    RAND.nextBytes(salt);

    String saltPlusToken = BASE64.encodeToString(salt) + "." + Long.toString(System.currentTimeMillis());
    String signature = BASE64.encodeToString(mac.doFinal(saltPlusToken.getBytes()));

    return saltPlusToken + "." + signature;
  }

  private boolean validateToken(String header, Cookie cookie) {
    // both the header and the cookie must be present, not null and equal
    if (header == null || cookie == null || !header.equals(cookie.getValue())) {
      return false;
    }

    String[] tokens = header.split("\\.");
    if (tokens.length != 3) {
      return false;
    }

    String saltPlusToken = tokens[0] + "." + tokens[1];
    String signature = BASE64.encodeToString(mac.doFinal(saltPlusToken.getBytes()));

    if(!signature.equals(tokens[2])) {
      return false;
    }

    try {
      // validate validity
      return !(System.currentTimeMillis() > Long.parseLong(tokens[1]) + timeout);
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
      ctx.fail(statusCode);
    }
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

    switch (method) {
      case GET:
        final String token = generateToken();
        // put the token in the context for users who prefer to render the token directly on the HTML
        ctx.put(headerName, token);
        ctx.addCookie(Cookie.cookie(cookieName, token).setPath(cookiePath));
        ctx.next();
        break;
      case POST:
      case PUT:
      case DELETE:
      case PATCH:
        final String header = ctx.request().getHeader(headerName);
        final Cookie cookie = ctx.getCookie(cookieName);
        if (validateToken(header == null ? ctx.request().getFormAttribute(headerName) : header, cookie)) {
          ctx.next();
        } else {
          forbidden(ctx);
        }
        break;
      default:
        // ignore these methods
        ctx.next();
        break;
    }
  }
}
