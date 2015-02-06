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

package io.vertx.ext.apex.sstore;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.apex.sstore.impl.ClusteredSessionStoreImpl;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface ClusteredSessionStore extends SessionStore {

  static final String DEFAULT_SESSION_MAP_NAME = "apex.sessions";

  static ClusteredSessionStore create(Vertx vertx, String sessionMapName) {
    return new ClusteredSessionStoreImpl(vertx, sessionMapName);
  }

  static ClusteredSessionStore create(Vertx vertx) {
    return new ClusteredSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME);
  }

}
