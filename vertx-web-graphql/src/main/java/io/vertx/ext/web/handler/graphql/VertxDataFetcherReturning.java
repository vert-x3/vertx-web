package io.vertx.ext.web.handler.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * A {@link DataFetcher} that works well with Vert.x Future API.
 *
 * @author Rogelio Orts
 */
class VertxDataFetcherReturning<T> implements DataFetcher<CompletionStage<T>> {

  private final Function<DataFetchingEnvironment, Future<T>> dataFetcher;

  /**
   * Create a new data fetcher.
   * The provided function will be invoked with the following arguments:
   * <ul>
   * <li>the {@link DataFetchingEnvironment}</li>
   * </ul>
   * The provided function will return a Future.
   */
  public VertxDataFetcherReturning(Function<DataFetchingEnvironment, Future<T>> dataFetcher) {
    this.dataFetcher = dataFetcher;
  }

  @Override
  public CompletionStage<T> get(DataFetchingEnvironment environment) throws Exception {
    CompletableFuture<T> cf = new CompletableFuture<>();
    Future<T> future = dataFetcher.apply(environment);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        cf.complete(ar.result());
      } else {
        cf.completeExceptionally(ar.cause());
      }
    });
    return cf;
  }

}
