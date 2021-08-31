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

import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.impl.ContextInternal;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.ExecutionInputConfig;
import io.vertx.ext.web.handler.graphql.ws.ConnectionInitEvent;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSHandler;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSOptions;
import io.vertx.ext.web.handler.graphql.ws.Message;

import java.util.Objects;

import static io.vertx.core.http.HttpHeaders.*;

public class GraphQLWSHandlerImpl implements GraphQLWSHandler {

  private static final ExecutionInputConfig<Message> DEFAULT_EXECUTION_INPUT_CONFIG = (message, builder) -> {
  };

  private final GraphQL graphQL;
  private final long connectionInitWaitTimeout;

  private ExecutionInputConfig<Message> executionInputConfig = DEFAULT_EXECUTION_INPUT_CONFIG;
  private Handler<ConnectionInitEvent> connectionInitHandler;

  public GraphQLWSHandlerImpl(GraphQL graphQL, GraphQLWSOptions options) {
    Objects.requireNonNull(graphQL, "graphQL instance is null");
    Objects.requireNonNull(options, "options instance is null");
    this.graphQL = graphQL;
    connectionInitWaitTimeout = options.getConnectionInitWaitTimeout();
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
  public GraphQLWSHandler executionInputConfig(ExecutionInputConfig<Message> config) {
    executionInputConfig = config != null ? config : DEFAULT_EXECUTION_INPUT_CONFIG;
    return this;
  }

  synchronized ExecutionInputConfig<Message> getExecutionInputConfig() {
    return executionInputConfig;
  }

  @Override
  public void handle(RoutingContext rc) {
    MultiMap headers = rc.request().headers();
    if (headers.contains(CONNECTION) && headers.contains(UPGRADE, WEBSOCKET, true)) {
      ContextInternal context = (ContextInternal) rc.vertx().getOrCreateContext();
      rc.request().toWebSocket().onComplete(ar -> {
        if (ar.succeeded()) {
          ConnectionHandler handler = new ConnectionHandler(this, context, ar.result());
          handler.handleConnection();
        } else {
          rc.fail(ar.cause());
        }
      });
    } else {
      rc.next();
    }
  }
}
