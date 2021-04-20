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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class JWTAuthHandlerImpl extends HTTPAuthorizationHandler<JWTAuth> implements JWTAuthHandler, ScopedAuthentication<JWTAuthHandler> {

  private final List<String> scopes;
  private final String delimiter;

  public JWTAuthHandlerImpl(JWTAuth authProvider, String realm) {
    super(authProvider, Type.BEARER, realm);
    scopes = new ArrayList<>();
    this.delimiter  = " ";
  }

  private JWTAuthHandlerImpl(JWTAuthHandlerImpl base, List<String> scopes, String delimiter) {
    super(base.authProvider, Type.BEARER, base.realm);
    this.scopes = scopes;
    this.delimiter = delimiter;
  }

  @Override
  public void authenticate(RoutingContext context, Handler<AsyncResult<User>> handler) {

    parseAuthorization(context, parseAuthorization -> {
      if (parseAuthorization.failed()) {
        handler.handle(Future.failedFuture(parseAuthorization.cause()));
        return;
      }

      String token = parseAuthorization.result();
      int segments = 0;
      for (int i = 0; i < token.length(); i++) {
        char c = token.charAt(i);
        if (c == '.') {
          if (++segments == 3) {
            handler.handle(Future.failedFuture(new HttpException(400, "Too many segments in token")));
            return;
          }
          continue;
        }
        if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
          continue;
        }
        // invalid character
        handler.handle(Future.failedFuture(new HttpException(400, "Invalid character in token: " + (int) c)));
        return;
      }

      authProvider.authenticate(new TokenCredentials(token), authn -> {
        if (authn.failed()) {
          handler.handle(Future.failedFuture(new HttpException(401, authn.cause())));
        } else {
          handler.handle(authn);
        }
      });
    });
  }

  @Override
  public JWTAuthHandler withScope(String scope) {
    List<String> updatedScopes = new ArrayList<>(this.scopes);
    updatedScopes.add(scope);
    return new JWTAuthHandlerImpl(this, updatedScopes, delimiter);
  }

  @Override
  public JWTAuthHandler withScopes(List<String> scopes) {
    return new JWTAuthHandlerImpl(this, scopes, delimiter);
  }

  @Override
  public JWTAuthHandler scopeDelimiter(String delimeter) {
    return new JWTAuthHandlerImpl(this, scopes, delimeter);
  }

  /**
   * The default behavior for post-authentication
   */
  @Override
  public void postAuthentication(RoutingContext ctx) {
    // the user is authenticated, however the user may not have all the required scopes
    if (scopes.size() > 0) {
      final JsonObject jwt = ctx.user().get("accessToken");
      if (jwt == null) {
        ctx.fail(403, new IllegalStateException("Invalid JWT: null"));
        return;
      }

      if(jwt.getValue("scope") == null) {
        ctx.fail(403, new IllegalStateException("Invalid JWT: scope claim is required"));
        return;
      }

      List<?> target;
      if (jwt.getValue("scope") instanceof String) {
        target =
          Stream.of(jwt.getString("scope")
            .split(delimiter))
            .collect(Collectors.toList());
      } else {
        target = jwt.getJsonArray("scope").getList();
      }

      if (target != null) {
        for (String scope : scopes) {
          if (!target.contains(scope)) {
            ctx.fail(403, new IllegalStateException("JWT scopes != handler scopes"));
            return;
          }
        }
      }
    }
    ctx.next();
  }
}
