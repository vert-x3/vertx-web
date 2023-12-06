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
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.impl.GraphiQLHandlerBuilderImpl;
import io.vertx.ext.web.handler.graphql.impl.GraphiQLHandlerImpl;

import java.util.Objects;
import java.util.function.Function;

/**
 * A handler for GraphiQL resources.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface GraphiQLHandler extends Handler<RoutingContext> {

  /**
   * Create a new {@link GraphiQLHandlerBuilder} with default {@link GraphiQLHandlerOptions}.
   */
  static GraphiQLHandlerBuilder builder(Vertx vertx) {
    return new GraphiQLHandlerBuilderImpl(Objects.requireNonNull(vertx, "vertx instance is null"));
  }

  /**
   * Create a new {@link GraphiQLHandler}.
   * <p>
   * The handler will be configured with default {@link GraphiQLHandlerOptions options}.
   */
  static GraphiQLHandler create(Vertx vertx) {
    return create(vertx, null);
  }

  /**
   * Create a new {@link GraphiQLHandler}.
   * <p>
   * The handler will be configured with default {@link GraphiQLHandlerOptions options}.
   *
   * @deprecated as of 4.5.1, use {@link #create(Vertx)}, with {@link #router()}
   */
  @Deprecated
  static GraphiQLHandler create() {
    return create(new GraphiQLHandlerOptions());
  }

  /**
   * Create a new {@link GraphiQLHandler}.
   * <p>
   * The handler will be configured with the given {@code options}.
   *
   * @param options options for configuring the {@link GraphiQLHandler}
   */
  static GraphiQLHandler create(Vertx vertx, GraphiQLHandlerOptions options) {
    return new GraphiQLHandlerImpl(Objects.requireNonNull(vertx, "vertx instance is null"), options);
  }

  /**
   * Create a new {@link GraphiQLHandler}.
   * <p>
   * The handler will be configured with the given {@code options}.
   *
   * @param options options for configuring the {@link GraphiQLHandler}
   * @deprecated as of 4.5.1, use {@link #create(Vertx, GraphiQLHandlerOptions)}, with {@link #router()}
   */
  @Deprecated
  static GraphiQLHandler create(GraphiQLHandlerOptions options) {
    return new GraphiQLHandlerImpl(null, options);
  }

  /**
   * Creates a router configured to serve GraphiQL resources.
   *
   * @return a router to be mounted on an existing {@link io.vertx.ext.web.Route}
   * @see io.vertx.ext.web.Route#subRouter(Router)
   */
  Router router();

  /**
   * Customize the HTTP headers to add to GraphQL requests sent by the GraphiQL user interface.
   * The result will be applied on top of the fixed set of headers specified in {@link GraphiQLHandlerOptions#getHeaders()}.
   * <p>
   * This can be useful if, for example, the server is protected by authentication.
   *
   * @return a reference to this, so the API can be used fluently
   * @deprecated as of 4.5.1, use {@link #builder(Vertx)} instead
   */
  @Fluent
  @Deprecated
  GraphiQLHandler graphiQLRequestHeaders(Function<RoutingContext, MultiMap> factory);
}
