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

package io.vertx.ext.web.handler.sockjs.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import java.util.UUID;

public abstract class SockJSSocketBase implements SockJSSocket {

  private final MessageConsumer<Buffer> registration;
  protected final Vertx vertx;
  protected Session webSession;
  protected User webUser;

  /**
   * When a {@code SockJSSocket} is created it automatically registers an event handler with the event bus, the ID of that
   * handler is given by {@code writeHandlerID}.<p>
   * Given this ID, a different event loop can send a buffer to that event handler using the event bus and
   * that buffer will be received by this instance in its own event loop and written to the underlying socket. This
   * allows you to write data to other sockets which are owned by different event loops.
   */
  private final String writeHandlerID;

  // Stops IntelliJ flagging an erroneous syntax error!
  @Override
  public abstract SockJSSocket exceptionHandler(Handler<Throwable> handler);

  protected SockJSSocketBase(Vertx vertx, Session webSession, User webUser) {
    this.vertx = vertx;
    this.webSession = webSession;
    this.webUser = webUser;
    Handler<Message<Buffer>> writeHandler = buff -> write(buff.body());
    this.writeHandlerID = UUID.randomUUID().toString();
    this.registration = vertx.eventBus().<Buffer>consumer(writeHandlerID).handler(writeHandler);
  }

  @Override
  public String writeHandlerID() {
    return writeHandlerID;
  }

  @Override
  public void end() {
    close();
  }

  @Override
  public void close() {
    registration.unregister();
  }

  // Only websocket transport allows status code and reason, so in other cases we simply call close()
  public void closeAfterSessionExpired() {
    close();
  }

  @Override
  public Session webSession() {
    return webSession;
  }

  @Override
  public User webUser() {
    return webUser;
  }
}
