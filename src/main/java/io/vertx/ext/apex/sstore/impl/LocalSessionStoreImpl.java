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

package io.vertx.ext.apex.sstore.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.sstore.LocalSessionStore;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LocalSessionStoreImpl implements LocalSessionStore, Handler<Long> {

  protected final Vertx vertx;
  protected final LocalMap<String, Session> localMap;
  private final long reaperPeriod;
  private long timerID = -1;
  private boolean closed;

  public LocalSessionStoreImpl(Vertx vertx, String sessionMapName, long reaperPeriod) {
    this.vertx = vertx;
    this.reaperPeriod = reaperPeriod;
    localMap = vertx.sharedData().getLocalMap(sessionMapName);
    setTimer();
  }

  @Override
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(localMap.get(id)));
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(localMap.remove(id) != null));
  }

  @Override
  public void put(String id, Session session, long timeout, Handler<AsyncResult<Boolean>> resultHandler) {
    localMap.put(id, session);
    resultHandler.handle(Future.succeededFuture(true));
  }

  @Override
  public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
    localMap.clear();
    resultHandler.handle(Future.succeededFuture(true));
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
    if (reaperPeriod != 0) {
      timerID = vertx.setTimer(reaperPeriod, this);
    }
  }

}
