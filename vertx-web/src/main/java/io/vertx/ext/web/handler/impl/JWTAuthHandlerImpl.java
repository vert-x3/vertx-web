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

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.auth.jwt.AbstractJWTHandler;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.JWTAuthHandler;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class JWTAuthHandlerImpl extends AbstractJWTHandler<RoutingContext> implements JWTAuthHandler {

  public JWTAuthHandlerImpl(JWTAuth authProvider, String realm) {
    super(authProvider, Type.BEARER, realm);
  }

  private JWTAuthHandlerImpl(JWTAuthHandlerImpl base, List<String> scopes, String delimiter) {
    super(base.authProvider, scopes, delimiter, base.realm);
  }

  @Override
  public JWTAuthHandler withScope(String scope) {
    Objects.requireNonNull(scope, "scope cannot be null");
    List<String> updatedScopes = new ArrayList<>(this.scopes);
    updatedScopes.add(scope);
    return new JWTAuthHandlerImpl(this, updatedScopes, delimiter);
  }

  @Override
  public JWTAuthHandler withScopes(List<String> scopes) {
    Objects.requireNonNull(scopes, "scopes cannot be null");
    return new JWTAuthHandlerImpl(this, scopes, delimiter);
  }

  @Override
  public JWTAuthHandler scopeDelimiter(String delimiter) {
    Objects.requireNonNull(delimiter, "delimiter cannot be null");
    this.delimiter = delimiter;
    return this;
  }

  // TODO remove duplicated code from WebAuthenticationHandlerImpl
  /**
   * This method is protected so custom auth handlers can override the default error handling
   */
  protected void processException(RoutingContext ctx, Throwable exception) {
    if (exception != null) {
      if (exception instanceof HttpException) {
        final int statusCode = ((HttpException) exception).getStatusCode();
        final String payload = ((HttpException) exception).getPayload();

        switch (statusCode) {
        case 302:
          ctx.response()
            .putHeader(HttpHeaders.LOCATION, payload)
            .setStatusCode(302)
            .end("Redirecting to " + payload + ".");
          return;
        case 401:
          if (!"XMLHttpRequest".equals(ctx.request().getHeader("X-Requested-With"))) {
            setAuthenticateHeader(ctx);
          }
          ctx.fail(401, exception);
          return;
        default:
          ctx.fail(statusCode, exception);
          return;
        }
      }
    }

    // fallback 500
    ctx.fail(exception);
  }

  protected void fail(RoutingContext ctx, int code, String msg) {
    ctx.fail(code, new IllegalStateException(msg));
  }
}
