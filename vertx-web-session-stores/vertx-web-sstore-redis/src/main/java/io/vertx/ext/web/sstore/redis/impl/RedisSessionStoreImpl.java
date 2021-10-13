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
package io.vertx.ext.web.sstore.redis.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.AbstractSession;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;
import io.vertx.ext.web.sstore.redis.RedisSessionStore;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;

import java.util.Objects;

import static io.vertx.redis.client.Command.*;
import static io.vertx.redis.client.Request.cmd;

/**
 * @author <a href="https://github.com/llfbandit">Rémy Noël</a>
 */
public class RedisSessionStoreImpl implements RedisSessionStore {
  private Redis redis;
  private VertxContextPRNG random;
  private long retryTimeout;

  public RedisSessionStoreImpl() {
    // required for the service loader
  }

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
    Objects.requireNonNull(options, "options are required");
    long timeout = options.getLong("retryTimeout", RedisSessionStore.DEFAULT_RETRY_TIMEOUT_MS);
    Redis redis = Redis.createClient(vertx, new RedisOptions(options));
    return init(vertx, timeout, redis);
  }

  public SessionStore init(Vertx vertx, long retryTimeout, Redis redis) {
    random = VertxContextPRNG.current(vertx);
    this.retryTimeout = retryTimeout;
    this.redis = Objects.requireNonNull(redis, "redis is required");
    return this;
  }

  @Override
  public long retryTimeout() {
    return retryTimeout;
  }

  @Override
  public Session createSession(long timeout) {
    return createSession(timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new SharedDataSessionImpl(random, timeout, length);
  }

  @Override
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    redis.send(cmd(GET).arg(id), resGet -> {
        if (resGet.failed()) {
          resultHandler.handle(Future.failedFuture(resGet.cause()));
          return;
        }

        Response response = resGet.result();
        if (response != null) {
          SharedDataSessionImpl session = new SharedDataSessionImpl(random);
          session.readFromBuffer(0, response.toBuffer());
          // postpone expiration time, this cannot be done in a single frame with GET cmd
          redis.send(cmd(PEXPIRE).arg(id).arg(session.timeout()), resExpire -> {
            if (resExpire.failed()) {
              resultHandler.handle(Future.failedFuture(resExpire.cause()));
            } else {
              resultHandler.handle(Future.succeededFuture(session));
            }
          });
        } else {
          resultHandler.handle(Future.succeededFuture());
        }
      });
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Void>> resultHandler) {
    redis.send(cmd(DEL).arg(id), res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture());
      }
    });
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {
    redis.send(cmd(GET).arg(session.id()), res -> {
        if (res.failed()) {
          resultHandler.handle(Future.failedFuture(res.cause()));
          return;
        }

        AbstractSession newSession = (AbstractSession) session;
        Response response = res.result();
        if (response != null) {
          // Old session exists, we need to validate versions
          SharedDataSessionImpl oldSession = new SharedDataSessionImpl(random);
          oldSession.readFromBuffer(0, response.toBuffer());

          if (oldSession.version() != newSession.version()) {
            resultHandler.handle(Future.failedFuture("Session version mismatch"));
            return;
          }
        }

        newSession.incrementVersion();
        writeSession(newSession, resultHandler);
      });
  }

  private void writeSession(Session session, Handler<AsyncResult<Void>> resultHandler) {
    Buffer buffer = Buffer.buffer();
    SharedDataSessionImpl sessionImpl = (SharedDataSessionImpl) session;
    sessionImpl.writeToBuffer(buffer);

    // submit with all session data & expiration TO in ms
    Request rq = cmd(SET)
      .arg(session.id()).arg(buffer)
      .arg("PX").arg(session.timeout());

    redis.send(rq, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture());
      }
    });
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    redis.send(cmd(FLUSHDB), res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture());
      }
    });
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    redis.send(cmd(DBSIZE), res -> {
        if (res.succeeded()) {
          Response response = res.result();
          if (response == null) {
            resultHandler.handle(Future.succeededFuture(-1));
          } else {
            Long lngCount = response.toLong();
            int count = (lngCount > Integer.MAX_VALUE) ? Integer.MAX_VALUE : lngCount.intValue();
            resultHandler.handle(Future.succeededFuture(count));
          }
        } else {
          resultHandler.handle(Future.failedFuture(res.cause()));
        }
      });
  }

  @Override
  public void close() {
    redis.close();
  }
}
