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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.sockjs.SockJSOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import java.util.UUID;

public abstract class SockJSSocketBase implements SockJSSocket {

  private final MessageConsumer<Object> registration;
  protected final Vertx vertx;
  protected final RoutingContext routingContext;

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

  protected SockJSSocketBase(Vertx vertx, RoutingContext rc, SockJSOptions options) {
    this.vertx = vertx;
    this.routingContext = rc;
    if (options.isRegisterWriteHandler()) {
      Handler<Message<Object>> writeHandler = msg -> {
        Object body = msg.body();
        if (body instanceof String) {
          write((String) msg.body());
        } else {
          write((Buffer) msg.body());
        }
      };
      writeHandlerID = UUID.randomUUID().toString();
      MessageConsumer<Object> consumer;
      if (options.isLocalWriteHandler()) {
        consumer = vertx.eventBus().localConsumer(writeHandlerID);
      } else {
        consumer = vertx.eventBus().consumer(writeHandlerID);
      }
      registration = consumer.handler(writeHandler);
    } else {
      writeHandlerID = null;
      registration = null;
    }
  }

  @Override
  public String writeHandlerID() {
    return writeHandlerID;
  }

  @Override
  public Future<Void> end() {
    Promise<Void> promise = ((VertxInternal) vertx).promise();
    if (registration != null) {
      registration.unregister(promise);
    } else {
      promise.complete();
    }
    return promise.future();
  }

  @Override
  public void end(Handler<AsyncResult<Void>> handler) {
    end()
      .onComplete(handler);
  }

  @Override
  public void close() {
    end();
  }

  // Only websocket transport allows status code and reason, so in other cases we simply call close()
  public void closeAfterSessionExpired() {
    close();
  }

  @Override
  public RoutingContext routingContext() {
    return routingContext;
  }

  @Override
  public Session webSession() {
    return routingContext.session();
  }

  @Override
  public User webUser() {
    return routingContext.user();
  }
}
