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

package io.vertx.ext.apex.handler.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.auth.AuthService;

import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BasicAuthHandlerImpl extends AuthHandlerImpl {

  private final String realm;

  public BasicAuthHandlerImpl(AuthService authService, String realm) {
    super(authService);
    this.realm = realm;
  }

  @Override
  public void handle(RoutingContext context) {

    Session session = context.session();
    if (session == null) {
      context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
    } else {
      if (session.isLoggedIn()) {
        // Already logged in, just authorise
        authorise(context);
      } else {
        HttpServerRequest request = context.request();
        String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
          handle401(context);
        } else {
          String user;
          String pass;
          String scheme;

          try {
            String[] parts = authorization.split(" ");
            scheme = parts[0];
            String[] credentials = new String(Base64.getDecoder().decode(parts[1])).split(":");
            user = credentials[0];
            // when the header is: "user:"
            pass = credentials.length > 1 ? credentials[1] : null;
          } catch (ArrayIndexOutOfBoundsException e) {
            handle401(context);
            return;
          } catch (IllegalArgumentException | NullPointerException e) {
            // IllegalArgumentException includes PatternSyntaxException
            context.fail(e);
            return;
          }

          if (!"Basic".equals(scheme)) {
            context.fail(400);
          } else {
            authService.login(new JsonObject().put("username", user).put("password", pass), res -> {
              if (res.succeeded()) {
                String loginID = res.result();
                session.setLoginID(loginID);
                session.setAuthService(authService);
                authorise(context);
              } else {
                handle401(context);
              }
            });
          }
        }
      }
    }
  }

  private void handle401(RoutingContext context) {
    context.response().putHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    context.fail(401);
  }
}
