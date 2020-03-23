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
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import io.vertx.ext.web.handler.graphql.ApolloWSMessage;
import io.vertx.ext.web.handler.graphql.ApolloWSMessageType;
import io.vertx.ext.web.handler.graphql.ApolloWSOptions;
import org.dataloader.DataLoaderRegistry;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.vertx.core.http.HttpHeaders.*;
import static io.vertx.ext.web.handler.graphql.ApolloWSMessageType.*;

/**
 * @author Rogelio Orts
 */
public class ApolloWSHandlerImpl implements ApolloWSHandler {

  private static final Function<ApolloWSMessage, Object> DEFAULT_QUERY_CONTEXT_FACTORY = context -> context;
  private static final Function<ApolloWSMessage, DataLoaderRegistry> DEFAULT_DATA_LOADER_REGISTRY_FACTORY = rc -> null;
  private static final Function<ApolloWSMessage, Locale> DEFAULT_LOCALE_FACTORY = rc -> null;

  private final GraphQL graphQL;
  private final long keepAlive;

  private Function<ApolloWSMessage, Object> queryContextFactory = DEFAULT_QUERY_CONTEXT_FACTORY;

  private Function<ApolloWSMessage, DataLoaderRegistry> dataLoaderRegistryFactory = DEFAULT_DATA_LOADER_REGISTRY_FACTORY;

  private Function<ApolloWSMessage, Locale> localeFactory = DEFAULT_LOCALE_FACTORY;

  private Handler<ServerWebSocket> connectionHandler;

  private Handler<ServerWebSocket> endHandler;

  private Handler<ApolloWSMessage> messageHandler;

  public ApolloWSHandlerImpl(GraphQL graphQL, ApolloWSOptions options) {
    Objects.requireNonNull(graphQL, "graphQL");
    Objects.requireNonNull(options, "options");
    this.graphQL = graphQL;
    this.keepAlive = options.getKeepAlive();
  }

  @Override
  public synchronized ApolloWSHandler connectionHandler(Handler<ServerWebSocket> connectionHandler) {
    this.connectionHandler = connectionHandler;
    return this;
  }

  @Override
  public synchronized ApolloWSHandler messageHandler(Handler<ApolloWSMessage> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  @Override
  public synchronized ApolloWSHandler endHandler(Handler<ServerWebSocket> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public synchronized ApolloWSHandler queryContext(Function<ApolloWSMessage, Object> factory) {
    queryContextFactory = factory != null ? factory : DEFAULT_QUERY_CONTEXT_FACTORY;
    return this;
  }

  @Override
  public synchronized ApolloWSHandler dataLoaderRegistry(Function<ApolloWSMessage, DataLoaderRegistry> factory) {
    dataLoaderRegistryFactory = factory != null ? factory : DEFAULT_DATA_LOADER_REGISTRY_FACTORY;
    return this;
  }

  @Override
  public synchronized ApolloWSHandler locale(Function<ApolloWSMessage, Locale> factory) {
    localeFactory = factory != null ? factory : DEFAULT_LOCALE_FACTORY;
    return this;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    MultiMap headers = routingContext.request().headers();
    if (headers.contains(CONNECTION) && headers.contains(UPGRADE, WEBSOCKET, true)) {
      ServerWebSocket serverWebSocket = routingContext.request().upgrade();
      handleConnection(routingContext.vertx(), serverWebSocket);
    } else {
      routingContext.next();
    }
  }

  private void handleConnection(Vertx vertx, ServerWebSocket serverWebSocket) {
    Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    Handler<ServerWebSocket> ch;
    synchronized (this) {
      ch = this.connectionHandler;
    }
    if (ch != null) {
      ch.handle(serverWebSocket);
    }

    serverWebSocket.handler(buffer -> {
      JsonObject content = buffer.toJsonObject();
      String opId = content.getString("id");
      ApolloWSMessageType type = from(content.getString("type"));

      if (type == null) {
        sendMessage(serverWebSocket, opId, ERROR, "Unknown message type: " + content.getString("type"));
        return;
      }

      ApolloWSMessage message = new ApolloWSMessageImpl(serverWebSocket, type, content);

      Handler<ApolloWSMessage> mh;
      synchronized (this) {
        mh = this.messageHandler;
      }
      if (mh != null) {
        mh.handle(message);
      }

      switch (type) {
        case CONNECTION_INIT:
          connect(vertx, serverWebSocket);
          break;
        case CONNECTION_TERMINATE:
          serverWebSocket.close();
          break;
        case START:
          start(serverWebSocket, subscriptions, message);
          break;
        case STOP:
          stop(serverWebSocket, subscriptions, opId);
          break;
        default:
          sendMessage(serverWebSocket, opId, ERROR, "Unsupported message type: " + type);
          break;
      }
    });

    serverWebSocket.closeHandler(v -> {
      subscriptions.values().forEach(Subscription::cancel);

      Handler<ServerWebSocket> eh;
      synchronized (this) {
        eh = this.endHandler;
      }
      if (eh != null) {
        eh.handle(serverWebSocket);
      }
    });
  }

  private void connect(Vertx vertx, ServerWebSocket serverWebSocket) {
    sendMessage(serverWebSocket, null, CONNECTION_ACK, null);

    if (keepAlive > 0) {
      sendMessage(serverWebSocket, null, CONNECTION_KEEP_ALIVE, null);
      vertx.setPeriodic(keepAlive, timerId -> {
        if (serverWebSocket.isClosed()) {
          vertx.cancelTimer(timerId);
        } else {
          sendMessage(serverWebSocket, null, CONNECTION_KEEP_ALIVE, null);
        }
      });
    }
  }

  private void start(ServerWebSocket serverWebSocket, Map<String, Subscription> subscriptions, ApolloWSMessage message) {
    String opId = message.content().getString("id");

    // Unsubscribe if it's subscribed
    if (subscriptions.containsKey(opId)) {
      stop(serverWebSocket, subscriptions, opId);
    }

    GraphQLQuery payload = new GraphQLQuery(message.content().getJsonObject("payload"));
    ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();
    builder.query(payload.getQuery());

    Function<ApolloWSMessage, Object> qc;
    synchronized (this) {
      qc = queryContextFactory;
    }
    builder.context(qc.apply(message));

    Function<ApolloWSMessage, DataLoaderRegistry> dlr;
    synchronized (this) {
      dlr = dataLoaderRegistryFactory;
    }
    DataLoaderRegistry registry = dlr.apply(message);
    if (registry != null) {
      builder.dataLoaderRegistry(registry);
    }

    Function<ApolloWSMessage, Locale> l;
    synchronized (this) {
      l = localeFactory;
    }
    Locale locale = l.apply(message);
    if (locale != null) {
      builder.locale(locale);
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
        sendMessage(serverWebSocket, opId, DATA, new JsonObject(executionResult.toSpecification()));
        sendMessage(serverWebSocket, opId, COMPLETE, null);
      }
    });
  }

  private void subscribe(ServerWebSocket serverWebSocket, Map<String, Subscription> subscriptions, String opId, ExecutionResult executionResult) {
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
        sendMessage(serverWebSocket, opId, DATA, new JsonObject(er.toSpecification()));
        subscriptionRef.get().request(1);
      }

      @Override
      public void onError(Throwable t) {
        sendMessage(serverWebSocket, opId, ERROR, new JsonObject().put("message", t.getMessage()));
        subscriptions.remove(opId);
      }

      @Override
      public void onComplete() {
        sendMessage(serverWebSocket, opId, COMPLETE, null);
        subscriptions.remove(opId);
      }
    });
  }

  private void stop(ServerWebSocket serverWebSocket, Map<String, Subscription> subscriptions, String opId) {
    Subscription subscription = subscriptions.get(opId);
    if (subscription != null) {
      subscription.cancel();
      subscriptions.remove(opId);
    }
  }

  private void sendMessage(ServerWebSocket serverWebSocket, String opId, ApolloWSMessageType type, Object payload) {
    Objects.requireNonNull(type, "type is null");
    JsonObject message = new JsonObject();
    if (opId != null) {
      message.put("id", opId);
    }
    message.put("type", type.getText());
    if (payload != null) {
      message.put("payload", payload);
    }
    serverWebSocket.writeTextMessage(message.toString());
  }
}
