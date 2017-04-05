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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.List;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class JWTAuthHandlerImpl extends AuthHandlerImpl implements JWTAuthHandler {

  private final String skip;
  private final JsonObject options;

  public JWTAuthHandlerImpl(JWTAuth authProvider, String skip) {
    super(authProvider);
    this.skip = skip;
    options = new JsonObject();
  }

  @Override
  public JWTAuthHandler setAudience(List<String> audience) {
    options.put("audience", new JsonArray(audience));
    return this;
  }

  @Override
  public JWTAuthHandler setIssuer(String issuer) {
    options.put("issuer", issuer);
    return this;
  }

  @Override
  public JWTAuthHandler setIgnoreExpiration(boolean ignoreExpiration) {
    options.put("ignoreExpiration", ignoreExpiration);
    return this;
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    final HttpServerRequest request = context.request();

    final String token;

    if (skip != null && context.normalisedPath().startsWith(skip)) {
      context.next();
      return;
    }

    final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorization == null) {
      handler.handle(Future.failedFuture(UNAUTHORIZED));
      return;
    }

    try {
      int idx = authorization.indexOf(' ');

      if (idx <= 0) {
        handler.handle(Future.failedFuture(BAD_REQUEST));
        return;
      }

      if (!"Bearer".equalsIgnoreCase(authorization.substring(0, idx))) {
        handler.handle(Future.failedFuture(BAD_REQUEST));
        return;
      }

      token = authorization.substring(idx + 1);

    } catch (RuntimeException e) {
      handler.handle(Future.failedFuture(e));
      return;
    }

    handler.handle(Future.succeededFuture(new JsonObject().put("jwt", token).put("options", options)));
  }

  @Override
  protected String authenticateHeader(RoutingContext context) {
    return "Bearer";
  }
}
