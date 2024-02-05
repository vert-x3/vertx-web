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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.impl.GraphiQLHandlerBuilderImpl;
import io.vertx.ext.web.handler.graphql.impl.GraphiQLHandlerImpl;

import java.util.Objects;

/**
 * A handler for GraphiQL resources.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface GraphiQLHandler {

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
   * The handler will be configured with the given {@code options}.
   *
   * @param options options for configuring the {@link GraphiQLHandler}
   */
  static GraphiQLHandler create(Vertx vertx, GraphiQLHandlerOptions options) {
    return new GraphiQLHandlerImpl(Objects.requireNonNull(vertx, "vertx instance is null"), options, null);
  }

  /**
   * Create a new {@link GraphiQLHandlerBuilder} with default {@link GraphiQLHandlerOptions}.
   */
  static GraphiQLHandlerBuilder builder(Vertx vertx) {
    return new GraphiQLHandlerBuilderImpl(Objects.requireNonNull(vertx, "vertx instance is null"));
  }

  /**
   * Creates a router configured to serve GraphiQL resources.
   *
   * @return a router to be mounted on an existing {@link io.vertx.ext.web.Route}
   * @see io.vertx.ext.web.Route#subRouter(Router)
   */
  Router router();
}
