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
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.impl.ApolloWSHandlerImpl;
import org.dataloader.DataLoaderRegistry;

import java.util.function.Function;

/**
 * A handler for GraphQL requests sent over Apollo's {@code subscriptions-transport-ws} transport.
 *
 * @author Rogelio Orts
 */
@VertxGen
public interface ApolloWSHandler extends Handler<RoutingContext> {

  /**
   * Customize the message {@link Handler}.
   * This handler will be called for each apollo message received.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ApolloWSHandler messageHandler(Handler<ApolloWSContext> messageHandler);

  /**
   * Customize the conncetion {@link Handler}.
   * This handler will be called at the beginning of each websocket connection.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ApolloWSHandler connectionHandler(Handler<ServerWebSocket> endHandler);

  /**
   * Customize the end {@link Handler}.
   * This handler will be called at the end of each websocket connection.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ApolloWSHandler endHandler(Handler<ServerWebSocket> endHandler);

  /**
   * Customize the query context object.
   * The provided {@code factory} method will be invoked for each incoming GraphQL request.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ApolloWSHandler queryContext(Function<ApolloWSContext, Object> factory);

  /**
   * Customize the {@link DataLoaderRegistry}.
   * The provided {@code factory} method will be invoked for each incoming GraphQL request.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ApolloWSHandler dataLoaderRegistry(Function<ApolloWSContext, DataLoaderRegistry> factory);

  /**
   * Create a new {@link ApolloWSHandler} that will use the provided {@code graphQL} object to execute requests.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static ApolloWSHandler create(GraphQL graphQL) {
    return new ApolloWSHandlerImpl(graphQL, new ApolloWSOptions());
  }

  /**
   * Create a new {@link ApolloWSHandler} that will use the provided {@code graphQL} object to execute requests.
   * <p>
   * The handler will be configured with the given {@code options}.
   *
   * @param options options for configuring the {@link ApolloWSOptions}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static ApolloWSHandler create(GraphQL graphQL, ApolloWSOptions options) {
    return new ApolloWSHandlerImpl(graphQL, options);
  }

}
