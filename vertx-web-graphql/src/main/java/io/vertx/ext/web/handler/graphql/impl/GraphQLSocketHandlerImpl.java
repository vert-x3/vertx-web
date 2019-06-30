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

  private Handler<Throwable> exceptionHandler;

  private Handler<SockJSSocket> completeHandler;

  private Handler<SockJSSocket> endHandler;

  public GraphQLSocketHandlerImpl(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @Override
  public void handle(SockJSSocket sockJSSocket) {
    final Collection<Subscription> subscriptions = Collections.synchronizedCollection(new ArrayList<>());

    sockJSSocket.handler(buffer -> {
      JsonObject body = buffer.toJsonObject();
      ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();
      builder.query(body.getString("query"));
      String operationName = body.getString("operationName");
      if (operationName != null) {
        builder.operationName(operationName);
      }
      JsonObject variables = body.getJsonObject("variables");
      if (variables != null) {
        builder.variables(variables.getMap());
      }

      graphQL.executeAsync(builder).thenAccept(executionResult -> {
        Publisher<ExecutionResult> publisher = executionResult.getData();

        AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        publisher.subscribe(new Subscriber<ExecutionResult>() {
          @Override
          public void onSubscribe(Subscription s) {
            subscriptionRef.set(s);
            subscriptions.add(s);

            s.request(1);
          }

          @Override
          public void onNext(ExecutionResult er) {
            JsonObject data = new JsonObject(er.toSpecification());
            sockJSSocket.write(data.toBuffer());

            subscriptionRef.get().request(1);
          }

          @Override
          public void onError(Throwable t) {
            if (exceptionHandler != null) {
              exceptionHandler.handle(t);
            }
          }

          @Override
          public void onComplete() {
            if (completeHandler != null) {
              completeHandler.handle(sockJSSocket);
            }
          }
        });
      });
    });

    sockJSSocket.endHandler(v -> {
      subscriptions.forEach(Subscription::cancel);

      if (endHandler != null) {
        endHandler.handle(sockJSSocket);
      }
    });
  }

  @Override
  public GraphQLSocketHandler exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;

    return this;
  }

  @Override
  public GraphQLSocketHandler completeHandler(Handler<SockJSSocket> completeHandler) {
    this.completeHandler = completeHandler;

    return this;
  }

  @Override
  public GraphQLSocketHandler endHandler(Handler<SockJSSocket> endHandler) {
    this.endHandler = endHandler;

    return this;
  }
}
