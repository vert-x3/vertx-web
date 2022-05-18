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

package io.vertx.ext.web.handler.graphql.impl;

import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ContextInternal;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.*;
import io.vertx.ext.web.impl.Origin;
import org.dataloader.DataLoaderRegistry;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * @author Rogelio Orts
 */
public class ApolloWSHandlerImpl implements ApolloWSHandler {

  private static final Function<ApolloWSMessage, Object> DEFAULT_QUERY_CONTEXT_FACTORY = context -> context;
  private static final Function<ApolloWSMessage, DataLoaderRegistry> DEFAULT_DATA_LOADER_REGISTRY_FACTORY = rc -> null;
  private static final Function<ApolloWSMessage, Locale> DEFAULT_LOCALE_FACTORY = rc -> null;

  private final GraphQL graphQL;
  private final long keepAlive;
  private final Origin origin;

  private Function<ApolloWSMessage, Object> queryContextFactory = DEFAULT_QUERY_CONTEXT_FACTORY;
  private Function<ApolloWSMessage, DataLoaderRegistry> dataLoaderRegistryFactory = DEFAULT_DATA_LOADER_REGISTRY_FACTORY;
  private Function<ApolloWSMessage, Locale> localeFactory = DEFAULT_LOCALE_FACTORY;
  private Handler<ServerWebSocket> connectionHandler;
  private Handler<ApolloWSConnectionInitEvent> connectionInitHandler;
  private Handler<ServerWebSocket> endHandler;
  private Handler<ApolloWSMessage> messageHandler;
  private Handler<ExecutionInputBuilderWithContext<ApolloWSMessage>> beforeExecute;

  public ApolloWSHandlerImpl(GraphQL graphQL, ApolloWSOptions options) {
    Objects.requireNonNull(graphQL, "graphQL");
    Objects.requireNonNull(options, "options");
    this.graphQL = graphQL;
    this.keepAlive = options.getKeepAlive();
    this.origin = options.getOrigin() != null ? Origin.parse(options.getOrigin()) : null;
  }

  GraphQL getGraphQL() {
    return graphQL;
  }

  long getKeepAlive() {
    return keepAlive;
  }

  @Override
  public synchronized ApolloWSHandler connectionHandler(Handler<ServerWebSocket> connectionHandler) {
    this.connectionHandler = connectionHandler;
    return this;
  }

  synchronized Handler<ServerWebSocket> getConnectionHandler() {
    return connectionHandler;
  }

  @Override
  public ApolloWSHandler connectionInitHandler(Handler<ApolloWSConnectionInitEvent> connectionInitHandler) {
    this.connectionInitHandler = connectionInitHandler;
    return this;
  }

  synchronized Handler<ApolloWSConnectionInitEvent> getConnectionInitHandler() {
    return connectionInitHandler;
  }

  @Override
  public synchronized ApolloWSHandler messageHandler(Handler<ApolloWSMessage> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  synchronized Handler<ApolloWSMessage> getMessageHandler() {
    return messageHandler;
  }

  @Override
  public synchronized ApolloWSHandler endHandler(Handler<ServerWebSocket> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  synchronized Handler<ServerWebSocket> getEndHandler() {
    return endHandler;
  }

  @Override
  public synchronized ApolloWSHandler queryContext(Function<ApolloWSMessage, Object> factory) {
    queryContextFactory = factory != null ? factory : DEFAULT_QUERY_CONTEXT_FACTORY;
    return this;
  }

  synchronized Function<ApolloWSMessage, Object> getQueryContext() {
    return queryContextFactory;
  }

  @Override
  public synchronized ApolloWSHandler dataLoaderRegistry(Function<ApolloWSMessage, DataLoaderRegistry> factory) {
    dataLoaderRegistryFactory = factory != null ? factory : DEFAULT_DATA_LOADER_REGISTRY_FACTORY;
    return this;
  }

  synchronized Function<ApolloWSMessage, DataLoaderRegistry> getDataLoaderRegistry() {
    return dataLoaderRegistryFactory;
  }

  @Override
  public synchronized ApolloWSHandler locale(Function<ApolloWSMessage, Locale> factory) {
    localeFactory = factory != null ? factory : DEFAULT_LOCALE_FACTORY;
    return this;
  }

  synchronized Function<ApolloWSMessage, Locale> getLocale() {
    return localeFactory;
  }

  @Override
  public ApolloWSHandler beforeExecute(Handler<ExecutionInputBuilderWithContext<ApolloWSMessage>> beforeExecute) {
    this.beforeExecute = beforeExecute;
    return this;
  }

  synchronized Handler<ExecutionInputBuilderWithContext<ApolloWSMessage>> getBeforeExecute() {
    return beforeExecute;
  }

  @Override
  public void handle(RoutingContext ctx) {
    MultiMap headers = ctx.request().headers();
    if (headers.contains(CONNECTION) && headers.contains(UPGRADE, WEBSOCKET, true)) {
      if (!Origin.check(origin, ctx)) {
        ctx.fail(403, new IllegalStateException("Invalid Origin"));
        return;
      }
      ContextInternal context = (ContextInternal) ctx.vertx().getOrCreateContext();
      ctx
        .request()
        .pause()
        .toWebSocket()
        .onFailure(ctx::fail)
        .onSuccess(ws -> {
          ApolloWSConnectionHandler connectionHandler = new ApolloWSConnectionHandler(this, context, ws);
          connectionHandler.handleConnection();
        });
    } else {
      ctx.next();
    }
  }
}
