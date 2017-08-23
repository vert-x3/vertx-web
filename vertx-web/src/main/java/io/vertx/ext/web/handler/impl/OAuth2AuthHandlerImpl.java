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
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.OAuth2AuthHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import static io.vertx.ext.auth.oauth2.OAuth2FlowType.*;
import static io.vertx.ext.web.handler.impl.AuthorizationAuthHandler.Type.*;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class OAuth2AuthHandlerImpl extends AuthorizationAuthHandler implements OAuth2AuthHandler {

  private static Type getTypeForFlow(OAuth2Auth provider) {
    switch (provider.getFlowType()) {
      case AUTH_CODE:
        return BEARER;
      case PASSWORD:
        return BASIC;
      case AUTH_JWT:
      case CLIENT:
      default:
        throw new RuntimeException("Oauth2 Flow " + provider.getFlowType() + " not allowed!");
    }
  }

  private final String host;
  private final String callbackPath;
  private final boolean supportJWT;

  private Route callback;
  private JsonObject extraParams = new JsonObject();

  public OAuth2AuthHandlerImpl(OAuth2Auth authProvider, String callbackURL) {
    super(authProvider, getTypeForFlow(authProvider));

    this.supportJWT = authProvider.hasJWTToken();

    if (authProvider.getFlowType() == AUTH_CODE) {
      try {
        final URL url = new URL(callbackURL);
        this.host = url.getProtocol() + "://" + url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort());
        this.callbackPath = url.getPath();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    } else {
      host = null;
      callbackPath = null;
    }
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    switch (type) {
      case BASIC:
        parseAuthorization(context, false, parseAuthorization -> {
          if (parseAuthorization.failed()) {
            handler.handle(Future.failedFuture(parseAuthorization.cause()));
            return;
          }

          final String suser;
          final String spass;

          try {
            // decode the payload
            String decoded = new String(Base64.getDecoder().decode(parseAuthorization.result()));

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
        });
        break;

      case BEARER:
        if (supportJWT) {
          parseAuthorization(context, true, parseAuthorization -> {
            if (parseAuthorization.failed()) {
              handler.handle(Future.failedFuture(parseAuthorization.cause()));
              return;
            }
            // if the provider supports JWT we can try to validate the Authorization header
            final String token = parseAuthorization.result();

            if (token != null) {
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
        // redirect request to the oauth2 server
        handler.handle(Future.failedFuture(new HttpStatusException(302, authURI(host, context.request().uri()))));
        break;

      default:
        handler.handle(Future.failedFuture("Unsupported Authorization type: " + type));
        break;
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
        .put("state", redirectURL)
        .mergeIn(extraParams));
    } else {
      return ((OAuth2Auth) authProvider).authorizeURL(new JsonObject()
        .put("redirect_uri", host + callback.getPath())
        .put("state", redirectURL)
        .mergeIn(extraParams));
    }
  }

  @Override
  public OAuth2AuthHandler extraParams(JsonObject extraParams) {
    this.extraParams = extraParams;
    return this;
  }

  @Override
  public OAuth2AuthHandler setupCallback(Route route) {

    callback = route;

    if (!"".equals(callbackPath)) {
      // no matter what path was provided we will make sure it is the correct one
      callback.path(callbackPath);
    }
    callback.method(HttpMethod.GET);

    route.handler(ctx -> {
      // Handle the callback of the flow
      final String code = ctx.request().getParam("code");

      // code is a require value
      if (code == null) {
        ctx.fail(400);
        return;
      }

      final String state = ctx.request().getParam("state");

      authProvider.authenticate(new JsonObject().put("code", code).put("redirect_uri", host + callback.getPath()).mergeIn(extraParams), res -> {
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
              // redirect
              .putHeader(HttpHeaders.LOCATION, state)
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
