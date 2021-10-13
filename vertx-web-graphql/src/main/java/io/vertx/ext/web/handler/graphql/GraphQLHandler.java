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

package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import graphql.GraphQLContext;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.impl.GraphQLHandlerImpl;
import org.dataloader.DataLoaderRegistry;

import java.util.Locale;
import java.util.function.Function;

import static io.vertx.codegen.annotations.GenIgnore.PERMITTED_TYPE;

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
   * Retrieves the {@link RoutingContext} from the {@link GraphQLContext}.
   *
   * @param graphQlContext the GraphQL context object
   * @return the {@link RoutingContext}
   */
  @GenIgnore(PERMITTED_TYPE)
  static RoutingContext getRoutingContext(GraphQLContext graphQlContext) {
    return graphQlContext.get(RoutingContext.class);
  }

  /**
   * Customize the query context object.
   * The provided {@code factory} method will be invoked for each incoming GraphQL request.
   *
   * @return a reference to this, so the API can be used fluently
   * @deprecated as of 4.2, use {@link #beforeExecute(Handler)} instead
   */
  @Fluent
  @Deprecated
  GraphQLHandler queryContext(Function<RoutingContext, Object> factory);

  /**
   * Customize the {@link DataLoaderRegistry}.
   * The provided {@code factory} method will be invoked for each incoming GraphQL request.
   *
   * @return a reference to this, so the API can be used fluently
   * @deprecated as of 4.2, use {@link #beforeExecute(Handler)} instead
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Deprecated
  GraphQLHandler dataLoaderRegistry(Function<RoutingContext, DataLoaderRegistry> factory);

  /**
   * Customize the {@link Locale} passed to the GraphQL execution engine.
   * The provided {@code factory} method will be invoked for each incoming GraphQL request.
   *
   * @return a reference to this, so the API can be used fluently
   * @deprecated as of 4.2, use {@link #beforeExecute(Handler)} instead
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Deprecated
  GraphQLHandler locale(Function<RoutingContext, Locale> factory);

  /**
   * Set a callback to invoke before executing a GraphQL query.
   *
   * @param config the callback to invoke
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLHandler beforeExecute(Handler<ExecutionInputBuilderWithContext<RoutingContext>> config);
}
