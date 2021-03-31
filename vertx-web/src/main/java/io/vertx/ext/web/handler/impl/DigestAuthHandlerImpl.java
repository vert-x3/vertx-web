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
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.htdigest.HtdigestAuth;
import io.vertx.ext.auth.htdigest.HtdigestCredentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.DigestAuthHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class DigestAuthHandlerImpl extends HTTPAuthorizationHandler<HtdigestAuth> implements DigestAuthHandler {

  private final static Logger LOG = LoggerFactory.getLogger(HTTPAuthorizationHandler.class);

  /**
   * Default name for map used to store nonces
   */
  private static final String DEFAULT_NONCE_MAP_NAME = "htdigest.nonces";

  /**
   * Shareable objects should be immutable.
   */
  private static class Nonce implements Shareable {
    private final long createdAt;
    private final int count;

    Nonce(int count) {
      this(System.currentTimeMillis(), count);
    }

    Nonce(long createdAt, int count) {
      this.createdAt = createdAt;
      this.count = count;
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

  private final VertxContextPRNG random;
  private final LocalMap<String, Nonce> nonces;

  private final long nonceExpireTimeout;

  private long lastExpireRun;

  public DigestAuthHandlerImpl(Vertx vertx, HtdigestAuth authProvider, long nonceExpireTimeout) {
    super(authProvider, authProvider.realm(), Type.DIGEST);
    random = VertxContextPRNG.current(vertx);
    nonces = vertx.sharedData().getLocalMap(DEFAULT_NONCE_MAP_NAME);
    this.nonceExpireTimeout = nonceExpireTimeout;
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<Credentials>> handler) {
    // clean up nonce
    long now = System.currentTimeMillis();
    if (now - lastExpireRun > nonceExpireTimeout / 2) {
      Set<String> toRemove = new HashSet<>();
      nonces.forEach((String key, Nonce n) -> {
        if (n != null && n.createdAt + nonceExpireTimeout < now) {
          toRemove.add(key);
        }
      });

      for (String n : toRemove) {
        nonces.remove(n);
      }
      lastExpireRun = now;
    }

    parseAuthorization(context, parseAuthorization -> {
      if (parseAuthorization.failed()) {
        handler.handle(Future.failedFuture(parseAuthorization.cause()));
        return;
      }

      final HtdigestCredentials authInfo = new HtdigestCredentials();

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
            switch (m.group(1)) {
              case "algorithm":
                authInfo.setAlgorithm(m.group(2));
                break;
              case "cnonce":
                authInfo.setCnonce(m.group(2));
                break;
              case "method":
                authInfo.setMethod(m.group(2));
                break;
              case "nc":
                authInfo.setNc(m.group(2));
                break;
              case "nonce":
                authInfo.setNonce(m.group(2));
                break;
              case "opaque":
                authInfo.setOpaque(m.group(2));
                break;
              case "qop":
                authInfo.setQop(m.group(2));
                break;
              case "realm":
                authInfo.setRealm(m.group(2));
                break;
              case "response":
                authInfo.setResponse(m.group(2));
                break;
              case "uri":
                authInfo.setUri(m.group(2));
                break;
              case "username":
                authInfo.setUsername(m.group(2));
                break;
              default:
                LOG.info("Uknown parameter: " + m.group(1));
            }
          }

          ++i;
        }

        final String nonce = authInfo.getNonce();

        // check for expiration
        if (!nonces.containsKey(nonce)) {
          handler.handle(Future.failedFuture(UNAUTHORIZED));
          return;
        }

        // check for nonce counter (prevent replay attack)
        if (authInfo.getQop() != null) {
          int nc = Integer.parseInt(authInfo.getNc(), 16);
          final Nonce n = nonces.get(nonce);
          if (nc <= n.count) {
            handler.handle(Future.failedFuture(UNAUTHORIZED));
            return;
          }
          // update the nounce count
          nonces.put(nonce, new Nonce(n.createdAt, nc));
        }

      } catch (RuntimeException e) {
        handler.handle(Future.failedFuture(e));
      }

      // validate the opaque value
      final Session session = context.session();
      if (session != null) {
        String opaque = (String) session.data().get("opaque");
        if (opaque != null && !opaque.equals(authInfo.getOpaque())) {
          handler.handle(Future.failedFuture(UNAUTHORIZED));
          return;
        }
      }

      // we now need to pass some extra info
      authInfo.setMethod(context.request().method().name());

      handler.handle(Future.succeededFuture(authInfo));
    });
  }

  @Override
  public String authenticateHeader(RoutingContext context) {
    final byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    // generate nonce
    String nonce = md5(bytes);
    // save it
    nonces.put(nonce, new Nonce(0));

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

  @Override
  public DigestAuthHandler postAuthenticationHandler(Handler<RoutingContext> handler) {
    super.postAuthenticationHandler(handler);
    return this;
  }
}
