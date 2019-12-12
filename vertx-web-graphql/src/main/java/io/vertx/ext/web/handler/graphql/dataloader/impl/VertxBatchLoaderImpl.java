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

package io.vertx.ext.web.handler.graphql.dataloader.impl;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Promise;
import io.vertx.ext.web.handler.graphql.TriConsumer;
import io.vertx.ext.web.handler.graphql.dataloader.VertxBatchLoader;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A {@link BatchLoaderWithContext} that works well with Vert.x callback-based APIs.
 *
 * @author Thomas Segismont
 */
public class VertxBatchLoaderImpl<K, V> implements VertxBatchLoader<K, V> {

  final TriConsumer<List<K>, BatchLoaderEnvironment, Promise<List<V>>> batchLoader;

  /**
   * Create a new batch loader.
   * The provided function will be invoked with the following arguments:
   * <ul>
   * <li>the keys for the data objects that should be loaded</li>
   * <li>the {@link BatchLoaderEnvironment}</li>
   * <li>a future that the implementor must complete after the data objects are loaded</li>
   * </ul>
   */
  public VertxBatchLoaderImpl(TriConsumer<List<K>, BatchLoaderEnvironment, Promise<List<V>>> batchLoader) {
    this.batchLoader = batchLoader;
  }

  @Override
  public CompletionStage<List<V>> load(List<K> keys, BatchLoaderEnvironment environment) {
    CompletableFuture<List<V>> cf = new CompletableFuture<>();
    Promise<List<V>> promise = Promise.promise();
    promise.future().setHandler(ar -> {
      if (ar.succeeded()) {
        cf.complete(ar.result());
      } else {
        cf.completeExceptionally(ar.cause());
      }
    });
    batchLoader.accept(keys, environment, promise);
    return cf;
  }
}
