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
import io.vertx.ext.web.sstore.impl.LocalSessionStoreImpl;

/**
 * A session store which is only available on a single node.
 * <p>
 * Can be used when sticky sessions are being used.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface LocalSessionStore extends SessionStore {

  /**
   * Default of how often, in ms, to check for expired sessions
   */
  long DEFAULT_REAPER_PERIOD = 1000;

  /**
   * Default name for map used to store sessions
   */
  String DEFAULT_SESSION_MAP_NAME = "vertx-web.sessions";

  /**
   * Create a session store
   *
   * @param vertx  the Vert.x instance
   * @return the session store
   */
  static LocalSessionStore create(Vertx vertx) {
    return new LocalSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, DEFAULT_REAPER_PERIOD);
  }

  /**
   * Create a session store
   *
   * @param vertx  the Vert.x instance
   * @param sessionMapName  name for map used to store sessions
   * @return the session store
   */
  static LocalSessionStore create(Vertx vertx, String sessionMapName) {
    return new LocalSessionStoreImpl(vertx, sessionMapName, DEFAULT_REAPER_PERIOD);
  }

  /**
   * Create a session store
   *
   * @param vertx  the Vert.x instance
   * @param sessionMapName  name for map used to store sessions
   * @param reaperPeriod  how often, in ms, to check for expired sessions
   * @return the session store
   */
  static LocalSessionStore create(Vertx vertx, String sessionMapName, long reaperPeriod) {
    return new LocalSessionStoreImpl(vertx, sessionMapName, reaperPeriod);
  }
}
