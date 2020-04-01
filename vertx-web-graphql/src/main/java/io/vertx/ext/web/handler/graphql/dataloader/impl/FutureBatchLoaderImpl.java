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

package io.vertx.ext.web.handler.graphql.dataloader.impl;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.ext.web.handler.graphql.dataloader.VertxBatchLoader;
import org.dataloader.BatchLoaderEnvironment;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

/**
 * @author Thomas Segismont
 */
public class FutureBatchLoaderImpl<K, V> implements VertxBatchLoader<K, V> {

  private final BiFunction<List<K>, BatchLoaderEnvironment, Future<List<V>>> batchLoader;
  private final ContextInternal context;

  public FutureBatchLoaderImpl(BiFunction<List<K>, BatchLoaderEnvironment, Future<List<V>>> batchLoader, Context context) {
    this.batchLoader = batchLoader;
    this.context = (ContextInternal) context;
  }

  @Override
  public CompletionStage<List<V>> load(List<K> keys, BatchLoaderEnvironment env) {
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
    batchLoader.apply(keys, env).onComplete(promise);
  }
}
