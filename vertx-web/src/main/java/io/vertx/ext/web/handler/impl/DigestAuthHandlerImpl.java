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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.htdigest.HtdigestAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.DigestAuthHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class DigestAuthHandlerImpl extends AuthorizationAuthHandler implements DigestAuthHandler {

  private static class Nonce {
    private final long createdAt;
    private int count;

    Nonce() {
      createdAt = System.currentTimeMillis();
      count = 0;
    }
  }

  private static final Pattern PARSER = Pattern.compile("(\\w+)=[\"]?([^\"]*)[\"]?$");
  private static final Pattern SPLITTER = Pattern.compile(",(?=(?:[^\"]|\"[^\"]*\")*$)");

  private static final MessageDigest MD5;

  static {
    try {
      MD5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private final SecureRandom random = new SecureRandom();
  private final Map<String, Nonce> nonces = new HashMap<>();

  private final long nonceExpireTimeout;

  private long lastExpireRun;

  public DigestAuthHandlerImpl(HtdigestAuth authProvider, long nonceExpireTimeout) {
    super(authProvider, authProvider.realm(), Type.DIGEST);
    this.nonceExpireTimeout = nonceExpireTimeout;
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    // clean up nonce
    long now = System.currentTimeMillis();
    if (now - lastExpireRun > nonceExpireTimeout / 2) {
      nonces.entrySet().removeIf(entry -> entry.getValue().createdAt + nonceExpireTimeout < now);
      lastExpireRun = now;
    }

    parseAuthorization(context, false, parseAuthorization -> {
      if (parseAuthorization.failed()) {
        handler.handle(Future.failedFuture(parseAuthorization.cause()));
        return;
      }

      final JsonObject authInfo = new JsonObject();

      try {
        // Split the parameters by comma.
        String[] tokens = SPLITTER.split(parseAuthorization.result());
        // Parse parameters.
        int i = 0;
        int len = tokens.length;

        while (i < len) {
          // Strip quotes and whitespace.
          Matcher m = PARSER.matcher(tokens[i]);
          if (m.find()) {
            authInfo.put(m.group(1), m.group(2));
          }

          ++i;
        }

        final String nonce = authInfo.getString("nonce");

        // check for expiration
        if (!nonces.containsKey(nonce)) {
          handler.handle(Future.failedFuture(UNAUTHORIZED));
          return;
        }

        // check for nonce counter (prevent replay attack
        if (authInfo.containsKey("qop")) {
          int nc = Integer.parseInt(authInfo.getString("nc"));
          final Nonce n = nonces.get(nonce);
          if (nc <= n.count) {
            handler.handle(Future.failedFuture(UNAUTHORIZED));
            return;
          }
          n.count = nc;
        }
      } catch (RuntimeException e) {
        handler.handle(Future.failedFuture(e));
      }

      // validate the opaque value
      final Session session = context.session();
      if (session != null) {
        String opaque = (String) session.data().get("opaque");
        if (opaque != null && !opaque.equals(authInfo.getString("opaque"))) {
          handler.handle(Future.failedFuture(UNAUTHORIZED));
          return;
        }
      }

      // we now need to pass some extra info
      authInfo.put("method", context.request().method().name());

      handler.handle(Future.succeededFuture(authInfo));
    });
  }

  @Override
  protected String authenticateHeader(RoutingContext context) {
    final byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    // generate nonce
    String nonce = md5(bytes);
    // save it
    nonces.put(nonce, new Nonce());

    // generate opaque
    String opaque = null;
    final Session session = context.session();
    if (session != null) {
      opaque = (String) session.data().get("opaque");
    }

    if (opaque == null) {
      random.nextBytes(bytes);
      // generate random opaque
      opaque = md5(bytes);
    }

    return "Digest realm=\"" + realm + "\", qop=\"auth\", nonce=\"" + nonce + "\", opaque=\"" + opaque + "\"";
  }

  private final static char[] hexArray = "0123456789abcdef".toCharArray();

  private static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  private static synchronized String md5(byte[] payload) {
    MD5.reset();
    return bytesToHex(MD5.digest(payload));
  }
}
