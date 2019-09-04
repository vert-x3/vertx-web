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
package io.vertx.ext.web.sstore.redis;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.redis.impl.RedisSessionStoreImpl;
import io.vertx.redis.client.RedisOptions;

/**
 * A SessionStore that uses a Redis to store the sessions and associated data.
 * This assumes that the redis logical database (i.e. select) is only used
 * to store the sessions to make size query reliable.
 *
 * @author <a href="https://github.com/llfbandit">Rémy Noël</a>
 */
@VertxGen
public interface RedisSessionStore extends SessionStore {
  long DEFAULT_RETRY_TIMEOUT_MS = 2 * 1000;

  /**
   * Creates a RedisSessionStore with the default retry TO.
   *
   * @param vertx   a Vert.x instance
   * @param options The given options to establish the connection
   * @return the store
   */
  static RedisSessionStore create(Vertx vertx, RedisOptions options) {
    return new RedisSessionStoreImpl(vertx, options, DEFAULT_RETRY_TIMEOUT_MS);
  }

  /**
   * Creates a RedisSessionStore with the given retry TO.
   *
   * @param vertx          a Vert.x instance
   * @param options        The given options to establish the connection
   * @param retryTimeoutMs The time between two consecutive tries
   * @return the store
   */
  static RedisSessionStore create(Vertx vertx, RedisOptions options, long retryTimeoutMs) {
    return new RedisSessionStoreImpl(vertx, options, retryTimeoutMs);
  }
}
