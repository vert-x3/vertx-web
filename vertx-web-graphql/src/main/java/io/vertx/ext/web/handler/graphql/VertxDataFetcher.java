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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author Thomas Segismont
 */
@VertxGen
public interface VertxDataFetcher<T> extends DataFetcher<CompletableFuture<T>> {

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static <T> VertxDataFetcher<T> create(BiConsumer<Future<T>, DataFetchingEnvironment> dataFetcher) {
    CompletableFuture<T> cf = new CompletableFuture<>();
    Future<T> future = Future.future();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        cf.complete(ar.result());
      } else {
        cf.completeExceptionally(ar.cause());
      }
    });
    return environment -> {
      dataFetcher.accept(future, environment);
      return cf;
    };
  }
}
