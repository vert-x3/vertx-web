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

import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.predicate.ErrorConverter;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;

import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public class ResponsePredicateImpl implements ResponsePredicate {

  private final Function<HttpResponse<Void>, ResponsePredicateResult> test;
  private ErrorConverter errorConverter;

  public ResponsePredicateImpl(Function<HttpResponse<Void>, ResponsePredicateResult> test) {
    this.test = test;
    errorConverter = ResponsePredicateImpl::convert;
  }

  private static Throwable convert(ResponsePredicateResult result) {
    String message = result.message();
    return message == null ? new NoStackTraceThrowable("Invalid http response") : new NoStackTraceThrowable(message);
  }

  public ResponsePredicateImpl(Function<HttpResponse<Void>, ResponsePredicateResult> test, ErrorConverter errorConverter) {
    this.test = test;
    this.errorConverter = errorConverter;
  }

  public Function<HttpResponse<Void>, ResponsePredicateResult> getTest() {
    return test;
  }

  @Override
  public ResponsePredicateImpl errorConverter(ErrorConverter errorConverter) {
    return new ResponsePredicateImpl(test, errorConverter);
  }

  public ErrorConverter getErrorConverter() {
    return errorConverter;
  }
}
