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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
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
  public Future<@Nullable Session> get(String id) {
    return redis.send(cmd(GET).arg(id))
      .compose(response -> {
        if (response != null) {
          SharedDataSessionImpl session = new SharedDataSessionImpl(random);
          session.readFromBuffer(0, response.toBuffer());
          // postpone expiration time, this cannot be done in a single frame with GET cmd
          return redis
            .send(cmd(PEXPIRE).arg(id).arg(session.timeout()))
            .map(session);
        } else {
          return Future.succeededFuture();
        }
      });
  }

  @Override
  public Future<Void> delete(String id) {
    return redis.send(cmd(DEL).arg(id))
      .mapEmpty();
  }

  @Override
  public Future<Void> put(Session session) {
    return redis.send(cmd(GET).arg(session.id()))
      .compose(response -> {
      AbstractSession newSession = (AbstractSession) session;
      if (response != null) {
        // Old session exists, we need to validate versions
        SharedDataSessionImpl oldSession = new SharedDataSessionImpl(random);
        oldSession.readFromBuffer(0, response.toBuffer());

        if (oldSession.version() != newSession.version()) {
          return Future.failedFuture("Session version mismatch");
        }
      }

      newSession.incrementVersion();
      return writeSession(newSession);
    });
  }

  private Future<Void> writeSession(Session session) {
    Buffer buffer = Buffer.buffer();
    SharedDataSessionImpl sessionImpl = (SharedDataSessionImpl) session;
    sessionImpl.writeToBuffer(buffer);

    // submit with all session data & expiration TO in ms
    Request rq = cmd(SET)
      .arg(session.id()).arg(buffer)
      .arg("PX").arg(session.timeout());

    return redis.send(rq)
      .mapEmpty();
  }

  @Override
  public Future<Void> clear() {
    return redis.send(cmd(FLUSHDB))
      .mapEmpty();
  }

  @Override
  public Future<Integer> size() {
    return redis.send(cmd(DBSIZE))
      .compose(response -> {
        if (response == null) {
          return Future.succeededFuture(-1);
        } else {
          Long lngCount = response.toLong();
          int count = (lngCount > Integer.MAX_VALUE) ? Integer.MAX_VALUE : lngCount.intValue();
          return Future.succeededFuture(count);
        }
    });
  }

  @Override
  public void close() {
    redis.close();
  }
}
