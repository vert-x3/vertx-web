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

package io.vertx.ext.web.handler.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@link DataFetcher} that works well with Vert.x future APIs.
 *
 * @author Thomas Segismont
 */
public class VertxDataFetcher<T> implements DataFetcher<CompletionStage<T>> {

  private final DataFetcher<CompletionStage<T>> dataFetcher;

  /**
   * Receiving a BiConsumer the used data fetcher will be a {@link VertxDataFetcherCallback}
   */
  public VertxDataFetcher(BiConsumer<DataFetchingEnvironment, Future<T>> dataFetcher) {
    this.dataFetcher = new VertxDataFetcherCallback(dataFetcher);
  }

  /**
   * Receiving a Function the used data fetcher will be a {@link VertxDataFetcherReturning}
   */
  public VertxDataFetcher(Function<DataFetchingEnvironment, Future<T>> dataFetcher) {
    this.dataFetcher = new VertxDataFetcherReturning<>(dataFetcher);
  }

  @Override
  public CompletionStage<T> get(DataFetchingEnvironment environment) throws Exception {
    return dataFetcher.get(environment);
  }

}
