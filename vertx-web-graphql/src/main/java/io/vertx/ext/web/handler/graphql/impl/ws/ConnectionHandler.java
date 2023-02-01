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

package io.vertx.ext.web.handler.graphql.impl.ws;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.execution.preparsed.persisted.PersistedQuerySupport;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.ExecutionInputBuilderWithContext;
import io.vertx.ext.web.handler.graphql.impl.GraphQLQuery;
import io.vertx.ext.web.handler.graphql.ws.ConnectionInitEvent;
import io.vertx.ext.web.handler.graphql.ws.Message;
import io.vertx.ext.web.handler.graphql.ws.MessageType;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import static io.vertx.ext.web.handler.graphql.impl.ErrorUtil.*;
import static io.vertx.ext.web.handler.graphql.ws.MessageType.*;

public class ConnectionHandler {

  private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

  private final GraphQLWSHandlerImpl graphQLWSHandler;
  private final ContextInternal context;
  private final ServerWebSocket socket;

  private ConnectionState state;

  public ConnectionHandler(GraphQLWSHandlerImpl graphQLWSHandler, ContextInternal context, ServerWebSocket socket) {
    this.graphQLWSHandler = graphQLWSHandler;
    this.context = context;
    this.socket = socket;
    state = new InitialState();
  }

  public void handleConnection() {
    socket.closeHandler(this::close)
      .binaryMessageHandler(this::handleBinaryMessage)
      .textMessageHandler(this::handleTextMessage);
  }

  private void handleBinaryMessage(Buffer buffer) {
    handleMessage(new JsonObject(buffer));
  }

  private void handleTextMessage(String text) {
    handleMessage(new JsonObject(text));
  }

  private void handleMessage(JsonObject json) {
    String typeStr = json.getString("type");
    MessageType type = from(typeStr);
    if (type == null) {
      socket.close((short) 4400, "Unknown message type: " + typeStr);
      return;
    }
    MessageImpl message = state.createMessage(type, json);
    state.handleMessage(message);
  }

  private void sendPong(MessageImpl msg) {
    sendMessage(null, PONG, msg.message().getJsonObject("payload"));
  }

  private void sendMessage(String id, MessageType type, Object payload) {
    JsonObject message = new JsonObject();
    if (id != null) {
      message.put("id", id);
    }
    message.put("type", type.getText());
    if (payload != null) {
      message.put("payload", payload);
    }
    socket.writeTextMessage(message.toString());
  }

  private void close(Void unused) {
    state.close();
  }

  private interface ConnectionState {

    MessageImpl createMessage(MessageType type, JsonObject message);

    void handleMessage(MessageImpl msg);

    void close();
  }

  private class InitialState implements ConnectionState, Handler<Long> {

    final long timerId;

    InitialState() {
      timerId = context.setTimer(graphQLWSHandler.getConnectionInitWaitTimeout(), this);
    }

    @Override
    public void handle(Long unused) {
      socket.close((short) 4408, "Connection initialisation timeout");
    }

    @Override
    public MessageImpl createMessage(MessageType type, JsonObject message) {
      return new MessageImpl(socket, type, message);
    }

    @Override
    public void handleMessage(MessageImpl msg) {
      ServerWebSocket socket = msg.socket();
      switch (msg.type()) {
        case CONNECTION_INIT:
          connectionInit(msg);
          break;
        case PING:
          sendPong(msg);
          break;
        case PONG:
          break;
        default:
          socket.close((short) 4401, "Unauthorized");
          break;
      }
    }

    void connectionInit(MessageImpl msg) {
      context.owner().cancelTimer(timerId);
      Handler<ConnectionInitEvent> connectionInitHandler = graphQLWSHandler.getConnectionInitHandler();
      if (connectionInitHandler != null) {
        Promise<Object> connectionPromise = context.promise();
        state = new InitializingState(connectionPromise.future());
        connectionInitHandler.handle(new ConnectionInitEventImpl(msg, connectionPromise));
      } else {
        state = new ReadyState(null);
      }
    }

    @Override
    public void close() {
      context.owner().cancelTimer(timerId);
    }
  }

  private class InitializingState implements ConnectionState, Handler<AsyncResult<Object>> {

    InitializingState(Future<Object> connectionFuture) {
      connectionFuture.onComplete(this);
    }

    @Override
    public void handle(AsyncResult<Object> ar) {
      if (ar.succeeded()) {
        connect(ar.result());
      } else {
        log.trace("Failed to initialize GraphQLWS socket", ar.cause());
        socket.close((short) 4401, "Unauthorized");
      }
    }

    void connect(Object connectionParams) {
      sendMessage(null, CONNECTION_ACK, null);
      state = new ReadyState(connectionParams);
    }

    @Override
    public MessageImpl createMessage(MessageType type, JsonObject message) {
      return new MessageImpl(socket, type, message);
    }

    @Override
    public void handleMessage(MessageImpl msg) {
      switch (msg.type()) {
        case CONNECTION_INIT:
          socket.close((short) 4429, "Too many initialisation requests");
          break;
        case PING:
          sendPong(msg);
          break;
        case PONG:
          break;
        default:
          socket.close((short) 4401, "Unauthorized");
          break;
      }
    }

    @Override
    public void close() {
    }
  }

  private static final Subscription TRANSIENT_SUBSCRIPTION = new Subscription() {

    @Override
    public void request(long l) {
      throw new IllegalStateException();
    }

    @Override
    public void cancel() {
    }
  };

  private class ReadyState implements ConnectionState {

    final Object connectionParams;
    final Executor executor;
    final ConcurrentMap<String, Subscription> subscriptions;

    class Subscriber implements org.reactivestreams.Subscriber<ExecutionResult> {

      final String id;
      volatile Subscription subscription;

      Subscriber(String id) {
        this.id = id;
      }

      @Override
      public void onSubscribe(Subscription s) {
        subscription = s;
        if (!subscriptions.replace(id, TRANSIENT_SUBSCRIPTION, s)) {
          s.cancel();
        } else {
          s.request(1);
        }
      }

      @Override
      public void onNext(ExecutionResult er) {
        sendMessage(id, NEXT, new JsonObject(er.toSpecification()));
        subscription.request(1);
      }

      @Override
      public void onError(Throwable t) {
        sendMessage(id, ERROR, toJsonObject(t));
        subscriptions.remove(id);
      }

      @Override
      public void onComplete() {
        sendMessage(id, COMPLETE, null);
        subscriptions.remove(id);
      }
    }

    ReadyState(Object connectionParams) {
      this.connectionParams = connectionParams;
      executor = task -> context.runOnContext(v -> task.run());
      subscriptions = new ConcurrentHashMap<>();
    }

    @Override
    public MessageImpl createMessage(MessageType type, JsonObject message) {
      return new MessageImpl(socket, type, message, connectionParams);
    }

    @Override
    public void handleMessage(MessageImpl msg) {
      switch (msg.type()) {
        case PING:
          sendPong(msg);
          break;
        case PONG:
          break;
        case SUBSCRIBE:
          subscribe(msg);
          break;
        case COMPLETE:
          unsubscribe(msg);
          break;
        default:
          socket.close((short) 4400, "Unexpected message type: " + msg.type());
          break;
      }
    }

    void subscribe(MessageImpl msg) {
      String id = msg.id();
      if (id == null) {
        socket.close((short) 4400, "Subscribe message must have an ID");
        return;
      }

      if (subscriptions.putIfAbsent(id, TRANSIENT_SUBSCRIPTION) != null) {
        socket.close((short) 4409, "Subscriber for " + id + " already exists");
        return;
      }

      GraphQLQuery payload = new GraphQLQuery(msg.message().getJsonObject("payload"));
      ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();

      String operationName = payload.getOperationName();
      if (operationName != null) {
        builder.operationName(operationName);
      }
      Map<String, Object> variables = payload.getVariables();
      if (variables != null) {
        builder.variables(variables);
      }
      Object initialValue = payload.getInitialValue();
      if (initialValue != null) {
        builder.root(initialValue);
      }
      Map<String, Object> extensions = payload.getExtensions();
      if (extensions != null) {
        builder.extensions(extensions);
      }
      String query = payload.getQuery();
      if (query != null) {
        builder.query(query);
      } else if (extensions != null && extensions.containsKey("persistedQuery")) {
        builder.query(PersistedQuerySupport.PERSISTED_QUERY_MARKER);
      }

      Handler<ExecutionInputBuilderWithContext<Message>> beforeExecute = graphQLWSHandler.getBeforeExecute();
      if (beforeExecute != null) {
        beforeExecute.handle(new ExecutionInputBuilderWithContext<Message>() {
          @Override
          public Message context() {
            return msg;
          }

          @Override
          public ExecutionInput.Builder builder() {
            return builder;
          }
        });
      }

      graphQLWSHandler.getGraphQL().executeAsync(builder).whenCompleteAsync((executionResult, throwable) -> {
        if (throwable == null) {
          if (executionResult.getData() instanceof Publisher) {
            Publisher<ExecutionResult> data = executionResult.getData();
            data.subscribe(new Subscriber(id));

          } else {
            subscriptions.remove(id);
            sendMessage(id, NEXT, new JsonObject(executionResult.toSpecification()));
            sendMessage(id, COMPLETE, null);
          }
        } else {
          subscriptions.remove(id);
          sendMessage(id, ERROR, toJsonObject(throwable));
        }
      }, executor);
    }

    void unsubscribe(MessageImpl msg) {
      Subscription s = subscriptions.remove(msg.id());
      if (s != null) {
        s.cancel();
      }
    }

    @Override
    public void close() {
      subscriptions.values().forEach(Subscription::cancel);
    }
  }
}
