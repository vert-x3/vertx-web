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

import java.util.concurrent.atomic.AtomicBoolean;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.AbstractSession;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;
import io.vertx.ext.web.sstore.redis.RedisSessionStore;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;

/**
 * @author <a href="https://github.com/llfbandit">Rémy Noël</a>
 */
public class RedisSessionStoreImpl implements RedisSessionStore {
  private final RedisClientHelper redis;
  private final PRNG random;
  private final long retryTimeout;

  public RedisSessionStoreImpl(Vertx vertx, RedisOptions options, long retryTimeout) {
    random = new PRNG(vertx);
    this.retryTimeout = retryTimeout;
    redis = new RedisClientHelper(vertx, options);
  }

  @Override
  public SessionStore init(Vertx vertx, JsonObject options) {
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
    getConnection(resultHandler, connection ->
      connection.send(Request.cmd(Command.GET).arg(id), resGet -> {
        if (resGet.failed()) {
          resultHandler.handle(Future.failedFuture(resGet.cause()));
          return;
        }

        Response response = resGet.result();
        if (response != null) {
          SharedDataSessionImpl session = new SharedDataSessionImpl(random);
          session.readFromBuffer(0, response.toBuffer());
          // postpone expiration time, this cannot be done in a single frame with GET cmd
          connection.send(
            Request.cmd(Command.PEXPIRE).arg(id).arg(session.timeout()),
            resExpire -> wrapResult(resExpire, session, resultHandler));
        } else {
          resultHandler.handle(Future.succeededFuture());
        }
      })
    );
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Void>> resultHandler) {
    getConnection(resultHandler, connection ->
      connection.send(
        Request.cmd(Command.DEL).arg(id),
        res -> wrapResult(res, null, resultHandler))
    );
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {
    getConnection(resultHandler, connection ->
      connection.send(Request.cmd(Command.GET).arg(session.id()), res -> {
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
        writeSession(connection, newSession, resultHandler);
      })
    );
  }

  private void writeSession(RedisConnection redisConnection, Session session, Handler<AsyncResult<Void>> resultHandler) {
    Buffer buffer = Buffer.buffer();
    SharedDataSessionImpl sessionImpl = (SharedDataSessionImpl) session;
    sessionImpl.writeToBuffer(buffer);

    // submit with all session data & expiration TO in ms
    Request rq = Request.cmd(Command.SET)
      .arg(session.id()).arg(buffer)
      .arg("PX").arg(session.timeout());

    redisConnection.send(rq, res -> wrapResult(res, null, resultHandler));
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    getConnection(resultHandler, connection ->
      connection.send(Request.cmd(Command.FLUSHDB), res -> wrapResult(res, null, resultHandler))
    );
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    getConnection(resultHandler, connection ->
      connection.send(Request.cmd(Command.DBSIZE), res -> {
        if (res.succeeded()) {
          long lngCount = res.result().toLong();
          int count = (lngCount > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) lngCount;
          resultHandler.handle(Future.succeededFuture(count));
        } else {
          resultHandler.handle(Future.failedFuture(res.cause()));
        }
      })
    );
  }

  @Override
  public void close() {
    redis.close();
    random.close();
  }

  private <E> void getConnection(Handler<AsyncResult<E>> errorHandler, Handler<RedisConnection> processHandler) {
    redis.getConnection().setHandler(connResult -> {
      if (connResult.failed()) {
        errorHandler.handle(Future.failedFuture(connResult.cause()));
      } else {
        processHandler.handle(connResult.result());
      }
    });
  }

  private <R, E> void wrapResult(AsyncResult<R> asResult, E result, Handler<AsyncResult<E>> resultHandler) {
    if (asResult.failed()) {
      resultHandler.handle(Future.failedFuture(asResult.cause()));
    } else {
      resultHandler.handle(Future.succeededFuture(result));
    }
  }

  /**
   * A helper class to Redis to be able to auto reconnect.
   */
  private static class RedisClientHelper {
    private static final Logger LOG = LoggerFactory.getLogger(RedisClientHelper.class.getName());
    // Attempt to reconnect up to MAX_RECONNECT_RETRIES
    private static final int MAX_RECONNECT_RETRIES = 16;
    private static final String CONNECT_LOCK = "vertx.sstore.redis.connect";

    private final Vertx vertx;
    private final Redis client;
    private RedisConnection connection;
    private long retryTimerId = -1;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    private RedisClientHelper(Vertx vertx, RedisOptions options) {
      this.vertx = vertx;
      client = Redis.createClient(vertx, options);
    }

    void close() {
      if (retryTimerId != -1) {
        vertx.cancelTimer(retryTimerId);
      }
      client.close();
      isConnected.set(false);
    }

    /**
     * Will create a redis connection if needed and
     * setup a reconnect handler when there is
     * an exception related to.
     */
    Future<RedisConnection> getConnection() {
      if (isConnected.get()) {
        return Future.succeededFuture(connection);
      }

      Promise<RedisConnection> promise = Promise.promise();

      vertx.sharedData().getLocalLock(CONNECT_LOCK, lockRes -> {
        if (lockRes.failed()) {
          promise.fail(lockRes.cause());
          return;
        }

        if (isConnected.get()) {
          promise.complete(connection);
          lockRes.result().release();
          return;
        }

        client.connect(onConnect -> {
          if (onConnect.succeeded()) {
            connection = onConnect.result();
            isConnected.set(true);

            // make sure the client is reconnected on error
            connection.exceptionHandler(e -> {
              isConnected.set(false);
              tryReconnect(0);
            });

            promise.complete(connection);
          } else {
            promise.fail(onConnect.cause());
          }

          lockRes.result().release();
        });
      });

      return promise.future();
    }

    private void tryReconnect(int retry) {
      if (retry > MAX_RECONNECT_RETRIES) {
        LOG.error(String.format(
          "Redis session server can't be reached after %s retries.",
          MAX_RECONNECT_RETRIES));
      } else {
        // retry with increased delay
        long delay = (long) (Math.pow(2, MAX_RECONNECT_RETRIES - Math.max(MAX_RECONNECT_RETRIES - retry, 9)) * 10);

        retryTimerId = vertx.setTimer(delay, timer -> {
          retryTimerId = -1;

          getConnection().setHandler(onReconnect -> {
            if (onReconnect.failed()) {
              tryReconnect(retry + 1);
            }
          });
        });
      }
    }
  }
}
