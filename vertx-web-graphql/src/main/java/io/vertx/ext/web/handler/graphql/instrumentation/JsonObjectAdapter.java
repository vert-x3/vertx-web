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

import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.*;
import io.vertx.core.json.JsonObject;

import java.util.function.Supplier;

/**
 * Instrument {@link PropertyDataFetcher} so that {@link JsonObject} fields can be extracted.
 */
public class JsonObjectAdapter extends SimplePerformantInstrumentation {

  @Override
  public DataFetcher<?> instrumentDataFetcher(DataFetcher<?> dataFetcher, InstrumentationFieldFetchParameters parameters, InstrumentationState state) {
    if (dataFetcher instanceof SingletonPropertyDataFetcher) {
      return JsonObjectCompatible.FOR_SINGLETON_FETCHER;
    }
    if (dataFetcher instanceof PropertyDataFetcher) {
      PropertyDataFetcher<?> fetcher = (PropertyDataFetcher<?>) dataFetcher;
      return new JsonObjectCompatible(fetcher);
    }
    return dataFetcher;
  }

  private static class JsonObjectCompatible implements LightDataFetcher<Object> {

    static final JsonObjectCompatible FOR_SINGLETON_FETCHER = new JsonObjectCompatible(SingletonPropertyDataFetcher.singleton());

    final LightDataFetcher<?> delegate;

    JsonObjectCompatible(LightDataFetcher<?> delegate) {
      this.delegate = delegate;
    }

    @Override
    public Object get(GraphQLFieldDefinition fieldDefinition, Object sourceObject, Supplier<DataFetchingEnvironment> environmentSupplier) throws Exception {
      if (sourceObject instanceof JsonObject) {
        JsonObject jsonObject = (JsonObject) sourceObject;
        return delegate.get(fieldDefinition, jsonObject.getMap(), environmentSupplier);
      }
      return delegate.get(fieldDefinition, sourceObject, environmentSupplier);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
      return get(environment.getFieldDefinition(), environment.getSource(), () -> environment);
    }
  }
}
