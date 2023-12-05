/*
 * Copyright 2023 Red Hat, Inc.
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

import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.ExecutionInputBuilderWithContext;
import io.vertx.ext.web.handler.graphql.ws.ConnectionInitEvent;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSHandler;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSOptions;
import io.vertx.ext.web.handler.graphql.ws.Message;

import static io.vertx.ext.web.impl.Utils.canUpgradeToWebsocket;

public class GraphQLWSHandlerImpl implements GraphQLWSHandler {

  private final GraphQL graphQL;
  private final long connectionInitWaitTimeout;
  private final Handler<ConnectionInitEvent> connectionInitHandler;
  private final Handler<ExecutionInputBuilderWithContext<Message>> beforeExecuteHandler;
  private final Handler<Message> messageHandler;
  private final Handler<ServerWebSocket> endHandler;

  public GraphQLWSHandlerImpl(GraphQL graphQL, GraphQLWSOptions options, Handler<ConnectionInitEvent> connectionInitHandler, Handler<ExecutionInputBuilderWithContext<Message>> beforeExecuteHandler, Handler<Message> messageHandler, Handler<ServerWebSocket> endHandler) {
    this.graphQL = graphQL;
    this.connectionInitWaitTimeout = options.getConnectionInitWaitTimeout();
    this.connectionInitHandler = connectionInitHandler;
    this.beforeExecuteHandler = beforeExecuteHandler;
    this.messageHandler = messageHandler;
    this.endHandler = endHandler;
  }

  @Override
  public void handle(RoutingContext rc) {
    if (canUpgradeToWebsocket(rc.request())) {
      rc
        .request()
        .toWebSocket()
        .onFailure(rc::fail)
        .onSuccess(socket -> {
          ConnectionHandler handler = new ConnectionHandler(graphQL, connectionInitWaitTimeout, connectionInitHandler, beforeExecuteHandler, messageHandler, endHandler, rc, socket);
          handler.handleConnection();
        });
    } else {
      rc.next();
    }
  }
}
