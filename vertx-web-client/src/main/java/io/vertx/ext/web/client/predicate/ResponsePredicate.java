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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.predicate.ResponsePredicateImpl;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@VertxGen
public interface ResponsePredicate {

  static ResponsePredicate status(int statusCode) {
    return status(statusCode, statusCode + 1);
  }

  static ResponsePredicate statusSuccess() {
    return status(200, 300);
  }

  /**
   * Return a predicate asserting that the status response code is in the {@code [min,max[} range.
   *
   * @param min the lower (inclusive) accepted status code
   * @param max the highest (exclusive) accepted status code
   * @return the expectation
   */
  static ResponsePredicate status(int min, int max) {
    return create(response -> {
      int sc = response.statusCode();
      if (sc >= min && sc < max) {
        return ResponsePredicateResult.success();
      }
      if (max - min == 1) {
        return ResponsePredicateResult.failure("Response status code " + sc + " is not equal to " + min);
      }
      return ResponsePredicateResult.failure("Response status code " + sc + " is not between " + min + " and " + max);
    });
  }

  static ResponsePredicate contentTypeJson() {
    return contentType("application/json");
  }

  /**
   * Create a predicate validating the response has a {@code content-type} header matching
   * the {@code mimeType}.
   *
   * @param mimeType the mime type
   * @return the predicate
   */
  static ResponsePredicate contentType(String mimeType) {
    return ResponsePredicate.contentType(Collections.singletonList(mimeType));
  }

  /**
   * Create a predicate validating the response has a {@code content-type} header matching
   * one of the {@code mimeTypes}.
   *
   * @param mimeTypes the list of mime types
   * @return the predicate
   */
  static ResponsePredicate contentType(List<String> mimeTypes) {
    return create(response -> {
      String contentType = response.getHeader(HttpHeaders.CONTENT_TYPE.toString());
      if (contentType == null) {
        return ResponsePredicateResult.failure("Missing response content type");
      }
      for (String mimeType : mimeTypes) {
        if (mimeType.equals(contentType)) {
          return ResponsePredicateResult.success();
        }
      }
      StringBuilder sb = new StringBuilder("Expect content type ").append(contentType).append(" to be one of ");
      boolean first = true;
      for (String mimeType : mimeTypes) {
        if (!first) {
          sb.append(", ");
        }
        first = false;
        sb.append(mimeType);
      }
      return ResponsePredicateResult.failure(sb.toString());
    });
  }

  static ResponsePredicate create(Function<HttpResponse<Void>, ResponsePredicateResult> test) {
    return new ResponsePredicateImpl(test);
  }

  static ResponsePredicate create(Function<HttpResponse<Void>, ResponsePredicateResult> test, ErrorConverter errorConverter) {
    return new ResponsePredicateImpl(test, errorConverter);
  }

  /**
   * Map the http response to a {@code Throwable} describing the error.
   *
   * The default implementation returns a {@code NoStackTraceThrowable} with a vanilla message.
   *
   * @param errorConverter
   */
  @Fluent
  ResponsePredicate errorConverter(ErrorConverter errorConverter);
}
