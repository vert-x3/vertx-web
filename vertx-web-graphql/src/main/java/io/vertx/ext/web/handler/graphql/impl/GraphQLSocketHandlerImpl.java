package io.vertx.ext.web.handler.graphql.impl;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.GraphQLSocketHandler;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GraphQLSocketHandlerImpl implements GraphQLSocketHandler {

  private final GraphQL graphQL;

  private Handler<SockJSSocket> endHandler;

  public GraphQLSocketHandlerImpl(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @Override
  public void handle(SockJSSocket sockJSSocket) {
    final Map<String, Subscription> subscriptions = Collections.synchronizedMap(new HashMap<>());

    sockJSSocket.handler(buffer -> {
      try {
        GraphQLMessageWithPayload message = buffer.toJsonObject().mapTo(GraphQLMessageWithPayload.class);
        String opId = message.getId();

        if (message.getType() == null) {
          sendError(sockJSSocket, opId, new Exception("Invalid message type!"));
          return;
        }

        switch (message.getType()) {
          case CONNECTION_INIT:
            sendMessage(sockJSSocket, null, GraphQLMessage.Type.CONNECTION_ACK);
            break;
          case CONNECTION_TERMINATE:
            sockJSSocket.close();
            break;
          case START:
            subscribe(sockJSSocket, subscriptions, message);
            break;
          case STOP:
            unsubscribe(sockJSSocket, subscriptions, opId);
            break;
          default:
            sendError(sockJSSocket, opId, new Exception("Invalid message type!"));
            break;
        }
      } catch (Exception e) {
        sendError(sockJSSocket, null, e);
      }
    });

    sockJSSocket.endHandler(v -> {
      subscriptions.values().forEach(Subscription::cancel);

      if (endHandler != null) {
        endHandler.handle(sockJSSocket);
      }
    });
  }

  @Override
  public GraphQLSocketHandler endHandler(Handler<SockJSSocket> endHandler) {
    this.endHandler = endHandler;

    return this;
  }

  private void subscribe(
    SockJSSocket sockJSSocket,
    Map<String, Subscription> subscriptions,
    GraphQLMessageWithPayload message
  ) {
    String opId = message.getId();

    // Unsubscribe if it's subscribed
    if (subscriptions.containsKey(opId)) {
      unsubscribe(sockJSSocket, subscriptions, opId);
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
          JsonObject data = new JsonObject(er.toSpecification());
          sendMessage(sockJSSocket, opId, GraphQLMessage.Type.DATA, er);

          subscriptionRef.get().request(1);
        }

        @Override
        public void onError(Throwable t) {
          sendError(sockJSSocket, opId, t);
        }

        @Override
        public void onComplete() {
          sendMessage(sockJSSocket, opId, GraphQLMessage.Type.COMPLETE);
          subscriptions.remove(opId);
        }
      });
    });
  }

  private void unsubscribe(SockJSSocket sockJSSocket, Map<String, Subscription> subscriptions, String opId) {
    Subscription subscription = subscriptions.get(opId);

    if (subscription != null) {
      subscription.cancel();
      subscriptions.remove(opId);
    }
  }

  private void sendMessage(SockJSSocket sockJSSocket, String opId, GraphQLMessage.Type type, ExecutionResult payload) {
    final GraphQLMessageWithExecutionResult message = new GraphQLMessageWithExecutionResult();
    message.setId(opId);
    message.setType(type);
    message.setPayload(payload);

    sockJSSocket.write(JsonObject.mapFrom(message).toBuffer());
  }

  private void sendMessage(SockJSSocket sockJSSocket, String opId, GraphQLMessage.Type type) {
    final GraphQLMessage message = new GraphQLMessage();
    message.setId(opId);
    message.setType(type);

    sendMessage(sockJSSocket, message);
  }

  private void sendError(SockJSSocket sockJSSocket, String opId, Throwable throwable) {
    GraphQLMessageWithError message = new GraphQLMessageWithError();
    message.setId(opId);
    message.setType(GraphQLMessage.Type.ERROR);

    GraphQLMessageWithError.Error error = new GraphQLMessageWithError.Error();
    error.setMessage(throwable.getMessage());
    message.setPayload(error);

    sendMessage(sockJSSocket, message);
  }

  private void sendMessage(SockJSSocket sockJSSocket, GraphQLMessage message) {
    sockJSSocket.write(JsonObject.mapFrom(message).toBuffer());
  }

}
