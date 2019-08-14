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

package io.vertx.ext.web.client.impl.predicate;

import io.vertx.ext.web.client.predicate.ErrorConverter;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;

import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public class ErrorConverterImpl implements ErrorConverter {

  private final Function<ResponsePredicateResult, Throwable> converter;
  private final boolean needsBody;

  public ErrorConverterImpl(Function<ResponsePredicateResult, Throwable> converter, boolean needsBody) {
    this.converter = converter;
    this.needsBody = needsBody;
  }

  @Override
  public boolean requiresBody() {
    return needsBody;
  }

  @Override
  public Throwable apply(ResponsePredicateResult result) {
    return converter.apply(result);
  }
}
