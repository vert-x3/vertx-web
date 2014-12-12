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

package io.vertx.ext.apex.addons.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.apex.core.Session;
import io.vertx.ext.apex.core.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClusteredSessionStoreImpl implements SessionStore {

  private final Vertx vertx;
  private final String sessionMapName;
  // Clustered Map
  private volatile AsyncMap<String, Session> sessionMap;
  // We also cache locally for better performance - in many cases requests will be pinned to a node anyway
  private final LocalMap<String, Session> localMap;

  public ClusteredSessionStoreImpl(Vertx vertx, String sessionMapName, boolean cacheLocally) {
    this.vertx = vertx;
    this.sessionMapName = sessionMapName;
    localMap = cacheLocally ? vertx.sharedData().getLocalMap(sessionMapName) : null;
  }

  @Override
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    Session session = localMap != null ? localMap.get(id) : null;
    if (session != null) {
      resultHandler.handle(Future.succeededFuture(hasExpired(session)));
    } else {
      getMap(res -> {
        if (res.succeeded()) {
          res.result().get(id, res2 -> {
            if (res2.succeeded()) {
              resultHandler.handle(Future.succeededFuture(hasExpired(res2.result())));
            } else {
              resultHandler.handle(Future.failedFuture(res2.cause()));
            }
          });
        } else {
          resultHandler.handle(Future.failedFuture(res.cause()));
        }
      });
    }
  }

  private Session hasExpired(Session session) {
    if (System.currentTimeMillis() - session.lastAccessed() >= 1000 * session.timeout()) {
      return null;
    } else {
      return session;
    }
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().remove(id, res2 -> {
          if (res2.succeeded()) {
            if (localMap != null) {
              localMap.remove(id);
            }
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
  public void put(String id, Session session, long timeout, Handler<AsyncResult<Boolean>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().put(id, session, timeout * 1000, res2 -> {
          if (res2.succeeded()) {
            if (localMap != null) {
              localMap.put(id, session);
            }
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
  public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
    getMap(res -> {
      if (res.succeeded()) {
        res.result().clear(res2 -> {
          if (res2.succeeded()) {
            if (localMap != null) {
              localMap.clear();
            }
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
    resultHandler.handle(Future.succeededFuture(localMap == null ? -1 : localMap.size()));
  }

  @Override
  public void close() {
    if (localMap != null) {
      localMap.close();
    }
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
