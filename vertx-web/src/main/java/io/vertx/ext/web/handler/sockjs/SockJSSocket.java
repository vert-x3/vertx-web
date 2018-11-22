/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.Session;
import io.vertx.ext.auth.User;

/**
 *
 * You interact with SockJS clients through instances of SockJS socket.
 * <p>
 * The API is very similar to {@link io.vertx.core.http.WebSocket}.
 * It implements both {@link ReadStream} and {@link WriteStream}
 * so it can be used with
 * {@link io.vertx.core.streams.Pump} to pump data with flow control.<p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface SockJSSocket extends ReadStream<Buffer>, WriteStream<Buffer> {

  @Override
  SockJSSocket exceptionHandler(Handler<Throwable> handler);

  @Override
  SockJSSocket handler(Handler<Buffer> handler);

  @Override
  SockJSSocket pause();

  @Override
  SockJSSocket resume();

  @Override
  SockJSSocket endHandler(Handler<Void> endHandler);

  @Override
  SockJSSocket write(Buffer data);

  /**
   * Write a {@link String} to the socket, encoded in UTF-8.
   *
   * @param data  the string to write
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default SockJSSocket write(String data) {
    return write(Buffer.buffer(data));
  }

  @Override
  SockJSSocket setWriteQueueMaxSize(int maxSize);

  @Override
  SockJSSocket drainHandler(Handler<Void> handler);

  /**
   * When a {@code SockJSSocket} is created it automatically registers an event handler with the event bus, the ID of that
   * handler is given by {@code writeHandlerID}.
   * <p>
   * Given this ID, a different event loop can send a buffer to that event handler using the event bus and
   * that buffer will be received by this instance in its own event loop and written to the underlying socket. This
   * allows you to write data to other sockets which are owned by different event loops.
   */
  String writeHandlerID();

  /**
   * Call {@link #end()}.
   */
  @Override
  void end();

  /**
   * Close it
   */
  void close();

  /**
   * Close it giving a status code and reason. Only Applicable to RawWebSocket will downgrade to plain close for
   * other transports.
   */
  default void close(int statusCode, String reason) {
    close();
  }

  /**
   * Return the remote address for this socket
   */
  SocketAddress remoteAddress();

  /**
   * Return the local address for this socket
   */
  SocketAddress localAddress();

  /**
   * Return the headers corresponding to the last request for this socket or the websocket handshake
   * Any cookie headers will be removed for security reasons
   */
  MultiMap headers();

  /**
   * Return the URI corresponding to the last request for this socket or the websocket handshake
   */
  String uri();

  /**
   * @return the Vert.x-Web session corresponding to this socket
   */
  @Nullable
  Session webSession();

  /**
   *  @return the Vert.x-Web user corresponding to this socket
   */
  @Nullable
  User webUser();
}
