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

import graphql.GraphQL;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.impl.GraphQLHandlerImpl;
import org.dataloader.DataLoaderRegistry;

import java.util.function.Function;

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
    return create(graphQL, new GraphQLHandlerOptions());
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
    return new GraphQLHandlerImpl(graphQL, options);
  }

  /**
   * Customize the query context object.
   * The provided {@code factory} method will be invoked for each incoming GraphQL request.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLHandler queryContext(Function<RoutingContext, Object> factory);

  /**
   * Customize the {@link DataLoaderRegistry}.
   * The provided {@code factory} method will be invoked for each incoming GraphQL request.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  GraphQLHandler dataLoaderRegistry(Function<RoutingContext, DataLoaderRegistry> factory);

  /**
   * Customize the HTTP headers to add to GraphQL requests sent by the GraphiQL user interface.
   * The result will be applied on top of the fixed set of headers specified in {@link GraphiQLOptions#getHeaders()}.
   * <p>
   * This can be useful if, for example, the server is protected by authentication.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLHandler graphiQLRequestHeaders(Function<RoutingContext, MultiMap> factory);
}
