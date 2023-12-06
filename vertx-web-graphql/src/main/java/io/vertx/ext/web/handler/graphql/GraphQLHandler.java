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

package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.impl.GraphQLHandlerBuilderImpl;
import io.vertx.ext.web.handler.graphql.impl.GraphQLHandlerImpl;

import java.util.Objects;

/**
 * A {@link io.vertx.ext.web.Route} handler for GraphQL requests.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface GraphQLHandler extends Handler<RoutingContext> {

  /**
   * Create a new {@link GraphQLHandler} that will use the provided {@code graphQL} object to execute queries.
   * <p>
   * The handler will be configured with default {@link GraphQLHandlerOptions options}.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static GraphQLHandler create(GraphQL graphQL) {
    return create(graphQL, null);
  }

  /**
   * Create a new {@link GraphQLHandler} that will use the provided {@code graphQL} object to execute queries.
   * <p>
   * The handler will be configured with the given {@code options}.
   *
   * @param options options for configuring the {@link GraphQLHandler}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static GraphQLHandler create(GraphQL graphQL, GraphQLHandlerOptions options) {
    return new GraphQLHandlerImpl(Objects.requireNonNull(graphQL, "graphQL instance is null"), options, null);
  }

  /**
   * Create a new {@link GraphQLHandlerBuilder} that will use the provided {@code graphQL} to build a {@link GraphQLHandler}.
   * <p>
   * The handler will be configured with default {@link GraphQLHandlerOptions options}.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static GraphQLHandlerBuilder builder(GraphQL graphQL) {
    return new GraphQLHandlerBuilderImpl(Objects.requireNonNull(graphQL, "graphQL instance is null"));
  }
}
