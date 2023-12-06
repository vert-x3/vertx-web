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

package io.vertx.ext.web.handler.graphql.impl;

import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.ExecutionInputBuilderWithContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerBuilder;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;

public class GraphQLHandlerBuilderImpl implements GraphQLHandlerBuilder {

  private final GraphQL graphQL;
  private GraphQLHandlerOptions options;
  private Handler<ExecutionInputBuilderWithContext<RoutingContext>> beforeExecuteHandler;

  public GraphQLHandlerBuilderImpl(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @Override
  public GraphQLHandlerBuilder with(GraphQLHandlerOptions options) {
    this.options = options;
    return this;
  }

  @Override
  public GraphQLHandlerBuilder beforeExecute(Handler<ExecutionInputBuilderWithContext<RoutingContext>> beforeExecuteHandler) {
    this.beforeExecuteHandler = beforeExecuteHandler;
    return this;
  }

  @Override
  public GraphQLHandler build() {
    return new GraphQLHandlerImpl(graphQL, options).beforeExecute(beforeExecuteHandler);
  }
}
