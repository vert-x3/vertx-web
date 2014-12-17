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

package io.vertx.groovy.ext.apex.core;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class SessionStore {
  final def io.vertx.ext.apex.core.SessionStore delegate;
  public SessionStore(io.vertx.ext.apex.core.SessionStore delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void get(String id, Handler<AsyncResult<Session>> resultHandler) {
    this.delegate.get(id, new Handler<AsyncResult<io.vertx.ext.apex.core.Session>>() {
      public void handle(AsyncResult<io.vertx.ext.apex.core.Session> event) {
        AsyncResult<Session> f
        if (event.succeeded()) {
          f = InternalHelper.<Session>result(new Session(event.result()))
        } else {
          f = InternalHelper.<Session>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void delete(String id, Handler<AsyncResult<Boolean>> resultHandler) {
    this.delegate.delete(id, resultHandler);
  }
  public void put(String id, Session session, long timeout, Handler<AsyncResult<Boolean>> resultHandler) {
    this.delegate.put(id, (io.vertx.ext.apex.core.Session)session.getDelegate(), timeout, resultHandler);
  }
  public void clear(Handler<AsyncResult<Boolean>> resultHandler) {
    this.delegate.clear(resultHandler);
  }
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    this.delegate.size(resultHandler);
  }
  public void close() {
    this.delegate.close();
  }

  static final java.util.function.Function<io.vertx.ext.apex.core.SessionStore, SessionStore> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.SessionStore arg -> new SessionStore(arg);
  };
}
