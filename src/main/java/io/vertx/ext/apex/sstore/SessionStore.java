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
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.apex.Session;

/**
 * A session store is used to store sessions for an Apex web app
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface SessionStore {

  /**
   * Create a new session
   *
   * @param timeout - the session timeout, in ms
   *
   * @return the session
   */
  Session createSession(long timeout);

  /**
   * Get the session with the specified ID
   *
   * @param id  the unique ID of the session
   * @param resultHandler  will be called with a result holding the session, or a failure
   */
  void get(String id, Handler<AsyncResult<Session>> resultHandler);

  /**
   * Delete the session with the specified ID
   *
   * @param id  the unique ID of the session
   * @param resultHandler  will be called with a result true/false, or a failure
   */
  void delete(String id, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Add a session with the specified ID
   *
   * @param session  the session
   * @param resultHandler  will be called with a result true/false, or a failure
   */
  void put(Session session, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Remove all sessions from the store
   *
   * @param resultHandler  will be called with a result true/false, or a failure
   */
  void clear(Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Get the number of sessions in the store
   *
   * @param resultHandler  will be called with the number, or a failure
   */
  void size(Handler<AsyncResult<Integer>> resultHandler);

  /**
   * Close the store
   */
  void close();

}
