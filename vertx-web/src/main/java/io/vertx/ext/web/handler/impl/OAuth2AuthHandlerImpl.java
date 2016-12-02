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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.OAuth2AuthHandler;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class OAuth2AuthHandlerImpl extends AuthHandlerImpl implements OAuth2AuthHandler {

  private final String host;

  private Route callback;
  private JsonObject extraParams = new JsonObject();

  public OAuth2AuthHandlerImpl(OAuth2Auth authProvider, String host) {
    super(authProvider);
    this.host = host;
  }

  private String getHost(RoutingContext ctx) {
    if (host != null) {
      return host;
    } else {
      String host = ctx.request().getHeader("Host");

      if (host != null) {
        return (ctx.request().isSSL() ? "https://" : "http://") + host.split("\\s*,\\s*")[0];
      }

      return (ctx.request().isSSL() ? "https://" : "http://") + ctx.request().host();
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    User user = ctx.user();
    if (user != null) {
      // Already authenticated.

      // if this provider support JWT authorize
      if (((OAuth2Auth) authProvider).hasJWTToken()) {
        authorise(user, ctx);
      } else {
        // oauth2 used only for authentication (with or without scopes)
        ctx.next();
      }

    } else {
      // redirect request to the oauth2 server
      ctx.response()
          .putHeader("Location", authURI(getHost(ctx), ctx.normalisedPath()))
          .setStatusCode(302)
          .end();
    }
  }

  private String authURI(String host, String redirectURL) {
    if (callback == null) {
      throw new NullPointerException("callback is null");
    }

    if (authorities.size() > 0) {
      JsonArray scopes = new JsonArray();
      // scopes are passed as an array because the auth provider has the knowledge on how to encode them
      for (String authority : authorities) {
        scopes.add(authority);
      }

      return ((OAuth2Auth) authProvider).authorizeURL(new JsonObject()
        .put("redirect_uri", host + callback.getPath())
        .put("scopes", scopes)
        .put("state", redirectURL));
    } else {
      return ((OAuth2Auth) authProvider).authorizeURL(new JsonObject()
        .put("redirect_uri", host + callback.getPath())
        .put("state", redirectURL));
    }
  }

  @Override
  public OAuth2AuthHandler extraParams(JsonObject extraParams) {
    this.extraParams = extraParams;
    return this;
  }

  @Override
  public OAuth2AuthHandler setupCallback(Route route) {

    this.callback = route;

    route.handler(ctx -> {
      // Handle the callback of the flow
      final String code = ctx.request().getParam("code");

      // code is a require value
      if (code == null) {
        ctx.fail(400);
        return;
      }

      final String state = ctx.request().getParam("state");

      ((OAuth2Auth) authProvider).getToken(new JsonObject().put("code", code).put("redirect_uri", getHost(ctx) + callback.getPath()).mergeIn(extraParams), res -> {
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
              .putHeader("Location", state)
              .setStatusCode(302)
              .end("Redirecting to " + state + ".");
          } else {
            // there is no session object so we cannot keep state
            ctx.reroute(state);
          }
        }
      });
    });

    return this;
  }
}
