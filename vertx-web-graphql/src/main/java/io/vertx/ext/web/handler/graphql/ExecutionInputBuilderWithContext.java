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
 * Holds a {@link ExecutionInput.Builder} with a contextual object.
 *
 * @param <C> the contextual object type
 */
@VertxGen
public interface ExecutionInputBuilderWithContext<C> {

  /**
   * @return the GraphQL execution input builder
   */
  @GenIgnore(PERMITTED_TYPE)
  ExecutionInput.Builder builder();

  /**
   * @return the contextual object
   */
  C context();
}
