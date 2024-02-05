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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Function;

/**
 * A builder for {@link GraphiQLHandler} instances.
 */
@VertxGen
public interface GraphiQLHandlerBuilder {

  /**
   * Change the {@link GraphiQLHandlerOptions} to use.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphiQLHandlerBuilder with(GraphiQLHandlerOptions options);

  /**
   * Customize the HTTP headers to add to GraphQL requests sent by the GraphiQL user interface.
   * The result will be applied on top of the fixed set of headers specified in {@link GraphiQLHandlerOptions#getHeaders()}.
   * <p>
   * This can be useful if, for example, the server is protected by authentication.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GraphiQLHandlerBuilder addingHeaders(Function<RoutingContext, MultiMap> factory);

  /**
   * @return a new instance of {@link GraphiQLHandler}
   */
  GraphiQLHandler build();
}
