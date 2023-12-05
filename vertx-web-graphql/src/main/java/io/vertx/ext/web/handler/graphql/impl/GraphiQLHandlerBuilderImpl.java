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

package io.vertx.ext.web.handler.graphql.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerBuilder;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;

import java.util.function.Function;

public class GraphiQLHandlerBuilderImpl implements GraphiQLHandlerBuilder {

  private static final Function<RoutingContext, MultiMap> DEFAULT_FACTORY = rc -> null;

  private final Vertx vertx;
  private GraphiQLHandlerOptions options;
  private Function<RoutingContext, MultiMap> factory;

  public GraphiQLHandlerBuilderImpl(Vertx vertx) {
    this.vertx = vertx;
    options = new GraphiQLHandlerOptions();
  }

  @Override
  public GraphiQLHandlerBuilder with(GraphiQLHandlerOptions options) {
    this.options = options == null ? new GraphiQLHandlerOptions() : options;
    return this;
  }

  @Override
  public GraphiQLHandlerBuilder withHeadersFactory(Function<RoutingContext, MultiMap> factory) {
    this.factory = factory == null ? DEFAULT_FACTORY : factory;
    return this;
  }

  @Override
  public GraphiQLHandler build() {
    return new GraphiQLHandlerImpl(vertx, options, factory);
  }
}
