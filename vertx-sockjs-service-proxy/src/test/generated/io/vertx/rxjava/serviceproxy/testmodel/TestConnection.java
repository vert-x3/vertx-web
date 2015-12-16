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

package io.vertx.rxjava.serviceproxy.testmodel;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.serviceproxy.testmodel.TestConnection original} non RX-ified interface using Vert.x codegen.
 */

public class TestConnection {

  final io.vertx.serviceproxy.testmodel.TestConnection delegate;

  public TestConnection(io.vertx.serviceproxy.testmodel.TestConnection delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public TestConnection startTransaction(Handler<AsyncResult<String>> resultHandler) { 
    this.delegate.startTransaction(resultHandler);
    return this;
  }

  public Observable<String> startTransactionObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    startTransaction(resultHandler.toHandler());
    return resultHandler;
  }

  public TestConnection insert(String name, JsonObject data, Handler<AsyncResult<String>> resultHandler) { 
    this.delegate.insert(name, data, resultHandler);
    return this;
  }

  public Observable<String> insertObservable(String name, JsonObject data) { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    insert(name, data, resultHandler.toHandler());
    return resultHandler;
  }

  public TestConnection commit(Handler<AsyncResult<String>> resultHandler) { 
    this.delegate.commit(resultHandler);
    return this;
  }

  public Observable<String> commitObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    commit(resultHandler.toHandler());
    return resultHandler;
  }

  public TestConnection rollback(Handler<AsyncResult<String>> resultHandler) { 
    this.delegate.rollback(resultHandler);
    return this;
  }

  public Observable<String> rollbackObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    rollback(resultHandler.toHandler());
    return resultHandler;
  }

  public void close() { 
    this.delegate.close();
  }


  public static TestConnection newInstance(io.vertx.serviceproxy.testmodel.TestConnection arg) {
    return arg != null ? new TestConnection(arg) : null;
  }
}
