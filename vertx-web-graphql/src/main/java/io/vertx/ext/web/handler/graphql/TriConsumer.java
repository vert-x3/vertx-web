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

package io.vertx.ext.web.handler.graphql;

/**
 * Like {@link java.util.function.BiConsumer} but accepts three arguments.
 *
 * @deprecated only useful in dataloader package which is now deprecated
 */
@FunctionalInterface
@Deprecated
public interface TriConsumer<T, U, V> {

  /**
   * Execute with the provided arguments.
   */
  void accept(T t, U u, V v);
}
