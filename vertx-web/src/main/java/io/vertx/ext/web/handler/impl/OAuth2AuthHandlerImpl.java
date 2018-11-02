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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static io.vertx.ext.auth.oauth2.OAuth2FlowType.AUTH_CODE;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class OAuth2AuthHandlerImpl extends AuthorizationAuthHandler implements OAuth2AuthHandler {

  private static final Logger log = LoggerFactory.getLogger(OAuth2AuthHandlerImpl.class);

  /**
   * This is a verification step, it can abort the instantiation by
   * throwing a RuntimeException
   *
   * @param provider a auth provider
   * @return the provider if valid
   */
  private static AuthProvider verifyProvider(AuthProvider provider) {
    if (provider instanceof OAuth2Auth) {
      if (((OAuth2Auth) provider).getFlowType() != AUTH_CODE) {
        throw new IllegalArgumentException("OAuth2Auth + Bearer Auth requires OAuth2 AUTH_CODE flow");
      }
    }

    return provider;
  }

  private final String host;
  private final String callbackPath;
  private final Set<String> scopes = new HashSet<>();

  private Route callback;
  private JsonObject extraParams;
  // explicit signal that tokens are handled as bearer only (meaning, no backend server known)
  private boolean bearerOnly = true;

  public OAuth2AuthHandlerImpl(OAuth2Auth authProvider, String callbackURL) {
    super(verifyProvider(authProvider), Type.BEARER);

    try {
      if (callbackURL != null) {
        final URL url = new URL(callbackURL);
        this.host = url.getProtocol() + "://" + url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort());
        this.callbackPath = url.getPath();
      } else {
        this.host = null;
        this.callbackPath = null;
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AuthHandler addAuthority(String authority) {
    scopes.add(authority);
    return this;
  }

  @Override
  public AuthHandler addAuthorities(Set<String> authorities) {
    this.scopes.addAll(authorities);
    return this;
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    // when the handler is working as bearer only, then the `Authorization` header is required
    parseAuthorization(context, !bearerOnly, parseAuthorization -> {
      if (parseAuthorization.failed()) {
        handler.handle(Future.failedFuture(parseAuthorization.cause()));
        return;
      }
      // Authorization header can be null when in bearerOnly mode
      final String token = parseAuthorization.result();

      if (token == null) {
        // redirect request to the oauth2 server as we know nothing about this request
        if (callback == null) {
          // it's a failure both cases but the cause is not the same
          handler.handle(Future.failedFuture("callback route is not configured."));
          return;
        }
        // when this handle is mounted as a catch all, the callback route must be configured before,
        // as it would shade the callback route. When a request matches the callback path and has the
        // method GET the exceptional case should not redirect to the oauth2 server as it would become
        // an infinite redirect loop. In this case an exception must be raised.
        if (
          context.request().method() == HttpMethod.GET &&
            context.normalisedPath().equals(callback.getPath())) {

          if (log.isWarnEnabled()) {
            log.warn("The callback route is shaded by the OAuth2AuthHandler, ensure the callback route is added BEFORE the OAuth2AuthHandler route!");
          }
          handler.handle(Future.failedFuture(new HttpStatusException(500, "Infinite redirect loop [oauth2 callback]")));
        } else {
          // the redirect is processed as a failure to abort the chain
          handler.handle(Future.failedFuture(new HttpStatusException(302, authURI(context.request().uri()))));
        }
      } else {
        // attempt to decode the token and handle it as a user
        ((OAuth2Auth) authProvider).decodeToken(token, decodeToken -> {
          if (decodeToken.failed()) {
            handler.handle(Future.failedFuture(new HttpStatusException(401, decodeToken.cause().getMessage())));
            return;
          }

          context.setUser(decodeToken.result());
          // continue
          handler.handle(Future.succeededFuture());
        });
      }
    });
  }

  private String authURI(String redirectURL) {
    final JsonObject config = new JsonObject()
      .put("state", redirectURL);

    if (host != null) {
      config.put("redirect_uri", host + callback.getPath());
    }

    if (extraParams != null) {
      config.mergeIn(extraParams);
    }

    if (scopes.size() > 0) {
      JsonArray _scopes = new JsonArray();
      // scopes are passed as an array because the auth provider has the knowledge on how to encode them
      for (String authority : scopes) {
        _scopes.add(authority);
      }

      config.put("scopes", _scopes);
    }

    return ((OAuth2Auth) authProvider).authorizeURL(config);
  }

  @Override
  public OAuth2AuthHandler extraParams(JsonObject extraParams) {
    this.extraParams = extraParams;
    return this;
  }

  @Override
  public OAuth2AuthHandler setupCallback(final Route route) {

    if (callbackPath != null && !"".equals(callbackPath)) {
      // no matter what path was provided we will make sure it is the correct one
      route.path(callbackPath);
    }

    route.method(HttpMethod.GET);

    route.handler(ctx -> {
      // Handle the callback of the flow
      final String code = ctx.request().getParam("code");

      // code is a require value
      if (code == null) {
        ctx.fail(400);
        return;
      }

      final String state = ctx.request().getParam("state");

      final JsonObject config = new JsonObject()
        .put("code", code);

      if (host != null) {
        config.put("redirect_uri", host + route.getPath());
      }

      if (extraParams != null) {
        config.mergeIn(extraParams);
      }

      authProvider.authenticate(config, res -> {
        if (res.failed()) {
          ctx.fail(res.cause());
        } else {
          ctx.setUser(res.result());
          Session session = ctx.session();
          if (session != null) {
            // the user has upgraded from unauthenticated to authenticated
            // session should be upgraded as recommended by owasp
            session.regenerateId();
            // we should redirect the UA so this link becomes invalid
            ctx.response()
              // disable all caching
              .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
              .putHeader("Pragma", "no-cache")
              .putHeader(HttpHeaders.EXPIRES, "0")
              // redirect (when there is no state, redirect to home
              .putHeader(HttpHeaders.LOCATION, state != null ? state : "/")
              .setStatusCode(302)
              .end("Redirecting to " + (state != null ? state : "/") + ".");
          } else {
            // there is no session object so we cannot keep state
            ctx.reroute(state != null ? state : "/");
          }
        }
      });
    });

    // the redirect handler has been setup so we can process this
    // handler has full oauth2
    bearerOnly = false;
    callback = route;

    return this;
  }
}
