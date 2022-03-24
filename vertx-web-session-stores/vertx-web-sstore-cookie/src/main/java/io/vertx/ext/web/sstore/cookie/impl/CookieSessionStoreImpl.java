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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CookieSessionStoreImpl implements CookieSessionStore {

  public CookieSessionStoreImpl() {
    // required for the service loader
  }

  public CookieSessionStoreImpl(Vertx vertx, String secret) {
    init(vertx, new JsonObject().put("secret", secret));
  }

  private Mac mac;
  private VertxContextPRNG random;

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    // initialize a secure random
    this.random = VertxContextPRNG.current(vertx);

    try {
      mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(options.getString("secret").getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
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
    return new CookieSession(mac, random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new CookieSession(mac, random, timeout, length);
  }

  @Override
  public Future<@Nullable Session> get(String cookieValue) {
    try {
      Session session = new CookieSession(mac, random).setValue(cookieValue);

      if (session == null) {
        return Future.succeededFuture();
      }

      // need to validate for expired
      long now = System.currentTimeMillis();
      // if expired, the operation succeeded, but returns null
      if (now - session.lastAccessed() > session.timeout()) {
        return Future.succeededFuture();
      } else {
        // return the already recreated session
        return Future.succeededFuture(session);
      }
    } catch (RuntimeException e) {
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<Void> delete(String id) {
    return Future.succeededFuture();
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
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> clear() {
    return Future.succeededFuture();
  }

  @Override
  public Future<Integer> size() {
    return Future.succeededFuture(0);
  }

  @Override
  public void close() {
    // nothing to close
  }
}
