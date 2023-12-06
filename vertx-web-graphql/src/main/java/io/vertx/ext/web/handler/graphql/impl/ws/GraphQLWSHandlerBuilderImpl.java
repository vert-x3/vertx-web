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
import io.vertx.ext.web.handler.graphql.ExecutionInputBuilderWithContext;
import io.vertx.ext.web.handler.graphql.ws.*;

public class GraphQLWSHandlerBuilderImpl implements GraphQLWSHandlerBuilder {

  private final GraphQL graphQL;
  private GraphQLWSOptions options;
  private Handler<ConnectionInitEvent> connectionInitHandler;
  private Handler<ExecutionInputBuilderWithContext<Message>> beforeExecuteHandler;
  private Handler<Message> messageHandler;
  private Handler<ServerWebSocket> endHandler;

  public GraphQLWSHandlerBuilderImpl(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @Override
  public GraphQLWSHandlerBuilder with(GraphQLWSOptions options) {
    this.options = options;
    return this;
  }

  @Override
  public GraphQLWSHandlerBuilder onConnectionInit(Handler<ConnectionInitEvent> connectionInitHandler) {
    this.connectionInitHandler = connectionInitHandler;
    return this;
  }

  @Override
  public GraphQLWSHandlerBuilder beforeExecute(Handler<ExecutionInputBuilderWithContext<Message>> beforeExecuteHandler) {
    this.beforeExecuteHandler = beforeExecuteHandler;
    return this;
  }

  @Override
  public GraphQLWSHandlerBuilder onMessage(Handler<Message> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  @Override
  public GraphQLWSHandlerBuilder onSocketEnd(Handler<ServerWebSocket> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public GraphQLWSHandler build() {
    return new GraphQLWSHandlerImpl(graphQL, options)
      .connectionInitHandler(connectionInitHandler)
      .beforeExecute(beforeExecuteHandler)
      .messageHandler(messageHandler)
      .endHandler(endHandler);
  }
}
