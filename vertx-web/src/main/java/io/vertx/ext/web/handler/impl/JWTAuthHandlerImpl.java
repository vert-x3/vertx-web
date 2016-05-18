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

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class JWTAuthHandlerImpl extends AuthHandlerImpl implements JWTAuthHandler {

  private static final Logger log = LoggerFactory.getLogger(JWTAuthHandlerImpl.class);

  private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

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
  public void handle(RoutingContext context) {
    User user = context.user();
    if (user != null) {
      // Already authenticated in, just authorise
      authorise(user, context);
    } else {
      final HttpServerRequest request = context.request();

      String token = null;

      if (request.method() == HttpMethod.OPTIONS && request.headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS) != null) {
        for (String ctrlReq : request.headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS).split(",")) {
          if (ctrlReq.equalsIgnoreCase("authorization")) {
            // this request has auth in access control
            context.next();
            return;
          }
        }
      }

      if (skip != null && skip.contains(request.path())) {
        context.next();
        return;
      }

      final String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);

      if (authorization != null) {
        String[] parts = authorization.split(" ");
        if (parts.length == 2) {
          final String scheme = parts[0],
              credentials = parts[1];

          if (BEARER.matcher(scheme).matches()) {
            token = credentials;
          } else {
            log.warn("Format is Authorization: Bearer [token]");
            context.fail(401);
            return;
          }
        } else {
          log.warn("Format is Authorization: Bearer [token]");
          context.fail(401);
          return;
        }
      } else {
        log.warn("No Authorization header was found");
        context.fail(401);
        return;
      }

      JsonObject authInfo = new JsonObject().put("jwt", token).put("options", options);

      authProvider.authenticate(authInfo, res -> {
        if (res.succeeded()) {
          final User user2 = res.result();
          context.setUser(user2);
          authorise(user2, context);
        } else {
          log.warn("JWT decode failure");
          context.fail(401);
        }
      });
    }
  }
}
