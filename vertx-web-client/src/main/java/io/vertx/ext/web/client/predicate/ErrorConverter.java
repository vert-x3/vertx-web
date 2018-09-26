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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.client.impl.predicate.ErrorConverterImpl;

import java.util.function.Function;

/**
 * Converts a {@link ResponsePredicateResult} to a {@code Throwable} describing the error.
 *
 * @author Thomas Segismont
 */
@VertxGen
public interface ErrorConverter {

  /**
   * Creates an {@link ErrorConverter} ignoring the HTTP response body.
   *
   * @param converter a function creating a {@link Throwable} from a {@link ResponsePredicateResult}
   */
  static ErrorConverter withoutBody(Function<ResponsePredicateResult, Throwable> converter) {
    return new ErrorConverterImpl(converter, false);
  }

  /**
   * Creates an {@link ErrorConverter}.
   *
   * <p>The {@code converter} function will be invoked <em>after</em> the HTTP response body is received.
   *
   * @param converter a function creating a {@link Throwable} from a {@link ResponsePredicateResult}
   */
  static ErrorConverter withBody(Function<ResponsePredicateResult, Throwable> converter) {
    return new ErrorConverterImpl(converter, true);
  }
}
