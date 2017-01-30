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
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.ClusteredSessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClusteredSessionStoreImpl implements ClusteredSessionStore {

  private final Vertx vertx;
  private final PRNG random;
  private final String sessionMapName;
  private final long retryTimeout;

  // Clustered Map
  private volatile AsyncMap<String, Session> sessionMap;

  public ClusteredSessionStoreImpl(Vertx vertx, String sessionMapName, long retryTimeout) {
    this.vertx = vertx;
    this.sessionMapName = sessionMapName;
    this.retryTimeout = retryTimeout;
    this.random = new PRNG(vertx);
  }

  @Override
  public long retryTimeout() {
    return retryTimeout;
  }

  @Override
  public Session createSession(long timeout) {
    return new SessionImpl(random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new SessionImpl(random, timeout, length);
  }

  @Override
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().get(id, res2 -> {
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
  public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().remove(id, res2 -> {
          if (res2.succeeded()) {
            resultHandler.handle(Future.succeededFuture(res2.result() != null));
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
  public void put(Session session, Handler<AsyncResult<Boolean>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        // we need to take care of the transactionality of session data
        res.result().get(session.id(), old -> {
          final SessionImpl oldSession;
          final SessionImpl newSession = (SessionImpl) session;
          // only care if succeeded
          if (old.succeeded()) {
            oldSession = (SessionImpl) old.result();
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
              resultHandler.handle(Future.succeededFuture(res2.result() != null));
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
  public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().clear(res2 -> {
          if (res2.succeeded()) {
            resultHandler.handle(Future.succeededFuture(res2.result() != null));
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
