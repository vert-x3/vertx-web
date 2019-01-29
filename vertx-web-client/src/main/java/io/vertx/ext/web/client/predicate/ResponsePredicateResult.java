/*
 * Copyright 2018 Red Hat, Inc.
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

package io.vertx.ext.web.client.predicate;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.predicate.ResponsePredicateResultImpl;

import java.util.function.Function;

/**
 * Represents the outcome of a {@link ResponsePredicate} applied to an {@link HttpResponse}.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface ResponsePredicateResult {

  /**
   * @return a successful result
   */
  static ResponsePredicateResult success() {
    return ResponsePredicateResultImpl.SUCCESS;
  }

  /**
   * Creates a failed result.
   *
   * @param message the failure description
   */
  static ResponsePredicateResult failure(String message) {
    return new ResponsePredicateResultImpl(false, message);
  }

  /**
   * Whether the result is a success or failure.
   *
   * @return {@code true} if the {@link ResponsePredicate} was applied successfully, false otherwise
   */
  boolean succeeded();

  /**
   * The failure message. May be {@code null}.
   */
  @Nullable String message();

  /**
   * The {@link HttpResponse} which has been tested.
   *
   * @return {@code null} after the result has been created, or the tested response for converters created
   *         with {@link ErrorConverter#createFullBody(Function)}
   */
  @Nullable HttpResponse<Buffer> response();
}
