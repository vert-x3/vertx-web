package io.vertx.ext.web.handler.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

/**
 * A {@link DataFetcher} that works well with Vert.x callback-based APIs.
 *
 * @author Thomas Segismont
 */
class VertxDataFetcherCallback<T> implements DataFetcher<CompletionStage<T>> {

  private final BiConsumer<DataFetchingEnvironment, Future<T>> dataFetcher;

  /**
   * Create a new data fetcher.
   * The provided function will be invoked with the following arguments:
   * <ul>
   * <li>the {@link DataFetchingEnvironment}</li>
   * <li>a future that the implementor must complete after the data objects are fetched</li>
   * </ul>
   */
  public VertxDataFetcherCallback(BiConsumer<DataFetchingEnvironment, Future<T>> dataFetcher) {
    this.dataFetcher = dataFetcher;
  }

  @Override
  public CompletionStage<T> get(DataFetchingEnvironment environment) throws Exception {
    CompletableFuture<T> cf = new CompletableFuture<>();
    Future<T> future = Future.future();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        cf.complete(ar.result());
      } else {
        cf.completeExceptionally(ar.cause());
      }
    });
    dataFetcher.accept(environment, future);
    return cf;
  }

}
