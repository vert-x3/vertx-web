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
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.impl.ContextInternal;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.ExecutionInputBuilderWithContext;
import io.vertx.ext.web.handler.graphql.ws.ConnectionInitEvent;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSHandler;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSOptions;
import io.vertx.ext.web.handler.graphql.ws.Message;

import static io.vertx.ext.web.handler.graphql.ws.GraphQLWSOptions.DEFAULT_CONNECTION_INIT_WAIT_TIMEOUT;

public class GraphQLWSHandlerImpl implements GraphQLWSHandler {

  private final GraphQL graphQL;
  private final long connectionInitWaitTimeout;

  private Handler<ExecutionInputBuilderWithContext<Message>> beforeExecute;
  private Handler<ConnectionInitEvent> connectionInitHandler;
  private Handler<Message> messageHandler;
  private Handler<ServerWebSocket> endHandler;

  public GraphQLWSHandlerImpl(GraphQL graphQL, GraphQLWSOptions options) {
    this.graphQL = graphQL;
    connectionInitWaitTimeout = options == null ? DEFAULT_CONNECTION_INIT_WAIT_TIMEOUT : options.getConnectionInitWaitTimeout();
  }

  GraphQL getGraphQL() {
    return graphQL;
  }

  long getConnectionInitWaitTimeout() {
    return connectionInitWaitTimeout;
  }

  @Override
  public GraphQLWSHandler connectionInitHandler(Handler<ConnectionInitEvent> connectionInitHandler) {
    this.connectionInitHandler = connectionInitHandler;
    return this;
  }

  synchronized Handler<ConnectionInitEvent> getConnectionInitHandler() {
    return connectionInitHandler;
  }

  @Override
  public GraphQLWSHandler beforeExecute(Handler<ExecutionInputBuilderWithContext<Message>> beforeExecute) {
    this.beforeExecute = beforeExecute;
    return this;
  }

  synchronized Handler<ExecutionInputBuilderWithContext<Message>> getBeforeExecute() {
    return beforeExecute;
  }

  @Override
  public synchronized GraphQLWSHandler messageHandler(Handler<Message> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  synchronized Handler<Message> getMessageHandler() { return messageHandler; }

  @Override
  public synchronized GraphQLWSHandler endHandler(Handler<ServerWebSocket> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  synchronized Handler<ServerWebSocket> getEndHandler() {
    return endHandler;
  }

  @Override
  public void handle(RoutingContext rc) {
    if (HttpUtils.canUpgradeToWebSocket(rc.request())) {
      ContextInternal context = (ContextInternal) rc.vertx().getOrCreateContext();
      rc
        .request()
        .toWebSocket()
        .onFailure(rc::fail)
        .onSuccess(socket -> {
          ConnectionHandler handler = new ConnectionHandler(this, context, socket, rc);
          handler.handleConnection();
        });
    } else {
      rc.next();
    }
  }
}
