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
import rx.Observable;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.serviceproxy.testmodel.TestConnection original} non RX-ified interface using Vert.x codegen.
 */

public class TestConnection {

  public static final io.vertx.lang.rxjava.TypeArg<TestConnection> arg = new io.vertx.lang.rxjava.TypeArg<>(
    obj -> new TestConnection((io.vertx.serviceproxy.testmodel.TestConnection) obj),
    TestConnection::getDelegate
  );

  final io.vertx.serviceproxy.testmodel.TestConnection delegate;
  
  public TestConnection(io.vertx.serviceproxy.testmodel.TestConnection delegate) {
    this.delegate = delegate;
  }

  public io.vertx.serviceproxy.testmodel.TestConnection getDelegate() {
    return delegate;
  }

  public TestConnection startTransaction(Handler<AsyncResult<String>> resultHandler) { 
    delegate.startTransaction(resultHandler);
    return this;
  }

  public Observable<String> startTransactionObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    startTransaction(resultHandler.toHandler());
    return resultHandler;
  }

  public TestConnection insert(String name, JsonObject data, Handler<AsyncResult<String>> resultHandler) { 
    delegate.insert(name, data, resultHandler);
    return this;
  }

  public Observable<String> insertObservable(String name, JsonObject data) { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    insert(name, data, resultHandler.toHandler());
    return resultHandler;
  }

  public TestConnection commit(Handler<AsyncResult<String>> resultHandler) { 
    delegate.commit(resultHandler);
    return this;
  }

  public Observable<String> commitObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    commit(resultHandler.toHandler());
    return resultHandler;
  }

  public TestConnection rollback(Handler<AsyncResult<String>> resultHandler) { 
    delegate.rollback(resultHandler);
    return this;
  }

  public Observable<String> rollbackObservable() { 
    io.vertx.rx.java.ObservableFuture<String> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    rollback(resultHandler.toHandler());
    return resultHandler;
  }

  public void close() { 
    delegate.close();
  }


  public static TestConnection newInstance(io.vertx.serviceproxy.testmodel.TestConnection arg) {
    return arg != null ? new TestConnection(arg) : null;
  }
}
