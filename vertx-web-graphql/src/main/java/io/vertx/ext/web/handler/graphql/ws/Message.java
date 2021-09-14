/*
 * Copyright 2021 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql.ws;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

/**
 * A <a href="https://github.com/enisdenjo/graphql-ws/blob/master/PROTOCOL.md">GraphQL over WebSocket Protocol</a> message.
 */
@VertxGen
public interface Message {

  /**
   * @return the websocket that received the message
   */
  ServerWebSocket socket();

  /**
   * @return the message type
   */
  MessageType type();

  /**
   * @return the message content
   */
  JsonObject message();

  /**
   * @return the connection params
   */
  Object connectionParams();
}
