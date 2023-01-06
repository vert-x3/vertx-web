/*
 * Copyright 2023 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql.instrumentation;

import io.vertx.core.Future;
import io.vertx.ext.web.handler.graphql.instrumentation.impl.ToCompletionStageImpl;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Instrument data fetchers so that {@link Future} results are automatically converted to {@link java.util.concurrent.CompletionStage}.
 */
@SuppressWarnings("rawtypes")
public class VertxFutureAdapter extends ToCompletionStageImpl<Future> {

  private VertxFutureAdapter(Class<Future> targetType, Function<Future, CompletionStage<?>> converter) {
    super(targetType, converter);
  }

  public static VertxFutureAdapter create() {
    return new VertxFutureAdapter(Future.class, Future::toCompletionStage);
  }
}
