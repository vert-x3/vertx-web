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

package io.vertx.ext.web.handler.graphql.dataloader;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.graphql.TriConsumer;
import io.vertx.ext.web.handler.graphql.dataloader.impl.CallbackBatchLoaderImpl;
import io.vertx.ext.web.handler.graphql.dataloader.impl.FutureBatchLoaderImpl;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link BatchLoaderWithContext} that works well with Vert.x callback and {@link Future} based APIs.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface VertxBatchLoader<K, V> extends BatchLoaderWithContext<K, V> {

  /**
   * Create a new batch loader that works well with callback based APIs.
   * <p>
   * The provided {@code batchLoader} will be invoked with the following arguments:
   * <ul>
   * <li>the keys for the data objects that should be loaded</li>
   * <li>the {@link BatchLoaderEnvironment}</li>
   * <li>a {@link Promise} that the implementor must complete after the data objects are loaded</li>
   * </ul>
   */
  @GenIgnore
  static <K, V> VertxBatchLoader<K, V> create(TriConsumer<List<K>, BatchLoaderEnvironment, Promise<List<V>>> batchLoader) {
    return create(batchLoader, env -> Vertx.currentContext());
  }

  /**
   * Like {@link #create(TriConsumer)}, except the method uses the provided {@code contextProvider} instead of capturing the current one.
   */
  @GenIgnore
  static <K, V> VertxBatchLoader<K, V> create(TriConsumer<List<K>, BatchLoaderEnvironment, Promise<List<V>>> batchLoader, Function<BatchLoaderEnvironment, Context> contextProvider) {
    return new CallbackBatchLoaderImpl<>(batchLoader, contextProvider);
  }

  /**
   * Create a new batch loader that works well with {@link Future} based APIs.
   * <p>
   * The provided {@code batchLoader} will be invoked with the following arguments:
   * <ul>
   * <li>the keys for the data objects that should be loaded</li>
   * <li>the {@link BatchLoaderEnvironment}</li>
   * </ul>
   */
  @GenIgnore
  static <K, V> VertxBatchLoader<K, V> create(BiFunction<List<K>, BatchLoaderEnvironment, Future<List<V>>> batchLoader) {
    return create(batchLoader, env -> Vertx.currentContext());
  }

  /**
   * Like {@link #create(BiFunction)}, except the method uses the provided {@code contextProvider} instead of capturing the current one.
   */
  @GenIgnore
  static <K, V> VertxBatchLoader<K, V> create(BiFunction<List<K>, BatchLoaderEnvironment, Future<List<V>>> batchLoader, Function<BatchLoaderEnvironment, Context> contextProvider) {
    return new FutureBatchLoaderImpl<>(batchLoader, contextProvider);
  }
}
