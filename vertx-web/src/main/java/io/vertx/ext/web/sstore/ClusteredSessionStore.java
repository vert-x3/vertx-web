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

package io.vertx.ext.web.sstore;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.sstore.impl.ClusteredSessionStoreImpl;

/**
 * A session store which stores sessions in a distributed map so they are available across the cluster.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface ClusteredSessionStore extends SessionStore {

  /**
   * The default name used for the session map
   */
  String DEFAULT_SESSION_MAP_NAME = "vertx-web.sessions";

  /**
   * Default retry time out, in ms, for a session not found in this store.
   */
  long DEFAULT_RETRY_TIMEOUT = 5 * 1000; // 5 seconds

  /**
   * Create a session store
   *
   * @param vertx  the Vert.x instance
   * @param sessionMapName  the session map name
   * @return the session store
   */
  static ClusteredSessionStore create(Vertx vertx, String sessionMapName) {
    return new ClusteredSessionStoreImpl(vertx, sessionMapName, DEFAULT_RETRY_TIMEOUT);
  }

  /**
   * Create a session store.<p/>
   *
   * The retry timeout value, configures how long the session handler will retry to get a session from the store
   * when it is not found.
   *
   * @param vertx  the Vert.x instance
   * @param sessionMapName  the session map name
   * @param retryTimeout the store retry timeout, in ms
   * @return the session store
   */
  static ClusteredSessionStore create(Vertx vertx, String sessionMapName, long retryTimeout) {
    return new ClusteredSessionStoreImpl(vertx, sessionMapName, retryTimeout);
  }

  /**
   * Create a session store
   *
   * @param vertx  the Vert.x instance
   * @return the session store
   */
  static ClusteredSessionStore create(Vertx vertx) {
    return new ClusteredSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, DEFAULT_RETRY_TIMEOUT);
  }

  /**
   * Create a session store.<p/>
   *
   * The retry timeout value, configures how long the session handler will retry to get a session from the store
   * when it is not found.
   *
   * @param vertx  the Vert.x instance
   * @param retryTimeout the store retry timeout, in ms
   * @return the session store
   */
  static ClusteredSessionStore create(Vertx vertx, long retryTimeout) {
    return new ClusteredSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, retryTimeout);
  }
}
