/*
 * Copyright 2020 Red Hat, Inc.
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
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.ApolloWSMessage;
import io.vertx.ext.web.handler.graphql.ApolloWSMessageType;
import org.dataloader.DataLoaderRegistry;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.web.handler.graphql.ApolloWSMessageType.*;

/**
 * @author Rogelio Orts
 */
class ApolloWSConnectionHandler {

  private final ApolloWSHandlerImpl apolloWSHandler;
  private final ServerWebSocket serverWebSocket;
  private final ContextInternal context;
  private final ConcurrentMap<String, Subscription> subscriptions;

  ApolloWSConnectionHandler(ApolloWSHandlerImpl apolloWSHandler, ContextInternal context, ServerWebSocket serverWebSocket) {
    this.apolloWSHandler = apolloWSHandler;
    this.context = context;
    this.serverWebSocket = serverWebSocket;
    subscriptions = new ConcurrentHashMap<>();
  }

  void handleConnection() {
    Handler<ServerWebSocket> ch = apolloWSHandler.getConnectionHandler();
    if (ch != null) {
      ch.handle(serverWebSocket);
    }

    serverWebSocket.handler(buffer -> {
      JsonObject content = buffer.toJsonObject();
      String opId = content.getString("id");
      ApolloWSMessageType type = from(content.getString("type"));

      if (type == null) {
        sendMessage(opId, ERROR, "Unknown message type: " + content.getString("type"));
        return;
      }

      ApolloWSMessage message = new ApolloWSMessageImpl(serverWebSocket, type, content);

      Handler<ApolloWSMessage> mh = apolloWSHandler.getMessageHandler();
      if (mh != null) {
        mh.handle(message);
      }

      switch (type) {
        case CONNECTION_INIT:
          connect();
          break;
        case CONNECTION_TERMINATE:
          serverWebSocket.close();
          break;
        case START:
          start(message);
          break;
        case STOP:
          stop(opId);
          break;
        default:
          sendMessage(opId, ERROR, "Unsupported message type: " + type);
          break;
      }
    });

    serverWebSocket.closeHandler(v -> {
      subscriptions.values().forEach(Subscription::cancel);

      Handler<ServerWebSocket> eh = apolloWSHandler.getEndHandler();
      if (eh != null) {
        eh.handle(serverWebSocket);
      }
    });
  }

  private void connect() {
    sendMessage(null, CONNECTION_ACK, null);

    long keepAlive = apolloWSHandler.getKeepAlive();
    if (keepAlive > 0) {
      sendMessage(null, CONNECTION_KEEP_ALIVE, null);
      context.setPeriodic(keepAlive, timerId -> {
        if (serverWebSocket.isClosed()) {
          context.owner().cancelTimer(timerId);
        } else {
          sendMessage(null, CONNECTION_KEEP_ALIVE, null);
        }
      });
    }
  }

  private void start(ApolloWSMessage message) {
    String opId = message.content().getString("id");

    // Unsubscribe if it's subscribed
    if (subscriptions.containsKey(opId)) {
      stop(opId);
    }

    GraphQLQuery payload = new GraphQLQuery(message.content().getJsonObject("payload"));
    ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();
    builder.query(payload.getQuery());

    builder.context(apolloWSHandler.getQueryContext().apply(message));

    DataLoaderRegistry registry = apolloWSHandler.getDataLoaderRegistry().apply(message);
    if (registry != null) {
      builder.dataLoaderRegistry(registry);
    }

    Locale locale = apolloWSHandler.getLocale().apply(message);
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

    apolloWSHandler.getGraphQL().executeAsync(builder).thenAcceptAsync(executionResult -> {
      if (executionResult.getData() instanceof Publisher) {
        subscribe(opId, executionResult);
      } else {
        sendMessage(opId, DATA, new JsonObject(executionResult.toSpecification()));
        sendMessage(opId, COMPLETE, null);
      }
    }, context);
  }

  private void subscribe(String opId, ExecutionResult executionResult) {
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
        sendMessage(opId, DATA, new JsonObject(er.toSpecification()));
        subscriptionRef.get().request(1);
      }

      @Override
      public void onError(Throwable t) {
        sendMessage(opId, ERROR, new JsonObject().put("message", t.getMessage()));
        subscriptions.remove(opId);
      }

      @Override
      public void onComplete() {
        sendMessage(opId, COMPLETE, null);
        subscriptions.remove(opId);
      }
    });
  }

  private void stop(String opId) {
    Subscription subscription = subscriptions.get(opId);
    if (subscription != null) {
      subscription.cancel();
      subscriptions.remove(opId);
    }
  }

  private void sendMessage(String opId, ApolloWSMessageType type, Object payload) {
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
