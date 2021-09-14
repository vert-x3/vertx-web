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

package io.vertx.ext.web.handler.graphql.impl.ws;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.ws.Message;
import io.vertx.ext.web.handler.graphql.ws.MessageType;

public class MessageImpl implements Message {

  private final ServerWebSocket socket;
  private final MessageType type;
  private final JsonObject message;
  private final Object connectionParams;

  public MessageImpl(ServerWebSocket socket, MessageType type, JsonObject message) {
    this(socket, type, message, null);
  }

  public MessageImpl(ServerWebSocket socket, MessageType type, JsonObject message, Object connectionParams) {
    this.socket = socket;
    this.type = type;
    this.message = message;
    this.connectionParams = connectionParams;
  }

  @Override
  public ServerWebSocket socket() {
    return socket;
  }

  @Override
  public MessageType type() {
    return type;
  }

  @Override
  public JsonObject message() {
    return message;
  }

  @Override
  public Object connectionParams() {
    return connectionParams;
  }

  public String id() {
    return message.getString("id");
  }
}
