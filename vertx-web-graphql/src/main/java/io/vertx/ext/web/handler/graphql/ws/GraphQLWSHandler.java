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

import graphql.GraphQL;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.handler.ProtocolUpgradeHandler;
import io.vertx.ext.web.handler.graphql.impl.ws.GraphQLWSHandlerBuilderImpl;

import java.util.Objects;

import static io.vertx.codegen.annotations.GenIgnore.PERMITTED_TYPE;

/**
 * A handler for the <a href="https://github.com/enisdenjo/graphql-ws/blob/master/PROTOCOL.md">GraphQL over WebSocket Protocol</a>.
 */
@VertxGen
public interface GraphQLWSHandler extends ProtocolUpgradeHandler {

  /**
   * Create a new {@link GraphQLWSHandlerBuilder} that will use the provided {@code graphQL} to build a {@link GraphQLWSHandler}.
   */
  @GenIgnore(PERMITTED_TYPE)
  static GraphQLWSHandlerBuilder builder(GraphQL graphQL) {
    return new GraphQLWSHandlerBuilderImpl(Objects.requireNonNull(graphQL, "graphQL instance is null"));
  }
}
