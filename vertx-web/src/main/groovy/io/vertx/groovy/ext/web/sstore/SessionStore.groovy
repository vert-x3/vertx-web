/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.web.sstore;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.web.Session
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * A session store is used to store sessions for an Vert.x-Web web app
*/
@CompileStatic
public class SessionStore {
  private final def io.vertx.ext.web.sstore.SessionStore delegate;
  public SessionStore(Object delegate) {
    this.delegate = (io.vertx.ext.web.sstore.SessionStore) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * The retry timeout value in milli seconds used by the session handler when it retrieves a value from the store.<p/>
   *
   * A non positive value means there is no retry at all.
   * @return the timeout value, in ms
   */
  public long retryTimeout() {
    def ret = delegate.retryTimeout();
    return ret;
  }
  /**
   * Create a new session
   * @param timeout - the session timeout, in ms
   * @return the session
   */
  public Session createSession(long timeout) {
    def ret = InternalHelper.safeCreate(delegate.createSession(timeout), io.vertx.groovy.ext.web.Session.class);
    return ret;
  }
  /**
   * Get the session with the specified ID
   * @param id the unique ID of the session
   * @param resultHandler will be called with a result holding the session, or a failure
   */
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    delegate.get(id, resultHandler != null ? new Handler<AsyncResult<io.vertx.ext.web.Session>>() {
      public void handle(AsyncResult<io.vertx.ext.web.Session> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(InternalHelper.safeCreate(ar.result(), io.vertx.groovy.ext.web.Session.class)));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  /**
   * Delete the session with the specified ID
   * @param id the unique ID of the session
   * @param resultHandler will be called with a result true/false, or a failure
   */
  public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {
    delegate.delete(id, resultHandler);
  }
  /**
   * Add a session with the specified ID
   * @param session the session
   * @param resultHandler will be called with a result true/false, or a failure
   */
  public void put(Session session, Handler<AsyncResult<Boolean>> resultHandler) {
    delegate.put(session != null ? (io.vertx.ext.web.Session)session.getDelegate() : null, resultHandler);
  }
  /**
   * Remove all sessions from the store
   * @param resultHandler will be called with a result true/false, or a failure
   */
  public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
    delegate.clear(resultHandler);
  }
  /**
   * Get the number of sessions in the store
   * @param resultHandler will be called with the number, or a failure
   */
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    delegate.size(resultHandler);
  }
  /**
   * Close the store
   */
  public void close() {
    delegate.close();
  }
}
