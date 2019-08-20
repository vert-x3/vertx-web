/*
 * Copyright 2019 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.impl.ApolloWSMessageType;

/**
 * @author Rogelio Orts
 */
public class ApolloWSContext {

  private final RoutingContext routingContext;

  private final ServerWebSocket serverWebSocket;

  private final ApolloWSMessageType type;

  private final JsonObject message;

  public ApolloWSContext(
      RoutingContext routingContext, ServerWebSocket serverWebSocket, ApolloWSMessageType type,
      JsonObject message) {
    this.routingContext = routingContext;
    this.serverWebSocket = serverWebSocket;
    this.type = type;
    this.message = message;
  }

  public RoutingContext getRoutingContext() {
    return routingContext;
  }

  public ServerWebSocket getServerWebSocket() {
    return serverWebSocket;
  }

  public ApolloWSMessageType getType() {
    return type;
  }

  public JsonObject getMessage() {
    return message;
  }

}
