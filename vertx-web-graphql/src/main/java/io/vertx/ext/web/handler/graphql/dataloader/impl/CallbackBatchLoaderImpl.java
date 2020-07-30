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

import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.ext.web.handler.graphql.TriConsumer;
import io.vertx.ext.web.handler.graphql.dataloader.VertxBatchLoader;
import org.dataloader.BatchLoaderEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public class CallbackBatchLoaderImpl<K, V> implements VertxBatchLoader<K, V> {

  private final TriConsumer<List<K>, BatchLoaderEnvironment, Promise<List<V>>> batchLoader;
  private final Function<BatchLoaderEnvironment, Context> contextProvider;

  public CallbackBatchLoaderImpl(
    TriConsumer<List<K>, BatchLoaderEnvironment, Promise<List<V>>> batchLoader,
    Function<BatchLoaderEnvironment, Context> contextProvider
  ) {
    this.batchLoader = Objects.requireNonNull(batchLoader, "batchLoader is null");
    this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider is null");
  }

  @Override
  public CompletionStage<List<V>> load(List<K> keys, BatchLoaderEnvironment env) {
    ContextInternal context = (ContextInternal) contextProvider.apply(env);
    Promise<List<V>> promise;
    if (context == null) {
      promise = Promise.promise();
      invokeBatchLoader(keys, env, promise);
    } else {
      promise = context.promise();
      context.runOnContext(v -> invokeBatchLoader(keys, env, promise));
    }
    return promise.future().toCompletionStage();
  }

  private void invokeBatchLoader(List<K> keys, BatchLoaderEnvironment env, Promise<List<V>> promise) {
    batchLoader.accept(keys, env, promise);
  }
}
