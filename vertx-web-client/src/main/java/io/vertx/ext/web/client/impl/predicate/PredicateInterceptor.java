/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.client.impl.predicate;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import io.vertx.ext.web.client.impl.HttpResponseImpl;
import io.vertx.ext.web.client.predicate.ErrorConverter;
import io.vertx.ext.web.client.predicate.ResponsePredicate;

import java.util.ArrayList;
import java.util.List;

public class PredicateInterceptor implements Handler<HttpContext<?>> {

  @Override
  public void handle(HttpContext<?> httpContext) {

    if (httpContext.phase() == ClientPhase.RECEIVE_RESPONSE) {

      // Run expectations
      HttpRequestImpl request = (HttpRequestImpl) httpContext.request();
      HttpClientResponse resp = httpContext.clientResponse();
      List<ResponsePredicate> expectations = request.expectations;
      if (expectations != null) {
        for (ResponsePredicate expectation : expectations) {
          ResponsePredicateResultImpl predicateResult;
          try {
            predicateResult = (ResponsePredicateResultImpl) expectation.apply(responseCopy(resp, null));
          } catch (Exception e) {
            httpContext.fail(e);
            return;
          }
          if (!predicateResult.succeeded()) {
            ErrorConverter errorConverter = expectation.errorConverter();
            if (!errorConverter.requiresBody()) {
              failOnPredicate(httpContext, errorConverter, predicateResult);
            } else {
              resp.bodyHandler(buffer -> {
                predicateResult.setHttpResponse(responseCopy(resp, buffer));
                failOnPredicate(httpContext, errorConverter, predicateResult);
              });
              resp.resume();
            }
            return;
          }
        }
      }
    }

    httpContext.next();
  }

  private <B> HttpResponseImpl<B> responseCopy(HttpClientResponse resp, B value) {
    return new HttpResponseImpl<>(
      resp.version(),
      resp.statusCode(),
      resp.statusMessage(),
      MultiMap.caseInsensitiveMultiMap().addAll(resp.headers()),
      null,
      new ArrayList<>(resp.cookies()),
      value);
  }

  private void failOnPredicate(HttpContext<?> ctx, ErrorConverter converter, ResponsePredicateResultImpl predicateResult) {
    Throwable result;
    try {
      result = converter.apply(predicateResult);
    } catch (Exception e) {
      result = e;
    }
    if (result != null) {
      ctx.fail(result);
    } else {
      ctx.fail(new NoStackTraceThrowable("Invalid HTTP response"));
    }
  }
}
