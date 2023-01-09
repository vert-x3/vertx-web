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

package io.vertx.ext.web.handler.graphql.instrumentation.impl;

import graphql.TrivialDataFetcher;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Instrument data fetchers so that results are automatically converted to {@link java.util.concurrent.CompletionStage}.
 */
public class ToCompletionStage<T> extends SimpleInstrumentation {

  private final Class<T> targetType;
  private final Function<T, CompletionStage<?>> converter;

  public ToCompletionStage(Class<T> targetType, Function<T, CompletionStage<?>> converter) {
    this.targetType = Objects.requireNonNull(targetType, "targetType is null");
    this.converter = Objects.requireNonNull(converter, "converter is null");
  }

  @Override
  public DataFetcher<?> instrumentDataFetcher(DataFetcher<?> dataFetcher, InstrumentationFieldFetchParameters parameters, InstrumentationState state) {
    if (dataFetcher instanceof TrivialDataFetcher) {
      return dataFetcher;
    }
    if (dataFetcher instanceof InstrumentedDataFetcher) {
      // Do not instrument data fetchers already instrumented by other Vert.x projects (e.g. Vert.x RxJava)
      return dataFetcher;
    }
    parameters.getEnvironment().getGraphQlContext().put(InstrumentedDataFetcher.class, dataFetcher);
    return new InstrumentedDataFetcher<>(targetType, converter, dataFetcher);
  }

  private static class InstrumentedDataFetcher<U> implements DataFetcher<Object> {

    final Class<U> targetType;
    final Function<U, CompletionStage<?>> converter;
    final DataFetcher<?> originalDataFetcher;

    InstrumentedDataFetcher(Class<U> targetType, Function<U, CompletionStage<?>> converter, DataFetcher<?> originalDataFetcher) {
      this.targetType = targetType;
      this.converter = converter;
      this.originalDataFetcher = originalDataFetcher;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
      Object o = originalDataFetcher.get(environment);
      if (targetType.isInstance(o)) {
        return converter.apply(targetType.cast(o));
      }
      return o;
    }
  }
}
