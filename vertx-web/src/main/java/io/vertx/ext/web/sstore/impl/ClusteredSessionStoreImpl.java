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
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.auth.PRNG;
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
  private PRNG random;
  private String sessionMapName;
  private long retryTimeout;

  // Clustered Map
  private volatile AsyncMap<String, Session> sessionMap;

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    this.vertx = vertx;
    this.sessionMapName = options.getString("mapName", DEFAULT_SESSION_MAP_NAME);
    this.retryTimeout = options.getLong("retryTimeout", DEFAULT_RETRY_TIMEOUT);
    this.random = new PRNG(vertx);

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
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().get(id, res2 -> {
          if (res2.succeeded()) {
            AbstractSession session = (AbstractSession) res2.result();
            if (session != null) {
              session.setPRNG(random);
            }
            resultHandler.handle(Future.succeededFuture(res2.result()));
          } else {
            resultHandler.handle(Future.failedFuture(res2.cause()));
          }
        });
      } else {
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Void>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().remove(id, res2 -> {
          if (res2.succeeded()) {
            resultHandler.handle(Future.succeededFuture());
          } else {
            resultHandler.handle(Future.failedFuture(res2.cause()));
          }
        });
      } else {
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        // we need to take care of the transactionality of session data
        res.result().get(session.id(), old -> {
          final AbstractSession oldSession;
          final AbstractSession newSession = (AbstractSession) session;
          // only care if succeeded
          if (old.succeeded()) {
            oldSession = (AbstractSession) old.result();
          } else {
            // either not existent or error getting it from the map
            oldSession = null;
          }

          if (oldSession != null) {
            // there was already some stored data in this case we need to validate versions
            if (oldSession.version() != newSession.version()) {
              resultHandler.handle(Future.failedFuture("Version mismatch"));
              return;
            }
          }

          // we can now safely store the new version
          newSession.incrementVersion();

          res.result().put(session.id(), session, session.timeout(), res2 -> {
            if (res2.succeeded()) {
              resultHandler.handle(Future.succeededFuture());
            } else {
              resultHandler.handle(Future.failedFuture(res2.cause()));
            }
          });
        });
      } else {
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().clear(res2 -> {
          if (res2.succeeded()) {
            resultHandler.handle(Future.succeededFuture());
          } else {
            resultHandler.handle(Future.failedFuture(res2.cause()));
          }
        });
      } else {
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().size(res2 -> {
          if (res2.succeeded()) {
            resultHandler.handle(Future.succeededFuture(res2.result()));
          } else {
            resultHandler.handle(Future.failedFuture(res2.cause()));
          }
        });
      } else {
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public void close() {
    // stop seeding the PRNG
    random.close();
  }

  private void getMap(Handler<AsyncResult<AsyncMap<String, Session>>> resultHandler) {
    if (sessionMap == null) {
      vertx.sharedData().<String, Session>getClusterWideMap(sessionMapName, res -> {
        if (res.succeeded()) {
          sessionMap = res.result();
          resultHandler.handle(Future.succeededFuture(res.result()));
        } else {
          resultHandler.handle(res);
        }
      });
    } else {
      resultHandler.handle(Future.succeededFuture(sessionMap));
    }
  }
}
