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

import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.predicate.ErrorConverter;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;

import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public class ResponsePredicateImpl implements ResponsePredicate {

  private final Function<HttpResponse<Void>, ResponsePredicateResult> predicate;
  private final ErrorConverter errorConverter;

  public ResponsePredicateImpl(Function<HttpResponse<Void>, ResponsePredicateResult> predicate, ErrorConverter errorConverter) {
    this.predicate = predicate;
    this.errorConverter = errorConverter;
  }

  @Override
  public ResponsePredicateResult apply(HttpResponse<Void> response) {
    return predicate.apply(response);
  }

  @Override
  public ErrorConverter errorConverter() {
    return errorConverter;
  }

}
