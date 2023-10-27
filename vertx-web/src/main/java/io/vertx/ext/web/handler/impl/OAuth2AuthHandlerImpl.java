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

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.audit.Marker;
import io.vertx.ext.auth.audit.SecurityAudit;
import io.vertx.ext.auth.common.AuthenticationContextInternal;
import io.vertx.ext.auth.common.Session;
import io.vertx.ext.auth.common.UserContextInternal;
import io.vertx.ext.auth.oauth2.AbstractOAuth2Handler;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.OAuth2AuthHandler;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class OAuth2AuthHandlerImpl extends AbstractOAuth2Handler<RoutingContext> implements OAuth2AuthHandler {

  private static final Logger LOG = LoggerFactory.getLogger(OAuth2AuthHandlerImpl.class);

  private int order = -1;
  private Route callback;

  public OAuth2AuthHandlerImpl(Vertx vertx, OAuth2Auth authProvider, String callbackURL) {
    this(vertx, authProvider, callbackURL, null);
  }

  public OAuth2AuthHandlerImpl(Vertx vertx, OAuth2Auth authProvider, String callbackURL, String realm) {
    super(vertx, authProvider, callbackURL, realm);
  }

  private OAuth2AuthHandlerImpl(OAuth2AuthHandlerImpl base, List<String> scopes) {
    super(base, scopes);
    this.callback = base.callback;
    this.order = base.order;
  }

  @Override
  public OAuth2AuthHandler withScope(String scope) {
    Objects.requireNonNull(scope, "scope cannot be null");

    List<String> updatedScopes = new ArrayList<>(this.scopes);
    updatedScopes.add(scope);
    return new OAuth2AuthHandlerImpl(this, updatedScopes);
  }

  @Override
  public OAuth2AuthHandler withScopes(List<String> scopes) {
    Objects.requireNonNull(scopes, "scopes cannot be null");
    return new OAuth2AuthHandlerImpl(this, scopes);
  }
  
  @Override
  public OAuth2AuthHandler setupCallback(final Route route) {

    if (callbackURL == null) {
      // warn that the setup is probably wrong
      throw new IllegalStateException("OAuth2AuthHandler was created without a origin/callback URL");
    }

    final String routePath = route.getPath();

    if (routePath == null) {
      // warn that the setup is probably wrong
      throw new IllegalStateException("OAuth2AuthHandler callback route created without a path");
    }

    final String callbackPath = callbackURL.resource();

    if (callbackPath != null && !"".equals(callbackPath)) {
      if (!callbackPath.endsWith(routePath)) {
        if (LOG.isWarnEnabled()) {
          LOG.warn("callback route doesn't match OAuth2AuthHandler origin configuration");
        }
      }
    }

    this.callback = route;
    // order was already known, but waiting for the callback
    if (this.order != -1) {
      mountCallback();
    }

    // the redirect handler has been setup so we can process this
    // handler has full oauth2 support, not just basic JWT
    bearerOnly = false;
    return this;
  }

  @Override
  public void onOrder(int order) {
    // order isn't known yet, we can attempt to mount
    if (this.order == -1) {
      this.order = order;
      // callback route already known, but waiting for order
      if (callback != null) {
        mountCallback();
      }
    }
  }

  private void callbackHandler(RoutingContext ctx) {
      // Some IdP's (e.g.: AWS Cognito) returns errors as query arguments
      String error = ctx.request().getParam("error");

      if (error != null) {
        int errorCode;
        // standard error's from the Oauth2 RFC
        switch (error) {
          case "invalid_token":
            errorCode = 401;
            break;
          case "insufficient_scope":
            errorCode = 403;
            break;
          case "invalid_request":
          default:
            errorCode = 400;
            break;
        }

        String errorDescription = ctx.request().getParam("error_description");
        if (errorDescription != null) {
          fail(ctx, errorCode, error + ": " + errorDescription);
        } else {
          fail(ctx, errorCode, error);
        }
        return;
      }

      // Handle the callback of the flow
      final String code = ctx.request().getParam("code");

      // code is a require value
      if (code == null) {
        fail(ctx, 400, "Missing code parameter");
        return;
      }

      final Oauth2Credentials credentials = new Oauth2Credentials()
        .setFlow(OAuth2FlowType.AUTH_CODE)
        .setCode(code);

      // the state that was passed to the IdP server. The state can be
      // an opaque random string (to protect against replay attacks)
      // or if there was no session available the target resource to
      // server after validation
      final String state = ctx.request().getParam("state");

      // state is a required field
      if (state == null) {
        fail(ctx, 400, "Missing IdP state parameter to the callback endpoint");
        return;
      }

      final String resource;
      final Session session = ctx.session();

      if (session != null) {
        // validate the state. Here we are a bit lenient, if there is no session
        // we always assume valid, however if there is session it must match
        String ctxState = session.remove("state");
        // if there's a state in the context they must match
        if (!state.equals(ctxState)) {
          // forbidden, the state is not valid (this is a replay attack)
          fail(ctx, 401, "Invalid oauth2 state");
          return;
        }

        // remove the code verifier, from the session as it will be trade for the
        // token during the final leg of the oauth2 handshake
        String codeVerifier = session.remove("pkce");
        credentials.setCodeVerifier(codeVerifier);
        // state is valid, extract the redirectUri from the session
        resource = session.get("redirect_uri");
      } else {
        resource = state;
      }

      // The valid callback URL set in your IdP application settings.
      // This must exactly match the redirect_uri passed to the authorization URL in the previous step.
      credentials.setRedirectUri(callbackURL.href());

      final SecurityAudit audit = ((AuthenticationContextInternal) ctx).securityAudit();
      audit.credentials(credentials);

      authProvider
        .authenticate(credentials)
        .andThen(op -> audit.audit(Marker.AUTHENTICATION, op.succeeded()))
        .onFailure(ctx::fail)
        .onSuccess(user -> {
          ((UserContextInternal) ctx.user())
            .setUser(user);
          String location = resource != null ? resource : "/";
          if (session != null) {
            // the user has upgraded from unauthenticated to authenticated
            // session should be upgraded as recommended by owasp
            session.regenerateId();
          } else {
            // there is no session object so we cannot keep state.
            // if there is no session and the resource is relative
            // we will reroute to "location"
            if (location.length() != 0 && location.charAt(0) == '/') {
              ctx.reroute(location);
              return;
            }
          }

          // we should redirect the UA so this link becomes invalid
          ctx.response()
            // disable all caching
            .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .putHeader("Pragma", "no-cache")
            .putHeader(HttpHeaders.EXPIRES, "0")
            // redirect (when there is no state, redirect to home
            .putHeader(HttpHeaders.LOCATION, location)
            .setStatusCode(302)
            .end("Redirecting to " + location + ".");
      });
  }

  private void mountCallback() {

    callback
      .method(HttpMethod.GET)
      // we want the callback before this handler
      .order(order - 1);

    callback.handler(this::callbackHandler);
  }


//TODO remove duplicated code from WebAuthenticationHandlerImpl
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
