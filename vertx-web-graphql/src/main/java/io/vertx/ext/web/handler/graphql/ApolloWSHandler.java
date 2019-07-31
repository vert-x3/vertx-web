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

/**
 * A handler for GraphQL requests sent over Apollo's {@code subscriptions-transport-ws} transport.
 *
 * @author Rogelio Orts
 */
@VertxGen
public interface ApolloWSHandler extends Handler<RoutingContext> {

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
   * Create a new {@link ApolloWSHandler} that will use the provided {@code graphQL} object to execute requests.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static ApolloWSHandler create(GraphQL graphQL) {
    return new ApolloWSHandlerImpl(graphQL);
  }

}
