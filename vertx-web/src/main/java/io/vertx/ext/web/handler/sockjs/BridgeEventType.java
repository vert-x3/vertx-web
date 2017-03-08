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
package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.VertxGen;

/**
 * Bridge Event Types.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public enum BridgeEventType {

  /**
   * This event will occur when a new SockJS socket is created.
   */
  SOCKET_CREATED,

  /**
   * This event will occur when a SockJS socket is closed.
   */
  SOCKET_CLOSED,

  /**
   * This event will occur when SockJS socket is on idle for longer period of time than configured.
   */
  SOCKET_IDLE,

  /**
   * This event will occur when the last ping timestamp is updated for the SockJS socket.
   */
  SOCKET_PING,

  /**
   * This event will occur when a message is attempted to be sent from the client to the server.
   */
  SEND,

  /**
   * This event will occur when a message is attempted to be published from the client to the server.
   */
  PUBLISH,

  /**
   * This event will occur when a message is attempted to be delivered from the server to the client.
   */
  RECEIVE,

  /**
   * This event will occur when a client attempts to register a handler.
   */
  REGISTER,

  /**
   * This event will occur when a client attempts to unregister a handler.
   */
  UNREGISTER
}
