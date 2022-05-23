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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.AbstractSession;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClusteredSessionStoreImpl implements SessionStore, ClusteredSessionStore {

  /**
   * The default name used for the session map
   */
  private static final String DEFAULT_SESSION_MAP_NAME = "vertx-web.sessions";

  /**
   * Default retry time out, in ms, for a session not found in this store.
   */
  private static final long DEFAULT_RETRY_TIMEOUT = 5 * 1000; // 5 seconds


  private Vertx vertx;
  private VertxContextPRNG random;
  private String sessionMapName;
  private long retryTimeout;
  private ContextInternal ctx;

  // Clustered Map
  private volatile AsyncMap<String, Session> sessionMap;

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    this.vertx = vertx;
    this.ctx = (ContextInternal) vertx.getOrCreateContext();
    this.sessionMapName = options.getString("mapName", DEFAULT_SESSION_MAP_NAME);
    this.retryTimeout = options.getLong("retryTimeout", DEFAULT_RETRY_TIMEOUT);
    this.random = VertxContextPRNG.current(vertx);

    return this;
  }

  @Override
  public long retryTimeout() {
    return retryTimeout;
  }

  @Override
  public Session createSession(long timeout) {
    return new SharedDataSessionImpl(random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new SharedDataSessionImpl(random, timeout, length);
  }

  @Override
  public Future<@Nullable Session> get(String id) {
    return getMap()
      .compose(map ->
        map.get(id)
          .onSuccess(session -> {
            if (session != null) {
              ((AbstractSession) session).setPRNG(random);
            }
          }));
  }

  @Override
  public Future<Void> delete(String id) {
    return getMap()
      .compose(map -> map.remove(id))
      .mapEmpty();
  }

  @Override
  public Future<Void> put(Session session) {
    return getMap()
      .compose(map ->
        // we need to take care of the transactionality of session data
        map.get(session.id())
          .compose(old -> {
            final AbstractSession oldSession = (AbstractSession) old;
            final AbstractSession newSession = (AbstractSession) session;

            if (oldSession != null) {
              // there was already some stored data in this case we need to validate versions
              if (oldSession.version() != newSession.version()) {
                return ctx.failedFuture("Session version mismatch");
              }
            }

            // we can now safely store the new version
            newSession.incrementVersion();

            return map.put(session.id(), session, session.timeout());
          })
      );
  }

  @Override
  public Future<Void> clear() {
    return getMap()
      .compose(AsyncMap::clear);
  }

  @Override
  public Future<Integer> size() {
    return getMap()
      .compose(AsyncMap::size);
  }

  @Override
  public void close() {
  }

  private Future<AsyncMap<String, Session>> getMap() {
    if (sessionMap == null) {
      return vertx.sharedData()
        .<String, Session>getClusterWideMap(sessionMapName)
        .onSuccess(sessionMap -> this.sessionMap = sessionMap);
    } else {
      return ctx.succeededFuture(sessionMap);
    }
  }
}
