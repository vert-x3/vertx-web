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
package io.vertx.ext.web.sstore.cookie;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CookieSessionStore implements SessionStore {

  private Mac mac;
  private PRNG random;

  @Override
  public String id() {
    return "cookie";
  }

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    // initialize a secure random
    this.random = new PRNG(vertx);

    try {
      mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(options.getString("secret").getBytes(), "HmacSHA256"));
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
  public void get(String cookieValue, Handler<AsyncResult<@Nullable Session>> resultHandler) {
    try {
      Session session = new CookieSession(mac, random).setValue(cookieValue);
      // need to validate for expired
      long now = System.currentTimeMillis();
      // if expired, the operation succeeded, but returns null
      if (now - session.lastAccessed() > session.timeout()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        // return the already recreated session
        resultHandler.handle(Future.succeededFuture(session));
      }
    } catch (RuntimeException e) {
      resultHandler.handle(Future.failedFuture(e));
    }
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Void>> resultHandler) {
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {
    final CookieSession cookieSession = (CookieSession) session;

    if (cookieSession.oldVersion() != -1) {
      // there was already some stored data in this case we need to validate versions
      if (cookieSession.oldVersion() != cookieSession.version()) {
        resultHandler.handle(Future.failedFuture("Version mismatch"));
        return;
      }
    }

    cookieSession.incrementVersion();
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(0));
  }

  @Override
  public void close() {
    random.close();
  }
}
