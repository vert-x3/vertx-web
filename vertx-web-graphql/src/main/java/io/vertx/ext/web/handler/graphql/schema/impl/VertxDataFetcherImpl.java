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

package io.vertx.ext.web.handler.graphql.schema.impl;

import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

public class VertxDataFetcherImpl<T> implements VertxDataFetcher<T> {

  private final BiConsumer<DataFetchingEnvironment, Promise<T>> dataFetcher;

  /**
   * Create a new data fetcher.
   * The provided function will be invoked with the following arguments:
   * <ul>
   * <li>the {@link DataFetchingEnvironment}</li>
   * <li>a future that the implementor must complete after the data objects are fetched</li>
   * </ul>
   */
  public VertxDataFetcherImpl(BiConsumer<DataFetchingEnvironment, Promise<T>> dataFetcher) {
    this.dataFetcher = dataFetcher;
  }

  @Override
  public CompletionStage<T> get(DataFetchingEnvironment environment) {
    CompletableFuture<T> cf = new CompletableFuture<>();
    Promise<T> promise = Promise.promise();
    promise.future().setHandler(ar -> {
      if (ar.succeeded()) {
        cf.complete(ar.result());
      } else {
        cf.completeExceptionally(ar.cause());
      }
    });
    dataFetcher.accept(environment, promise);
    return cf;
  }
}
