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

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BaseBridgeEvent;

/**
 * Represents an event that occurs on the event bus bridge.
 * <p>
 * Please consult the documentation for a full explanation.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface BridgeEvent extends BaseBridgeEvent {

  /**
   * Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   * no message involved.
   *
   * @param message the raw message
   * @return this reference, so it can be used fluently
   */
  @Fluent
  BridgeEvent setRawMessage(JsonObject message);

  /**
   * Get the SockJSSocket instance corresponding to the event
   *
   * @return  the SockJSSocket instance
   */
  @CacheReturn
  SockJSSocket socket();
}
