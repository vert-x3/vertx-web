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
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Future;

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

  /**
   * @return  the type of the event
   * @return 
   */
  public BridgeEventType type() { 
    if (cached_0 != null) {
      return cached_0;
    }
    BridgeEventType ret = this.delegate.type();
    cached_0 = ret;
    return ret;
  }

  /**
   * Use {@link io.vertx.ext.web.handler.sockjs.BridgeEvent} instead, will be removed in 3.3
   * @return 
   */
  public JsonObject rawMessage() { 
    if (cached_1 != null) {
      return cached_1;
    }
    JsonObject ret = this.delegate.rawMessage();
    cached_1 = ret;
    return ret;
  }

  /**
   * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   * no message involved. If the returned message is modified, {@link io.vertx.ext.web.handler.sockjs.BridgeEvent} should be called with the
   * new message.
   * @return the raw JSON message for the event
   */
  public JsonObject getRawMessage() { 
    JsonObject ret = this.delegate.getRawMessage();
    return ret;
  }

  /**
   * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   * no message involved.
   * @param message the raw message
   * @return this reference, so it can be used fluently
   */
  public BridgeEvent setRawMessage(JsonObject message) { 
    this.delegate.setRawMessage(message);
    return this;
  }

  /**
   * Get the SockJSSocket instance corresponding to the event
   * @return the SockJSSocket instance
   */
  public SockJSSocket socket() { 
    if (cached_2 != null) {
      return cached_2;
    }
    SockJSSocket ret= SockJSSocket.newInstance(this.delegate.socket());
    cached_2 = ret;
    return ret;
  }

  private io.vertx.ext.web.handler.sockjs.BridgeEventType cached_0;
  private io.vertx.core.json.JsonObject cached_1;
  private SockJSSocket cached_2;

  public static BridgeEvent newInstance(io.vertx.ext.web.handler.sockjs.BridgeEvent arg) {
    return arg != null ? new BridgeEvent(arg) : null;
  }
}
