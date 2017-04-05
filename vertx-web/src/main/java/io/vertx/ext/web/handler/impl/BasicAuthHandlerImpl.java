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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.auth.AuthProvider;

import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BasicAuthHandlerImpl extends AuthHandlerImpl {

  public BasicAuthHandlerImpl(AuthProvider authProvider, String realm) {
    super(authProvider, realm);
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    HttpServerRequest request = context.request();
    String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);

    if (authorization == null) {
      handler.handle(Future.failedFuture(UNAUTHORIZED));
      return;
    }

    String suser;
    String spass;

    try {
      String[] parts = authorization.split(" ");
      if (parts.length != 2) {
        handler.handle(Future.failedFuture(BAD_REQUEST));
        return;
      }

      if (!"Basic".equals(parts[0])) {
        handler.handle(Future.failedFuture(BAD_REQUEST));
        return;
      }

      // decode the payload
      String decoded = new String(Base64.getDecoder().decode(parts[1]));

      int colonIdx = decoded.indexOf(":");
      if (colonIdx != -1) {
        suser = decoded.substring(0, colonIdx);
        spass = decoded.substring(colonIdx + 1);
      } else {
        suser = decoded;
        spass = null;
      }
    } catch (RuntimeException e) {
      // IllegalArgumentException includes PatternSyntaxException
      context.fail(e);
      return;
    }

    handler.handle(Future.succeededFuture(new JsonObject().put("username", suser).put("password", spass)));
  }

  @Override
  protected String authenticateHeader(RoutingContext context) {
    return "Basic realm=\"" + realm + "\"";
  }
}
