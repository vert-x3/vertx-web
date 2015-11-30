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

package io.vertx.rxjava.ext.web.sstore;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.web.Session;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A session store is used to store sessions for an Vert.x-Web web app
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.sstore.SessionStore original} non RX-ified interface using Vert.x codegen.
 */

public class SessionStore {

  final io.vertx.ext.web.sstore.SessionStore delegate;

  public SessionStore(io.vertx.ext.web.sstore.SessionStore delegate) {
    this.delegate = delegate;
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
    long ret = this.delegate.retryTimeout();
    return ret;
  }

  /**
   * Create a new session
   * @param timeout - the session timeout, in ms
   * @return the session
   */
  public Session createSession(long timeout) { 
    Session ret= Session.newInstance(this.delegate.createSession(timeout));
    return ret;
  }

  /**
   * Get the session with the specified ID
   * @param id the unique ID of the session
   * @param resultHandler will be called with a result holding the session, or a failure
   */
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) { 
    this.delegate.get(id, new Handler<AsyncResult<io.vertx.ext.web.Session>>() {
      public void handle(AsyncResult<io.vertx.ext.web.Session> event) {
        AsyncResult<Session> f;
        if (event.succeeded()) {
          f = InternalHelper.<Session>result(new Session(event.result()));
        } else {
          f = InternalHelper.<Session>failure(event.cause());
        }
        resultHandler.handle(f);
      }
    });
  }

  /**
   * Get the session with the specified ID
   * @param id the unique ID of the session
   * @return 
   */
  public Observable<Session> getObservable(String id) { 
    io.vertx.rx.java.ObservableFuture<Session> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    get(id, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Delete the session with the specified ID
   * @param id the unique ID of the session
   * @param resultHandler will be called with a result true/false, or a failure
   */
  public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) { 
    this.delegate.delete(id, resultHandler);
  }

  /**
   * Delete the session with the specified ID
   * @param id the unique ID of the session
   * @return 
   */
  public Observable<Boolean> deleteObservable(String id) { 
    io.vertx.rx.java.ObservableFuture<Boolean> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    delete(id, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Add a session with the specified ID
   * @param session the session
   * @param resultHandler will be called with a result true/false, or a failure
   */
  public void put(Session session, Handler<AsyncResult<Boolean>> resultHandler) { 
    this.delegate.put((io.vertx.ext.web.Session) session.getDelegate(), resultHandler);
  }

  /**
   * Add a session with the specified ID
   * @param session the session
   * @return 
   */
  public Observable<Boolean> putObservable(Session session) { 
    io.vertx.rx.java.ObservableFuture<Boolean> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    put(session, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Remove all sessions from the store
   * @param resultHandler will be called with a result true/false, or a failure
   */
  public void clear(Handler<AsyncResult<Boolean>> resultHandler) { 
    this.delegate.clear(resultHandler);
  }

  /**
   * Remove all sessions from the store
   * @return 
   */
  public Observable<Boolean> clearObservable() { 
    io.vertx.rx.java.ObservableFuture<Boolean> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    clear(resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Get the number of sessions in the store
   * @param resultHandler will be called with the number, or a failure
   */
  public void size(Handler<AsyncResult<Integer>> resultHandler) { 
    this.delegate.size(resultHandler);
  }

  /**
   * Get the number of sessions in the store
   * @return 
   */
  public Observable<Integer> sizeObservable() { 
    io.vertx.rx.java.ObservableFuture<Integer> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    size(resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Close the store
   */
  public void close() { 
    this.delegate.close();
  }


  public static SessionStore newInstance(io.vertx.ext.web.sstore.SessionStore arg) {
    return arg != null ? new SessionStore(arg) : null;
  }
}
