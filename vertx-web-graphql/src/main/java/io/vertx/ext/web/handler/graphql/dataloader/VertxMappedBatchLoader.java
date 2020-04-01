/*
 * Copyright 2020 Red Hat, Inc.
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
import io.vertx.ext.web.handler.graphql.dataloader.impl.CallbackMappedBatchLoaderImpl;
import io.vertx.ext.web.handler.graphql.dataloader.impl.FutureMappedBatchLoaderImpl;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.MappedBatchLoaderWithContext;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A {@link MappedBatchLoaderWithContext} that works well with Vert.x callback and {@link Future} based APIs.
 *
 * @author Craig Day
 */
@VertxGen
public interface VertxMappedBatchLoader<K, V> extends MappedBatchLoaderWithContext<K, V> {

  /**
   * Create a new batch loader that works well with callback based APIs.
   * <p>
   * The provided {@code batchLoader} will be invoked with the following arguments:
   * <ul>
   * <li>the keys for the data objects that should be loaded</li>
   * <li>the {@link BatchLoaderEnvironment}</li>
   * <li>a {@link Promise} that the implementor must complete after the data objects are loaded</li>
   * </ul>
   * <p>
   * If called from a Vert.x thread, this method will capture the current {@link Context}.
   * The provided {@code batchLoader} will then be executed on this {@link Context}.
   */
  @GenIgnore
  static <K, V> VertxMappedBatchLoader<K, V> create(TriConsumer<Set<K>, BatchLoaderEnvironment, Promise<Map<K, V>>> batchLoader) {
    return create(batchLoader, Vertx.currentContext());
  }

  /**
   * Like {@link #create(TriConsumer)}, except the method uses the provided {@code context} instead of capturing the current one.
   */
  @GenIgnore
  static <K, V> VertxMappedBatchLoader<K, V> create(TriConsumer<Set<K>, BatchLoaderEnvironment, Promise<Map<K, V>>> batchLoader, Context context) {
    return new CallbackMappedBatchLoaderImpl<>(batchLoader, Vertx.currentContext());
  }

  /**
   * Create a new batch loader that works well with {@link Future} based APIs.
   * <p>
   * The provided {@code batchLoader} will be invoked with the following arguments:
   * <ul>
   * <li>the keys for the data objects that should be loaded</li>
   * <li>the {@link BatchLoaderEnvironment}</li>
   * </ul>
   * <p>
   * If called from a Vert.x thread, this method will capture the current {@link Context}.
   * The provided {@code batchLoader} will then be executed on this {@link Context}.
   */
  @GenIgnore
  static <K, V> VertxMappedBatchLoader<K, V> create(BiFunction<Set<K>, BatchLoaderEnvironment, Future<Map<K, V>>> batchLoader) {
    return create(batchLoader, Vertx.currentContext());
  }

  /**
   * Like {@link #create(BiFunction)}, except the method uses the provided {@code context} instead of capturing the current one.
   */
  @GenIgnore
  static <K, V> VertxMappedBatchLoader<K, V> create(BiFunction<Set<K>, BatchLoaderEnvironment, Future<Map<K, V>>> batchLoader, Context context) {
    return new FutureMappedBatchLoaderImpl<>(batchLoader, Vertx.currentContext());
  }
}
