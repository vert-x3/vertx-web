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

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.htdigest.HtdigestAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.DigestAuthHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class DigestAuthHandlerImpl extends AuthHandlerImpl implements DigestAuthHandler {

  private static class Nonce {
    private final long createdAt;
    private int count;

    Nonce() {
      createdAt = System.currentTimeMillis();
      count = 0;
    }
  }

  private static final Pattern PARSER = Pattern.compile("(\\w+)=[\"]?([^\"]*)[\"]?$");
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
    super(authProvider);
    this.nonceExpireTimeout = nonceExpireTimeout;
  }

  @Override
  public void handle(RoutingContext context) {
    // clean up nonce
    long now = System.currentTimeMillis();
    if (now - lastExpireRun > nonceExpireTimeout / 2) {
      for(Iterator<Map.Entry<String,Nonce>> it = nonces.entrySet().iterator(); it.hasNext();){
        Map.Entry<String, Nonce> entry = it.next();
        if (entry.getValue().createdAt + nonceExpireTimeout < now) {
          it.remove();
        }
      }

      lastExpireRun = now;
    }

    User user = context.user();
    if (user != null) {
      // Already authenticated in, just authorise
      authorise(user, context);
    } else {
      HttpServerRequest request = context.request();
      String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);

      if (authorization == null) {
        handle401(context);
      } else {
        try {
          String sscheme;
          int idx = authorization.indexOf(' ');

          if (idx <= 0) {
            context.fail(400);
            return;
          }

          sscheme = authorization.substring(0, idx);

          if (!"Digest".equalsIgnoreCase(sscheme)) {
            context.fail(400);
            return;
          }

          JsonObject authInfo = new JsonObject();
          // Split the parameters by comma.
          String[] tokens = authorization.substring(idx).split(",(?=(?:[^\"]|\"[^\"]*\")*$)");
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
            handle401(context);
            return;
          }

          // check for nonce counter (prevent replay attack
          if (authInfo.containsKey("qop")) {
            Integer nc = Integer.parseInt(authInfo.getString("nc"));
            final Nonce n = nonces.get(nonce);
            if (nc <= n.count) {
              handle401(context);
              return;
            }
            n.count = nc;
          }

          // validate the opaque value
          final Session session = context.session();
          if (session != null) {
            String opaque = (String) session.data().get("opaque");
            if (opaque != null && !opaque.equals(authInfo.getString("opaque"))) {
              handle401(context);
              return;
            }
          }

          // we now need to pass some extra info
          authInfo.put("method", context.request().method().name());

          authProvider.authenticate(authInfo, res -> {
            if (res.succeeded()) {
              User authenticated = res.result();
              context.setUser(authenticated);
              authorise(authenticated, context);
              // TODO: refresh session id
            } else {
              handle401(context);
            }
          });

        } catch (ArrayIndexOutOfBoundsException e) {
          handle401(context);
        } catch (IllegalArgumentException | NullPointerException e) {
          // IllegalArgumentException includes PatternSyntaxException and NumberFormatException
          context.fail(e);
        }
      }
    }
  }

  private void handle401(RoutingContext context) {
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

    context.response().putHeader("WWW-Authenticate", "Digest realm=\"" + ((HtdigestAuth) authProvider).realm() + "\", qop=\"auth\", nonce=\"" + nonce + "\", opaque=\"" + opaque + "\"");
    context.fail(401);
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
