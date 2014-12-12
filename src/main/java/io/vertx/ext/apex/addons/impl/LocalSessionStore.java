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

import io.vertx.core.Vertx;
import io.vertx.ext.apex.core.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface LocalSessionStore extends SessionStore {

  static final long DEFAULT_REAPER_PERIOD = 1000;
  static final String DEFAULT_SESSION_MAP_NAME = "apex.sessions";

  static SessionStore localSessionStore(Vertx vertx) {
    return new LocalSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, DEFAULT_REAPER_PERIOD);
  }

  static SessionStore localSessionStore(Vertx vertx, String sessionMapName) {
    return new LocalSessionStoreImpl(vertx, sessionMapName, DEFAULT_REAPER_PERIOD);
  }

  static SessionStore localSessionStore(Vertx vertx, String sessionMapName, long reaperPeriod) {
    return new LocalSessionStoreImpl(vertx, sessionMapName, reaperPeriod);
  }
}
