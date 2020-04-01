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

package io.vertx.ext.web.handler.graphql.schema;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.schema.impl.CallbackDataFetcherImpl;
import io.vertx.ext.web.handler.graphql.schema.impl.FutureDataFetcherImpl;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@link DataFetcher} that works well with Vert.x callback and {@link Future} based APIs.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface VertxDataFetcher<T> extends DataFetcher<CompletionStage<T>> {

  /**
   * Create a new data fetcher that works well with callback based APIs.
   * <p>
   * The provided {@code dataFetcher} will be invoked with the following arguments:
   * <ul>
   * <li>the {@link DataFetchingEnvironment}</li>
   * <li>a {@link Promise} that the implementor must complete after the data objects are fetched</li>
   * </ul>
   * <p>
   * If called from a Vert.x thread, this method will capture the current {@link Context}.
   * The provided {@code dataFetcher} will then be executed on this {@link Context}.
   */
  @GenIgnore
  static <T> VertxDataFetcher<T> create(BiConsumer<DataFetchingEnvironment, Promise<T>> dataFetcher) {
    return create(dataFetcher, Vertx.currentContext());
  }

  /**
   * Like {@link #create(BiConsumer)}, except the method uses the provided {@code context} instead of capturing the current one.
   */
  @GenIgnore
  static <T> VertxDataFetcher<T> create(BiConsumer<DataFetchingEnvironment, Promise<T>> dataFetcher, Context context) {
    return new CallbackDataFetcherImpl<>(dataFetcher, context);
  }

  /**
   * Create a new data fetcher that works well with {@link Future} based APIs.
   * <p>
   * The provided {@code dataFetcher} will be invoked with the following argument:
   * <ul>
   * <li>the {@link DataFetchingEnvironment}</li>
   * </ul>
   * <p>
   * If called from a Vert.x thread, this method will capture the current {@link Context}.
   * The provided {@code dataFetcher} will then be executed on this {@link Context}.
   */
  @GenIgnore
  static <T> VertxDataFetcher<T> create(Function<DataFetchingEnvironment, Future<T>> dataFetcher) {
    return create(dataFetcher, Vertx.currentContext());
  }

  /**
   * Like {@link #create(Function)}, except the method uses the provided {@code context} instead of capturing the current one.
   */
  @GenIgnore
  static <T> VertxDataFetcher<T> create(Function<DataFetchingEnvironment, Future<T>> dataFetcher, Context context) {
    return new FutureDataFetcherImpl<>(dataFetcher, context);
  }
}
