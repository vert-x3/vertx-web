/*
 * Copyright 2021 Red Hat, Inc.
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

import graphql.ExecutionInput;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

import static io.vertx.codegen.annotations.GenIgnore.PERMITTED_TYPE;

/**
 * A callback invoked by GraphQL handlers before executing a GraphQL query.
 */
@VertxGen
@FunctionalInterface
public interface ExecutionInputConfig<T> {

  @GenIgnore(PERMITTED_TYPE)
  void configure(T t, ExecutionInput.Builder builder);
}
