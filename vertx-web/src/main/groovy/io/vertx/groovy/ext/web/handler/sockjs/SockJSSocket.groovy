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
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.web.Session
import io.vertx.groovy.ext.auth.User
import io.vertx.groovy.core.buffer.Buffer
import io.vertx.groovy.core.streams.WriteStream
import io.vertx.groovy.core.streams.ReadStream
import io.vertx.groovy.core.MultiMap
import io.vertx.core.Handler
import io.vertx.groovy.core.net.SocketAddress
/**
 *
 * You interact with SockJS clients through instances of SockJS socket.
 * <p>
 * The API is very similar to {@link io.vertx.groovy.core.http.WebSocket}.
 * It implements both  and 
 * so it can be used with
 * {@link io.vertx.groovy.core.streams.Pump} to pump data with flow control.<p>
*/
@CompileStatic
public class SockJSSocket implements ReadStream<Buffer>,  WriteStream<Buffer> {
  private final def io.vertx.ext.web.handler.sockjs.SockJSSocket delegate;
  public SockJSSocket(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void end(Buffer t) {
    ((io.vertx.core.streams.WriteStream) delegate).end(t != null ? (io.vertx.core.buffer.Buffer)t.getDelegate() : null);
  }
  public boolean writeQueueFull() {
    def ret = ((io.vertx.core.streams.WriteStream) delegate).writeQueueFull();
    return ret;
  }
  public SockJSSocket exceptionHandler(Handler<Throwable> handler) {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).exceptionHandler(handler);
    return this;
  }
  public SockJSSocket handler(Handler<Buffer> handler) {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).handler(handler != null ? new Handler<io.vertx.core.buffer.Buffer>(){
      public void handle(io.vertx.core.buffer.Buffer event) {
        handler.handle(InternalHelper.safeCreate(event, io.vertx.groovy.core.buffer.Buffer.class));
      }
    } : null);
    return this;
  }
  public SockJSSocket pause() {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).pause();
    return this;
  }
  public SockJSSocket resume() {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).resume();
    return this;
  }
  public SockJSSocket endHandler(Handler<Void> endHandler) {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).endHandler(endHandler);
    return this;
  }
  public SockJSSocket write(Buffer data) {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).write(data != null ? (io.vertx.core.buffer.Buffer)data.getDelegate() : null);
    return this;
  }
  public SockJSSocket setWriteQueueMaxSize(int maxSize) {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).setWriteQueueMaxSize(maxSize);
    return this;
  }
  public SockJSSocket drainHandler(Handler<Void> handler) {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).drainHandler(handler);
    return this;
  }
  /**
   * When a <code>SockJSSocket</code> is created it automatically registers an event handler with the event bus, the ID of that
   * handler is given by <code>writeHandlerID</code>.
   * <p>
   * Given this ID, a different event loop can send a buffer to that event handler using the event bus and
   * that buffer will be received by this instance in its own event loop and written to the underlying socket. This
   * allows you to write data to other sockets which are owned by different event loops.
   * @return 
   */
  public String writeHandlerID() {
    def ret = delegate.writeHandlerID();
    return ret;
  }
  /**
   * Call {@link io.vertx.groovy.ext.web.handler.sockjs.SockJSSocket#end}.
   */
  public void end() {
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).end();
  }
  /**
   * Close it
   */
  public void close() {
    delegate.close();
  }
  /**
   * Return the remote address for this socket
   * @return 
   */
  public SocketAddress remoteAddress() {
    def ret = InternalHelper.safeCreate(delegate.remoteAddress(), io.vertx.groovy.core.net.SocketAddress.class);
    return ret;
  }
  /**
   * Return the local address for this socket
   * @return 
   */
  public SocketAddress localAddress() {
    def ret = InternalHelper.safeCreate(delegate.localAddress(), io.vertx.groovy.core.net.SocketAddress.class);
    return ret;
  }
  /**
   * Return the headers corresponding to the last request for this socket or the websocket handshake
   * Any cookie headers will be removed for security reasons
   * @return 
   */
  public MultiMap headers() {
    def ret = InternalHelper.safeCreate(delegate.headers(), io.vertx.groovy.core.MultiMap.class);
    return ret;
  }
  /**
   * Return the URI corresponding to the last request for this socket or the websocket handshake
   * @return 
   */
  public String uri() {
    def ret = delegate.uri();
    return ret;
  }
  /**
   * @return the Vert.x-Web session corresponding to this socket
   * @return 
   */
  public Session webSession() {
    def ret = InternalHelper.safeCreate(delegate.webSession(), io.vertx.groovy.ext.web.Session.class);
    return ret;
  }
  /**
   *  @return the Vert.x-Web user corresponding to this socket
   * @return 
   */
  public User webUser() {
    def ret = InternalHelper.safeCreate(delegate.webUser(), io.vertx.groovy.ext.auth.User.class);
    return ret;
  }
}
