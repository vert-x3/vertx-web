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
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.impl.RoutingContextInternal;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class AuthenticationHandlerImpl<T extends AuthenticationProvider> implements AuthenticationHandlerInternal {

  static final HttpException UNAUTHORIZED = new HttpException(401);
  static final HttpException BAD_REQUEST = new HttpException(400);
  static final HttpException BAD_METHOD = new HttpException(405);

  protected final T authProvider;
  // signal the kind of Multi-Factor Authentication used by the handler
  protected final String mfa;

  public AuthenticationHandlerImpl(T authProvider) {
    this(authProvider, null);
  }

  public AuthenticationHandlerImpl(T authProvider, String mfa) {
    this.authProvider = authProvider;
    this.mfa = mfa;
  }

  @Override
  public void handle(RoutingContext ctx) {
    if (handlePreflight(ctx)) {
      return;
    }

    if (!((RoutingContextInternal) ctx).seenHandler(RoutingContextInternal.BODY_HANDLER)) {
      // pause the request
      ctx.request().pause();
    }

    User user = ctx.user();
    if (user != null) {
      if (mfa != null) {
        // if we're dealing with MFA, the user principal must include a matching mfa
        if (mfa.equals(user.get("mfa"))) {
          // proceed with the router
          resume(ctx);
          postAuthentication(ctx);
          return;
        }
      } else {
        // proceed with the router
        resume(ctx);
        postAuthentication(ctx);
        return;
      }
    }
    // perform the authentication
    authenticate(ctx, authN -> {
      if (authN.succeeded()) {
        User authenticated = authN.result();
        ctx.setUser(authenticated);
        Session session = ctx.session();
        if (session != null) {
          // the user has upgraded from unauthenticated to authenticated
          // session should be upgraded as recommended by owasp
          session.regenerateId();
        }
        // proceed with the router
        resume(ctx);
        postAuthentication(ctx);
      } else {
        // to allow further processing if needed
        Throwable cause = authN.cause();
        resume(ctx);
        processException(ctx, cause);
      }
    });
  }

  /**
   * This method is protected so custom auth handlers can override the default
   * error handling
   */
  protected void processException(RoutingContext ctx, Throwable exception) {
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

    // fallback 500
    ctx.fail(exception);
  }

  private boolean handlePreflight(RoutingContext ctx) {
    final HttpServerRequest request = ctx.request();
    // See: https://www.w3.org/TR/cors/#cross-origin-request-with-preflight-0
    // Preflight requests should not be subject to security due to the reason UAs will remove the Authorization header
    if (request.method() == HttpMethod.OPTIONS) {
      // check if there is a access control request header
      final String accessControlRequestHeader = ctx.request().getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
      if (accessControlRequestHeader != null) {
        // lookup for the Authorization header
        for (String ctrlReq : accessControlRequestHeader.split(",")) {
          if (ctrlReq.equalsIgnoreCase("Authorization")) {
            // this request has auth in access control, so we can allow preflighs without authentication
            ctx.next();
            return true;
          }
        }
      }
    }

    return false;
  }

  private void resume(RoutingContext ctx) {
    if (!((RoutingContextInternal) ctx).seenHandler(RoutingContextInternal.BODY_HANDLER)) {
      ctx.request().resume();
    }
  }
}
