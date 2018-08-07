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

package io.vertx.ext.web.sstore.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.AbstractSession;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LocalSessionStoreImpl implements SessionStore, LocalSessionStore, Handler<Long> {

  /**
   * Default of how often, in ms, to check for expired sessions
   */
  private static final long DEFAULT_REAPER_INTERVAL = 1000;

  /**
   * Default name for map used to store sessions
   */
  private static final String DEFAULT_SESSION_MAP_NAME = "vertx-web.sessions";


  private LocalMap<String, Session> localMap;
  private long reaperInterval;
  private PRNG random;

  private long timerID = -1;
  private boolean closed;

  protected Vertx vertx;

  @Override
  public Session createSession(long timeout) {
    return new SharedDataSessionImpl(random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new SharedDataSessionImpl(random, timeout, length);
  }

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    // initialize a secure random
    this.random = new PRNG(vertx);
    this.vertx = vertx;
    this.reaperInterval = options.getLong("reaperInterval", DEFAULT_REAPER_INTERVAL);
    localMap = vertx.sharedData().getLocalMap(options.getString("mapName", DEFAULT_SESSION_MAP_NAME));
    setTimer();

    return this;
  }

  @Override
  public long retryTimeout() {
    return 0;
  }

  @Override
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(localMap.get(id)));
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Void>> resultHandler) {
    localMap.remove(id);
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {
    final AbstractSession oldSession = (AbstractSession) localMap.get(session.id());
    final AbstractSession newSession = (AbstractSession) session;

    if (oldSession != null) {
      // there was already some stored data in this case we need to validate versions
      if (oldSession.version() != newSession.version()) {
        resultHandler.handle(Future.failedFuture("Version mismatch"));
        return;
      }
    }

    newSession.incrementVersion();
    localMap.put(session.id(), session);
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    localMap.clear();
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(localMap.size()));
  }

  @Override
  public synchronized void close() {
    localMap.close();
    if (timerID != -1) {
      vertx.cancelTimer(timerID);
    }
    // stop seeding the PRNG
    random.close();
    closed = true;
  }

  @Override
  public synchronized void handle(Long tid) {
    long now = System.currentTimeMillis();
    Set<String> toRemove = new HashSet<>();
    for (Session session: localMap.values()) {
      if (now - session.lastAccessed() > session.timeout()) {
        toRemove.add(session.id());
      }
    }
    for (String id: toRemove) {
      localMap.remove(id);
    }
    if (!closed) {
      setTimer();
    }
  }

  private void setTimer() {
    if (reaperInterval != 0) {
      timerID = vertx.setTimer(reaperInterval, this);
    }
  }

}
