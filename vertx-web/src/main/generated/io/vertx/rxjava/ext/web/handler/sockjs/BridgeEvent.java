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

package io.vertx.rxjava.ext.web.handler.sockjs;

import java.util.Map;
import rx.Observable;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava.core.Future;
import java.util.function.Function;

/**
 * Represents an event that occurs on the event bus bridge.
 * <p>
 * Please consult the documentation for a full explanation.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.sockjs.BridgeEvent original} non RX-ified interface using Vert.x codegen.
 */

public class BridgeEvent extends Future<Boolean> {

  final io.vertx.ext.web.handler.sockjs.BridgeEvent delegate;

  public BridgeEvent(io.vertx.ext.web.handler.sockjs.BridgeEvent delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public Future<Boolean> setHandler(Handler<AsyncResult<Boolean>> arg0) { 
    delegate.setHandler(arg0);
    return this;
  }

  public Observable<Boolean> setHandlerObservable() { 
    io.vertx.rx.java.ObservableFuture<Boolean> arg0 = io.vertx.rx.java.RxHelper.observableFuture();
    setHandler(arg0.toHandler());
    return arg0;
  }

  public void complete(Boolean arg0) { 
    delegate.complete(arg0);
  }

  public Boolean result() { 
    Boolean ret = delegate.result();
    return ret;
  }

  public <U> Future<U> compose(Handler<Boolean> handler, Future<U> next) { 
    Future<U> ret = Future.newInstance(delegate.compose(handler, (io.vertx.core.Future<U>)next.getDelegate()));
    return ret;
  }

  public <U> Future<U> compose(Function<Boolean,Future<U>> mapper) { 
    Future<U> ret = Future.newInstance(delegate.compose(new java.util.function.Function<java.lang.Boolean,io.vertx.core.Future<U>>() {
      public io.vertx.core.Future<U> apply(java.lang.Boolean arg) {
        Future<U> ret = mapper.apply(arg);
        return (io.vertx.core.Future<U>)ret.getDelegate();
      }
    }));
    return ret;
  }

  public <U> Future<U> map(Function<Boolean,U> mapper) { 
    Future<U> ret = Future.newInstance(delegate.map(new java.util.function.Function<java.lang.Boolean,U>() {
      public U apply(java.lang.Boolean arg) {
        U ret = mapper.apply(arg);
        return ret;
      }
    }));
    return ret;
  }

  public Handler<AsyncResult<Boolean>> completer() { 
    if (cached_0 != null) {
      return cached_0;
    }
    Handler<AsyncResult<Boolean>> ret = new Handler<AsyncResult<Boolean>>() {
      public void handle(AsyncResult<Boolean> ar) {
        if (ar.succeeded()) {
          delegate.completer().handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          delegate.completer().handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    };
    cached_0 = ret;
    return ret;
  }

  /**
   * @return the type of the event
   */
  public BridgeEventType type() { 
    if (cached_1 != null) {
      return cached_1;
    }
    BridgeEventType ret = delegate.type();
    cached_1 = ret;
    return ret;
  }

  /**
   * Use {@link io.vertx.rxjava.ext.web.handler.sockjs.BridgeEvent#getRawMessage} instead, will be removed in 3.3
   * @return 
   */
  public JsonObject rawMessage() { 
    if (cached_2 != null) {
      return cached_2;
    }
    JsonObject ret = delegate.rawMessage();
    cached_2 = ret;
    return ret;
  }

  /**
   * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   * no message involved. If the returned message is modified, {@link io.vertx.rxjava.ext.web.handler.sockjs.BridgeEvent#setRawMessage} should be called with the
   * new message.
   * @return the raw JSON message for the event
   */
  public JsonObject getRawMessage() { 
    JsonObject ret = delegate.getRawMessage();
    return ret;
  }

  /**
   * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   * no message involved.
   * @param message the raw message
   * @return this reference, so it can be used fluently
   */
  public BridgeEvent setRawMessage(JsonObject message) { 
    delegate.setRawMessage(message);
    return this;
  }

  /**
   * Get the SockJSSocket instance corresponding to the event
   * @return the SockJSSocket instance
   */
  public SockJSSocket socket() { 
    if (cached_3 != null) {
      return cached_3;
    }
    SockJSSocket ret = SockJSSocket.newInstance(delegate.socket());
    cached_3 = ret;
    return ret;
  }

  private Handler<AsyncResult<Boolean>> cached_0;
  private BridgeEventType cached_1;
  private JsonObject cached_2;
  private SockJSSocket cached_3;

  public static BridgeEvent newInstance(io.vertx.ext.web.handler.sockjs.BridgeEvent arg) {
    return arg != null ? new BridgeEvent(arg) : null;
  }
}
