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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.impl.ClusteredSessionStoreImpl;
import io.vertx.ext.web.sstore.impl.LocalSessionStoreImpl;

/**
 * A session store is used to store sessions for an Vert.x-Web web app
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface SessionStore {

  /**
   * Create a Session store given a backend and configuration JSON.
   *
   * @param vertx vertx instance
   * @return the store or runtime exception
   */
  static SessionStore create(Vertx vertx) {
    return create(vertx, new JsonObject());
  }

  /**
   * Create a Session store given a backend and configuration JSON.
   *
   * @param vertx vertx instance
   * @param options extra options for initialization
   * @return the store or runtime exception
   */
  static SessionStore create(Vertx vertx, JsonObject options) {
    SessionStore defaultStore;

    try {
      defaultStore = ServiceHelper.loadFactoryOrNull(SessionStore.class);
      if (defaultStore != null) {
        return defaultStore.init(vertx, options);
      }
    } catch (RuntimeException e) {
      // ignore that it cannot be loaded, falling back to the next
    }

    if (vertx.isClustered()) {
      defaultStore = new ClusteredSessionStoreImpl();
    } else {
      defaultStore = new LocalSessionStoreImpl();
    }

    return defaultStore.init(vertx, options);
  }

  /**
   * Initialize this store.
   *
   * @param vertx  the vertx instance
   * @param options  optional Json with extra configuration options
   * @return  self
   */
  @Fluent
  SessionStore init(Vertx vertx, JsonObject options);

  /**
   * Default length for a session id.
   * More info: https://www.owasp.org/index.php/Session_Management_Cheat_Sheet
   */
  int DEFAULT_SESSIONID_LENGTH = 16;

  /**
   * The retry timeout value in milli seconds used by the session handler when it retrieves a value from the store.<p/>
   *
   * A non positive value means there is no retry at all.
   *
   * @return the timeout value, in ms
   */
  long retryTimeout();

  /**
   * Create a new session using the default min length.
   *
   * @param timeout - the session timeout, in ms
   *
   * @return the session
   */
  Session createSession(long timeout);

  /**
   * Create a new session.
   *
   * @param timeout - the session timeout, in ms
   * @param length - the required length for the session id
   *
   * @return the session
   */
  Session createSession(long timeout, int length);

  /**
   * Get the session with the specified ID.
   *
   * @param cookieValue  the unique ID of the session
   * @param resultHandler  will be called with a result holding the session, or a failure
   */
  void get(String cookieValue, Handler<AsyncResult<@Nullable Session>> resultHandler);

  /**
   * Delete the session with the specified ID.
   *
   * @param id  the session id
   * @param resultHandler  will be called with a success or a failure
   */
  void delete(String id, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Add a session with the specified ID.
   *
   * @param session  the session
   * @param resultHandler  will be called with a success or a failure
   */
  void put(Session session, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Remove all sessions from the store.
   *
   * @param resultHandler  will be called with a success or a failure
   */
  void clear(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Get the number of sessions in the store.
   * <p>
   * Beware of the result which is just an estimate, in particular with distributed session stores.
   *
   * @param resultHandler  will be called with the number, or a failure
   */
  void size(Handler<AsyncResult<Integer>> resultHandler);

  /**
   * Close the store
   */
  void close();
}
