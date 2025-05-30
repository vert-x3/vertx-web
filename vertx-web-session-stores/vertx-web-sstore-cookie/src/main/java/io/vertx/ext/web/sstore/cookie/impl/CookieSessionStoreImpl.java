/*
 * Copyright 2018 Red Hat, Inc.
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
package io.vertx.ext.web.sstore.cookie.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.prng.VertxContextPRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CookieSessionStoreImpl implements CookieSessionStore {

  private static final String SHA_CRYPT = "SHA-256";
  private static final String AES_ALGORITHM = "AES";
 
  public CookieSessionStoreImpl() {
    // required for the service loader
  }

  public CookieSessionStoreImpl(Vertx vertx, String secret) {
    init(vertx, new JsonObject().put("secret", secret));
  }

  private SecretKeySpec aesKey;
  private VertxContextPRNG random;
  private ContextInternal ctx;

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    // initialize a secure random
    this.random = VertxContextPRNG.current(vertx);
    this.ctx = (ContextInternal) vertx.getOrCreateContext();

    Objects.requireNonNull(options.getValue("secret"), "secret must be set");

    try {
      // AES Key generation
      MessageDigest sha256 = MessageDigest.getInstance(SHA_CRYPT);
      byte[] keyBytes = sha256.digest(options.getString("secret").getBytes(StandardCharsets.UTF_8));
      aesKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    return this;
  }

  @Override
  public long retryTimeout() {
    return -1;
  }

  @Override
  public Session createSession(long timeout) {
    return new CookieSession(aesKey, random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new CookieSession(aesKey, random, timeout, length);
  }

  @Override
  public Future<@Nullable Session> get(String cookieValue) {
    try {
      Session session = new CookieSession(aesKey, random).setValue(cookieValue);

      if (session == null) {
        return ctx.succeededFuture();
      }

      // need to validate for expired
      long now = System.currentTimeMillis();
      // if expired, the operation succeeded, but returns null
      if (now - session.lastAccessed() > session.timeout()) {
        return ctx.succeededFuture();
      } else {
        // return the already recreated session
        return ctx.succeededFuture(session);
      }
    } catch (RuntimeException e) {
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<Void> delete(String id) {
    return ctx.succeededFuture();
  }

  @Override
  public Future<Void> put(Session session) {
    final CookieSession cookieSession = (CookieSession) session;

    if (cookieSession.oldVersion() != -1) {
      // there was already some stored data in this case we need to validate versions
      if (cookieSession.oldVersion() != cookieSession.version()) {
        return Future.failedFuture("Session version mismatch");
      }
    }

    cookieSession.incrementVersion();
    return ctx.succeededFuture();
  }

  @Override
  public Future<Void> clear() {
    return ctx.succeededFuture();
  }

  @Override
  public Future<Integer> size() {
    return ctx.succeededFuture(0);
  }

  @Override
  public void close() {
    // nothing to close
  }
}
