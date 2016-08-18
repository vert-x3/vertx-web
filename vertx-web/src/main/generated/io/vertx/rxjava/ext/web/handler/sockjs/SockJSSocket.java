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
import io.vertx.rxjava.ext.web.Session;
import io.vertx.rxjava.ext.auth.User;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.streams.WriteStream;
import io.vertx.rxjava.core.streams.ReadStream;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.core.Handler;
import io.vertx.rxjava.core.net.SocketAddress;

/**
 *
 * You interact with SockJS clients through instances of SockJS socket.
 * <p>
 * The API is very similar to {@link io.vertx.rxjava.core.http.WebSocket}.
 * It implements both  and 
 * so it can be used with
 * {@link io.vertx.rxjava.core.streams.Pump} to pump data with flow control.<p>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.sockjs.SockJSSocket original} non RX-ified interface using Vert.x codegen.
 */

public class SockJSSocket implements ReadStream<Buffer>,  WriteStream<Buffer> {

  final io.vertx.ext.web.handler.sockjs.SockJSSocket delegate;

  public SockJSSocket(io.vertx.ext.web.handler.sockjs.SockJSSocket delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  private rx.Observable<Buffer> observable;

  public synchronized rx.Observable<Buffer> toObservable() {
    if (observable == null) {
      java.util.function.Function<io.vertx.core.buffer.Buffer, Buffer> conv = Buffer::newInstance;
      io.vertx.lang.rxjava.ReadStreamAdapter<io.vertx.core.buffer.Buffer, Buffer> adapter = new io.vertx.lang.rxjava.ReadStreamAdapter<>(this, conv);
      observable = rx.Observable.create(adapter);
    }
    return observable;
  }

  public void end(Buffer t) { 
    delegate.end((io.vertx.core.buffer.Buffer)t.getDelegate());
  }

  public boolean writeQueueFull() { 
    boolean ret = delegate.writeQueueFull();
    return ret;
  }

  public SockJSSocket exceptionHandler(Handler<Throwable> handler) { 
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).exceptionHandler(handler);
    return this;
  }

  public SockJSSocket handler(Handler<Buffer> handler) { 
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).handler(new Handler<io.vertx.core.buffer.Buffer>() {
      public void handle(io.vertx.core.buffer.Buffer event) {
        handler.handle(Buffer.newInstance(event));
      }
    });
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
    ((io.vertx.ext.web.handler.sockjs.SockJSSocket) delegate).write((io.vertx.core.buffer.Buffer)data.getDelegate());
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
    String ret = delegate.writeHandlerID();
    return ret;
  }

  /**
   * Call {@link io.vertx.rxjava.ext.web.handler.sockjs.SockJSSocket#end}.
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
    SocketAddress ret = SocketAddress.newInstance(delegate.remoteAddress());
    return ret;
  }

  /**
   * Return the local address for this socket
   * @return 
   */
  public SocketAddress localAddress() { 
    SocketAddress ret = SocketAddress.newInstance(delegate.localAddress());
    return ret;
  }

  /**
   * Return the headers corresponding to the last request for this socket or the websocket handshake
   * Any cookie headers will be removed for security reasons
   * @return 
   */
  public MultiMap headers() { 
    MultiMap ret = MultiMap.newInstance(delegate.headers());
    return ret;
  }

  /**
   * Return the URI corresponding to the last request for this socket or the websocket handshake
   * @return 
   */
  public String uri() { 
    String ret = delegate.uri();
    return ret;
  }

  /**
   * @return the Vert.x-Web session corresponding to this socket
   */
  public Session webSession() { 
    Session ret = Session.newInstance(delegate.webSession());
    return ret;
  }

  /**
   * @return the Vert.x-Web user corresponding to this socket
   */
  public User webUser() { 
    User ret = User.newInstance(delegate.webUser());
    return ret;
  }


  public static SockJSSocket newInstance(io.vertx.ext.web.handler.sockjs.SockJSSocket arg) {
    return arg != null ? new SockJSSocket(arg) : null;
  }
}
