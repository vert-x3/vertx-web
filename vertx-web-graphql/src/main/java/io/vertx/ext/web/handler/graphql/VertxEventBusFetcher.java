package io.vertx.ext.web.handler.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.eventbus.EventBus;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class VertxEventBusFetcher<T> implements DataFetcher<CompletionStage<Publisher<T>>> {

  private final EventBus eventBus;

  private final String event;

  public VertxEventBusFetcher(EventBus eventBus, String event) {
    this.eventBus = eventBus;
    this.event = event;
  }

  @Override
  public CompletionStage<Publisher<T>> get(DataFetchingEnvironment dataFetchingEnvironment) throws Exception {
    CompletableFuture<Publisher<T>> cf = new CompletableFuture<>();
    MessagePublisher<T> messagePublisher = new MessagePublisher<>();
    cf.complete(messagePublisher);

    eventBus.<T>consumer(event, msg -> {
      T body = msg.body();

      messagePublisher.send(body);
    });

    return cf;
  }

  private class MessagePublisher<T> implements Publisher<T> {
    private Set<Subscriber<? super T>> subscriptions = new HashSet();

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
      subscriptions.add(subscriber);
    }

    public void send(T message) {
      subscriptions.forEach(subscriber -> subscriber.onNext(message));
    }

  }

}
