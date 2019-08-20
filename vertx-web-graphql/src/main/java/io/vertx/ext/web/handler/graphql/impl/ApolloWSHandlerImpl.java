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

package io.vertx.ext.web.handler.graphql.impl;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.ApolloWSContext;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import io.vertx.ext.web.handler.graphql.ApolloWSOptions;
import org.dataloader.DataLoaderRegistry;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * @author Rogelio Orts
 */
public class ApolloWSHandlerImpl implements ApolloWSHandler {

  private static final Function<ApolloWSContext, Object> DEFAULT_QUERY_CONTEXT_FACTORY = rc -> rc;
  private static final Function<ApolloWSContext, DataLoaderRegistry> DEFAULT_DATA_LOADER_REGISTRY_FACTORY = rc -> null;

  private final static String HEADER_CONNECTION_UPGRADE_VALUE = "upgrade";

  private final GraphQL graphQL;

  private Function<ApolloWSContext, Object> queryContextFactory = DEFAULT_QUERY_CONTEXT_FACTORY;

  private Function<ApolloWSContext, DataLoaderRegistry> dataLoaderRegistryFactory = DEFAULT_DATA_LOADER_REGISTRY_FACTORY;

  private Handler<ServerWebSocket> connectionHandler;

  private Handler<ServerWebSocket> endHandler;

  private Handler<ApolloWSContext> messageHandler;

  private ApolloWSOptions options;

  public ApolloWSHandlerImpl(GraphQL graphQL, ApolloWSOptions options) {
    Objects.requireNonNull(graphQL, "graphQL");
    Objects.requireNonNull(options, "options");
    this.graphQL = graphQL;
    this.options = options;
  }

  @Override
  public synchronized ApolloWSHandler queryContext(Function<ApolloWSContext, Object> factory) {
    queryContextFactory = factory != null ? factory : DEFAULT_QUERY_CONTEXT_FACTORY;
    return this;
  }

  @Override
  public synchronized ApolloWSHandler dataLoaderRegistry(Function<ApolloWSContext, DataLoaderRegistry> factory) {
    dataLoaderRegistryFactory = factory != null ? factory : DEFAULT_DATA_LOADER_REGISTRY_FACTORY;
    return this;
  }

  @Override
  public ApolloWSHandler connectionHandler(Handler<ServerWebSocket> connectionHandler) {
    this.connectionHandler = connectionHandler;

    return this;
  }

  @Override
  public ApolloWSHandler endHandler(Handler<ServerWebSocket> endHandler) {
    this.endHandler = endHandler;

    return this;
  }

  @Override
  public ApolloWSHandler messageHandler(Handler<ApolloWSContext> messageHandler) {
    this.messageHandler = messageHandler;

    return this;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    MultiMap headers = routingContext.request().headers();
    if (headers.contains(CONNECTION) && headers.contains(UPGRADE, WEBSOCKET, true)) {
      ServerWebSocket serverWebSocket = routingContext.request().upgrade();
      handleConnection(routingContext, serverWebSocket);
    } else {
      routingContext.next();
    }
  }

  private void handleConnection(RoutingContext routingContext, ServerWebSocket serverWebSocket) {
    Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    if (connectionHandler != null) {
      connectionHandler.handle(serverWebSocket);
    }

    serverWebSocket.handler(buffer -> {
      try {
        JsonObject message = buffer.toJsonObject();
        String opId = message.getString("id");
        ApolloWSMessageType type = ApolloWSMessageType.from(message.getString("type"));

        if (type == null) {
          sendError(serverWebSocket, opId, "Invalid message type!");
          return;
        }

        ApolloWSContext context = new ApolloWSContext(routingContext, serverWebSocket, type, message);
        if (messageHandler != null) {
          messageHandler.handle(context);
        }

        switch (type) {
          case CONNECTION_INIT:
            connect(routingContext, serverWebSocket);
            break;
          case CONNECTION_TERMINATE:
            serverWebSocket.close();
            break;
          case START:
            start(context, serverWebSocket, subscriptions, message);
            break;
          case STOP:
            stop(serverWebSocket, subscriptions, opId);
            break;
          default:
            sendError(serverWebSocket, opId, "Invalid message type!");
            break;
        }
      } catch (Exception e) {
        sendError(serverWebSocket, null, e.getMessage());
      }
    });

    serverWebSocket.endHandler(v -> {
      subscriptions.values().forEach(Subscription::cancel);

      if (endHandler != null) {
        endHandler.handle(serverWebSocket);
      }
    });
  }

  private void connect(RoutingContext routingContext, ServerWebSocket serverWebSocket) {
    sendMessage(serverWebSocket, null, ApolloWSMessageType.CONNECTION_ACK);

    long keepAlive = options.getKeepAlive();
    if (keepAlive > 0) {
      sendMessage(serverWebSocket, null, ApolloWSMessageType.CONNECTION_KEEP_ALIVE);

      Vertx vertx = routingContext.vertx();
      vertx.setPeriodic(keepAlive, timerId -> {
        if (serverWebSocket.isClosed()) {
          vertx.cancelTimer(timerId);
        } else {
          sendMessage(serverWebSocket, null, ApolloWSMessageType.CONNECTION_KEEP_ALIVE);
        }
      });
    }
  }

  private void start(
      ApolloWSContext context, ServerWebSocket serverWebSocket, Map<String, Subscription> subscriptions,
      JsonObject message) {
    String opId = message.getString("id");

    // Unsubscribe if it's subscribed
    if (subscriptions.containsKey(opId)) {
      stop(serverWebSocket, subscriptions, opId);
    }

    GraphQLQuery payload = message.getJsonObject("payload").mapTo(GraphQLQuery.class);
    ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();
    builder.query(payload.getQuery());

    Function<ApolloWSContext, Object> qc;
    synchronized (this) {
      qc = queryContextFactory;
    }
    builder.context(qc.apply(context));

    Function<ApolloWSContext, DataLoaderRegistry> dlr;
    synchronized (this) {
      dlr = dataLoaderRegistryFactory;
    }
    DataLoaderRegistry registry = dlr.apply(context);
    if (registry != null) {
      builder.dataLoaderRegistry(registry);
    }

    String operationName = payload.getOperationName();
    if (operationName != null) {
      builder.operationName(operationName);
    }
    Map<String, Object> variables = payload.getVariables();
    if (variables != null) {
      builder.variables(variables);
    }

    graphQL.executeAsync(builder).thenAccept(executionResult -> {
      if (executionResult.getData() instanceof Publisher) {
        subscribe(serverWebSocket, subscriptions, opId, executionResult);
      } else {
        sendBackExecutionResult(serverWebSocket, opId, executionResult);
      }
    });
  }

  private void subscribe(
      ServerWebSocket serverWebSocket, Map<String, Subscription> subscriptions, String opId,
      ExecutionResult executionResult) {
    Publisher<ExecutionResult> publisher = executionResult.getData();

    AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
    publisher.subscribe(new Subscriber<ExecutionResult>() {
      @Override
      public void onSubscribe(Subscription s) {
        subscriptionRef.set(s);
        subscriptions.put(opId, s);

        s.request(1);
      }

      @Override
      public void onNext(ExecutionResult er) {
        sendMessage(serverWebSocket, opId, ApolloWSMessageType.DATA, er);

        subscriptionRef.get().request(1);
      }

      @Override
      public void onError(Throwable t) {
        sendError(serverWebSocket, opId, t.getMessage());
        subscriptions.remove(opId);
      }

      @Override
      public void onComplete() {
        sendMessage(serverWebSocket, opId, ApolloWSMessageType.COMPLETE);
        subscriptions.remove(opId);
      }
    });
  }

  private void sendBackExecutionResult(ServerWebSocket serverWebSocket, String opId, ExecutionResult executionResult) {
    sendMessage(serverWebSocket, opId, ApolloWSMessageType.DATA, executionResult);
  }

  private void stop(ServerWebSocket serverWebSocket, Map<String, Subscription> subscriptions, String opId) {
    Subscription subscription = subscriptions.get(opId);

    if (subscription != null) {
      subscription.cancel();
      subscriptions.remove(opId);
    }
  }

  private void sendMessage(
      ServerWebSocket serverWebSocket, String opId, ApolloWSMessageType type, ExecutionResult payload) {
    JsonObject message = new JsonObject()
      .put("id", opId)
      .put("type", type.getText())
      .put("payload", JsonObject.mapFrom(payload));

    serverWebSocket.writeTextMessage(message.toString());
  }

  private void sendMessage(ServerWebSocket serverWebSocket, String opId, ApolloWSMessageType type) {
    JsonObject message = new JsonObject()
      .put("id", opId)
      .put("type", type.getText());

    sendMessage(serverWebSocket, message);
  }

  private void sendError(ServerWebSocket serverWebSocket, String opId, String errorMsg) {
    JsonObject error = new JsonObject()
      .put("message", errorMsg);
    JsonObject message = new JsonObject()
      .put("id", opId)
      .put("type", ApolloWSMessageType.ERROR.getText())
      .put("payload", error);

    sendMessage(serverWebSocket, message);
  }

  private void sendMessage(ServerWebSocket serverWebSocket, JsonObject message) {
    serverWebSocket.writeTextMessage(message.toString());
  }

}
