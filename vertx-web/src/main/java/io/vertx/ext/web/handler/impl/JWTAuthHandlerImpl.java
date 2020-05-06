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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.List;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class JWTAuthHandlerImpl extends AuthorizationAuthHandler implements JWTAuthHandler {

  private final JsonObject options;

  public JWTAuthHandlerImpl(JWTAuth authProvider) {
    super(authProvider, Type.BEARER);
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

    parseAuthorization(context, false, parseAuthorization -> {
      if (parseAuthorization.failed()) {
        handler.handle(Future.failedFuture(parseAuthorization.cause()));
        return;
      }

      handler.handle(Future.succeededFuture(new JsonObject().put("jwt", parseAuthorization.result()).put("options", options)));
    });
  }

  @Override
  public String authenticateHeader(RoutingContext context) {
    if (realm != null && realm.length() > 0) {
      return "Bearer realm=\"" + realm + "\"";
    } else {
      return null;
    }
  }
}
