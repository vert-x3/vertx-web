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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.MethodOverrideHandler;

/**
 * # MethodOverrideParser
 * <p>
 * Parse method override on the request header
 * <p>
 * BEWARE: This might become an attack vector for ill-intentioned people. Even worse if you decide not to use our
 * safe-downgrade policy. You better know what you are doing!
 *
 * @author <a href="mailto:victorqrsilva@gmail.com">Victor Quezado</a>
 */
public class MethodOverrideHandlerImpl implements MethodOverrideHandler {
  private boolean useSafeDowngrade;

  private interface HttpMethodTraits {
    static boolean isIdempotent(HttpMethod method) {
      switch (method.name()) {
        case "GET":
        case "HEAD":
        case "PUT":
        case "PATCH":
        case "DELETE":
          return true;
        default:
          return false;
      }
    }

    static boolean isSafe(HttpMethod method) {
      switch (method.name()) {
        case "GET":
        case "HEAD":
          return true;
        default:
          return false;
      }
    }
  }

  public MethodOverrideHandlerImpl() {
    this(true);
  }

  public MethodOverrideHandlerImpl(boolean useSafeDowngrade) {
    this.useSafeDowngrade = useSafeDowngrade;
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();

    HttpMethod from = request.method();
    HttpMethod to = methodFromHeader(request);

    if (to != null && from != to && canOverride(from, to)) {
      context.reroute(to, request.path());
    } else {
      context.next();
    }
  }

  private HttpMethod methodFromHeader(HttpServerRequest request) {
    String method = request.headers().get("X-HTTP-METHOD-OVERRIDE");
    return (method != null)
      ? HttpMethod.valueOf(method)
      : null;
  }

  private boolean canOverride(HttpMethod from, HttpMethod to) {
    if (!this.useSafeDowngrade) {
      return true;
    }

    return HttpMethodTraits.isIdempotent(to) ||
          (HttpMethodTraits.isSafe(to) && !HttpMethodTraits.isIdempotent(from)) ||
          !HttpMethodTraits.isSafe(from);
  }
}
