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

package io.vertx.groovy.ext.web.handler.sockjs;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.ext.web.handler.sockjs.BridgeEvent.Type
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Future
/**
 * Represents an event that occurs on the event bus bridge.
 * <p>
 * Please consult the documentation for a full explanation.
*/
@CompileStatic
public class BridgeEvent extends Future<Boolean> {
  private final def io.vertx.ext.web.handler.sockjs.BridgeEvent delegate;
  public BridgeEvent(Object delegate) {
    super((io.vertx.ext.web.handler.sockjs.BridgeEvent) delegate);
    this.delegate = (io.vertx.ext.web.handler.sockjs.BridgeEvent) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * @return  the type of the event
   * @return 
   */
  public Type type() {
    if (cached_0 != null) {
      return cached_0;
    }
    def ret = this.delegate.type();
    cached_0 = ret;
    return ret;
  }
  /**
   * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   * no message involved.
   * @return the raw JSON message for the event
   */
  public Map<String, Object> rawMessage() {
    if (cached_1 != null) {
      return cached_1;
    }
    def ret = (Map<String, Object>)InternalHelper.wrapObject(this.delegate.rawMessage());
    cached_1 = ret;
    return ret;
  }
  /**
   * Get the SockJSSocket instance corresponding to the event
   * @return the SockJSSocket instance
   */
  public SockJSSocket socket() {
    if (cached_2 != null) {
      return cached_2;
    }
    def ret= InternalHelper.safeCreate(this.delegate.socket(), io.vertx.groovy.ext.web.handler.sockjs.SockJSSocket.class);
    cached_2 = ret;
    return ret;
  }
  private io.vertx.ext.web.handler.sockjs.BridgeEvent.Type cached_0;
  private Map<String, Object> cached_1;
  private SockJSSocket cached_2;
}
