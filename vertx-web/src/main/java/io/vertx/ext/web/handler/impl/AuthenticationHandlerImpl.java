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
import io.vertx.ext.web.handler.AuthenticationHandler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class AuthenticationHandlerImpl<T extends AuthenticationProvider> implements AuthenticationHandler {

  static final String AUTH_PROVIDER_CONTEXT_KEY = "io.vertx.ext.web.handler.AuthenticationHandler.provider";

  static final HttpStatusException UNAUTHORIZED = new HttpStatusException(401);
  static final HttpStatusException BAD_REQUEST = new HttpStatusException(400);
  static final HttpStatusException BAD_METHOD = new HttpStatusException(405);

  protected final String realm;
  protected final T authProvider;

  public AuthenticationHandlerImpl(T authProvider) {
    this(authProvider, null);
  }

  public AuthenticationHandlerImpl(T authProvider, String realm) {
    this.authProvider = authProvider;
    this.realm = realm == null ? null : realm
      // escape quotes
      .replaceAll("\"", "\\\"");

    if (this.realm != null &&
      (this.realm.indexOf('\r') != -1 || this.realm.indexOf('\n') != -1)) {
      throw new IllegalArgumentException("Not allowed [\\r|\\n] characters detected on realm name");
    }
  }

  @Override
  public void handle(RoutingContext ctx) {

    if (handlePreflight(ctx)) {
      return;
    }

    User user = ctx.user();
    if (user != null) {
      // proceed with the router
      postAuthentication(ctx);
      return;
    }
    // parse the request in order to extract the credentials object
    parseCredentials(ctx, res -> {
      if (res.failed()) {
        processException(ctx, res.cause());
        return;
      }
      // proceed to authN
      getAuthProvider(ctx).authenticate(res.result(), authN -> {
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
          postAuthentication(ctx);
        } else {
          String header = authenticateHeader(ctx);
          if (header != null) {
            ctx.response()
              .putHeader("WWW-Authenticate", header);
          }
          // to allow further processing if needed
          if (authN.cause() instanceof HttpStatusException) {
            processException(ctx, authN.cause());
          } else {
            processException(ctx, new HttpStatusException(401, authN.cause()));
          }
        }
      });
    });
  }

  /**
   * This method is protected so custom auth handlers can override the default
   * error handling
   */
  protected void processException(RoutingContext ctx, Throwable exception) {

    if (exception != null) {
      if (exception instanceof HttpStatusException) {
        final int statusCode = ((HttpStatusException) exception).getStatusCode();
        final String payload = ((HttpStatusException) exception).getPayload();

        switch (statusCode) {
          case 302:
            ctx.response()
              .putHeader(HttpHeaders.LOCATION, payload)
              .setStatusCode(302)
              .end("Redirecting to " + payload + ".");
            return;
          case 401:
            String header = authenticateHeader(ctx);
            if (header != null) {
              ctx.response()
                .putHeader("WWW-Authenticate", header);
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

  protected AuthenticationProvider getAuthProvider(RoutingContext ctx) {
    try {
      AuthenticationProvider provider = ctx.get(AUTH_PROVIDER_CONTEXT_KEY);
      if (provider != null) {
        // we're overruling the configured one for this request
        return provider;
      }
    } catch (RuntimeException e) {
      // bad type, ignore and return default
    }

    return authProvider;
  }
}
