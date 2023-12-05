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

package io.vertx.ext.web.handler.graphql.ws;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.handler.graphql.ExecutionInputBuilderWithContext;

/**
 * A builder for {@link GraphQLWSHandler} instances.
 */
@VertxGen
public interface GraphQLWSHandlerBuilder {

  /**
   * Change the {@link GraphQLWSOptions} to use.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLWSHandlerBuilder with(GraphQLWSOptions options);

  /**
   * Customize the connection init {@link Handler}.
   * This handler will be called when the {@link MessageType#CONNECTION_INIT} message is received.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLWSHandlerBuilder withConnectionInitHandler(Handler<ConnectionInitEvent> connectionInitHandler);

  /**
   * Set a callback to invoke before executing a GraphQL query.
   *
   * @param beforeExecuteHandler the callback to invoke
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLWSHandlerBuilder withBeforeExecuteHandler(Handler<ExecutionInputBuilderWithContext<Message>> beforeExecuteHandler);

  /**
   * Customize the message {@link Handler}.
   * This handler will be called for each {@link Message} received.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLWSHandlerBuilder withMessageHandler(Handler<Message> messageHandler);

  /**
   * Customize the end {@link Handler}.
   * This handler will be called at the end of each websocket connection.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphQLWSHandlerBuilder withEndHandler(Handler<ServerWebSocket> endHandler);

  /**
   * @return a new instance of {@link GraphQLWSHandler}
   */
  GraphQLWSHandler build();
}
