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
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Rogelio Orts
 */
public class ApolloWSHandlerImpl implements ApolloWSHandler {

  private final static String HEADER_CONNECTION_UPGRADE_VALUE = "Upgrade";
  private final static String HEADER_SEC_WEBSOCKET_PROTOCOL_KEY = "Sec-WebSocket-Protocol";
  private final static String HEADER_SEC_WEBSOCKET_PROTOCOL_VALUE = "graphql-ws";

  private final GraphQL graphQL;

  private Handler<ServerWebSocket> endHandler;

  public ApolloWSHandlerImpl(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    MultiMap headers = routingContext.request().headers();
    if(
      headers.contains(HttpHeaders.CONNECTION)
        &&
      HEADER_CONNECTION_UPGRADE_VALUE.equals(headers.get(HttpHeaders.CONNECTION))
    ) {
      ServerWebSocket serverWebSocket = routingContext.request().upgrade();
      handleConnection(serverWebSocket);
    } else {
      routingContext.next();
    }
  }

  private void handleConnection(ServerWebSocket serverWebSocket) {
    final Map<String, Subscription> subscriptions = Collections.synchronizedMap(new HashMap<>());

    serverWebSocket.handler(buffer -> {
      try {
        GraphQLMessageWithPayload message = buffer.toJsonObject().mapTo(GraphQLMessageWithPayload.class);
        String opId = message.getOpId();

        if (message.getType() == null) {
          sendError(serverWebSocket, opId, new Exception("Invalid message type!"));
          return;
        }

        switch (message.getType()) {
          case CONNECTION_INIT:
            sendMessage(serverWebSocket, null, GraphQLMessage.Type.CONNECTION_ACK);
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
            sendError(serverWebSocket, opId, new Exception("Invalid message type!"));
            break;
        }
      } catch (Exception e) {
        sendError(serverWebSocket, null, e);
      }
    });

    serverWebSocket.endHandler(v -> {
      subscriptions.values().forEach(Subscription::cancel);

      if (endHandler != null) {
        endHandler.handle(serverWebSocket);
      }
    });
  }

  @Override
  public ApolloWSHandler endHandler(Handler<ServerWebSocket> endHandler) {
    this.endHandler = endHandler;

    return this;
  }

  private void start(
    ServerWebSocket serverWebSocket,
    Map<String, Subscription> subscriptions,
    GraphQLMessageWithPayload message
  ) {
    String opId = message.getOpId();

    // Unsubscribe if it's subscribed
    if (subscriptions.containsKey(opId)) {
      stop(serverWebSocket, subscriptions, opId);
    }

    GraphQLQuery payload = message.getPayload();
    ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();
    builder.query(payload.getQuery());
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
    ServerWebSocket serverWebSocket,
    Map<String, Subscription> subscriptions,
    String opId,
    ExecutionResult executionResult
  ) {
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
        sendMessage(serverWebSocket, opId, GraphQLMessage.Type.DATA, er);

        subscriptionRef.get().request(1);
      }

      @Override
      public void onError(Throwable t) {
        sendError(serverWebSocket, opId, t);
      }

      @Override
      public void onComplete() {
        sendMessage(serverWebSocket, opId, GraphQLMessage.Type.COMPLETE);
        subscriptions.remove(opId);
      }
    });
  }

  private void sendBackExecutionResult(
    ServerWebSocket serverWebSocket,
    String opId,
    ExecutionResult executionResult
  ) {
    sendMessage(serverWebSocket, opId, GraphQLMessage.Type.DATA, executionResult);
  }

  private void stop(ServerWebSocket serverWebSocket, Map<String, Subscription> subscriptions, String opId) {
    Subscription subscription = subscriptions.get(opId);

    if (subscription != null) {
      subscription.cancel();
      subscriptions.remove(opId);
    }
  }

  private void sendMessage(ServerWebSocket serverWebSocket, String opId, GraphQLMessage.Type type, ExecutionResult payload) {
    final GraphQLMessageWithExecutionResult message = new GraphQLMessageWithExecutionResult();
    message.setOpId(opId);
    message.setType(type);
    message.setPayload(payload);

    serverWebSocket.writeTextMessage(JsonObject.mapFrom(message).toString());
  }

  private void sendMessage(ServerWebSocket serverWebSocket, String opId, GraphQLMessage.Type type) {
    final GraphQLMessage message = new GraphQLMessage();
    message.setOpId(opId);
    message.setType(type);

    sendMessage(serverWebSocket, message);
  }

  private void sendError(ServerWebSocket serverWebSocket, String opId, Throwable throwable) {
    GraphQLMessageWithError message = new GraphQLMessageWithError();
    message.setOpId(opId);
    message.setType(GraphQLMessage.Type.ERROR);

    GraphQLMessageWithError.Error error = new GraphQLMessageWithError.Error();
    error.setMessage(throwable.getMessage());
    message.setPayload(error);

    sendMessage(serverWebSocket, message);
  }

  private void sendMessage(ServerWebSocket serverWebSocket, GraphQLMessage message) {
    serverWebSocket.writeTextMessage(JsonObject.mapFrom(message).toString());
  }

}
