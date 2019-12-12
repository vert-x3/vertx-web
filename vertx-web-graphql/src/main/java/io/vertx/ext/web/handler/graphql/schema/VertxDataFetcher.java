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
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.schema.impl.VertxDataFetcherImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import static io.vertx.codegen.annotations.GenIgnore.PERMITTED_TYPE;

/**
 * A {@link DataFetcher} that works well with Vert.x callback-based APIs.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface VertxDataFetcher<T> extends DataFetcher<CompletionStage<T>> {

  /**
   * Create a new data fetcher.
   * The provided function will be invoked with the following arguments:
   * <ul>
   * <li>the {@link DataFetchingEnvironment}</li>
   * <li>a future that the implementor must complete after the data objects are fetched</li>
   * </ul>
   */
  @GenIgnore(PERMITTED_TYPE)
  static <T> VertxDataFetcher<T> create(BiConsumer<DataFetchingEnvironment, Promise<T>> dataFetcher) {
    return new VertxDataFetcherImpl<>(dataFetcher);
  }
}
