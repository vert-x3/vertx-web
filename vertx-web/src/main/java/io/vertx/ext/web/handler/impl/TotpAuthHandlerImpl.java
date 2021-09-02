/*
 * Copyright 2021 Red Hat, Inc.
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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.otp.OtpCredentials;
import io.vertx.ext.auth.otp.OtpKey;
import io.vertx.ext.auth.otp.OtpKeyGenerator;
import io.vertx.ext.auth.otp.totp.TotpAuth;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.OtpAuthHandler;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class TotpAuthHandlerImpl extends AuthenticationHandlerImpl<TotpAuth> implements OtpAuthHandler {

  private final OtpKeyGenerator otpKeyGen;

  private String verifyUrl;
  private String issuer;
  private String label;

  // the extra routes
  private Route register = null;
  private Route verify = null;

  public TotpAuthHandlerImpl(TotpAuth totp, OtpKeyGenerator otpKeyGen) {
    super(totp, "totp");
    this.otpKeyGen = otpKeyGen;
  }

  private static boolean matchesRoute(RoutingContext ctx, Route route) {
    if (route != null) {
      return ctx.request().method() == HttpMethod.POST && ctx.normalizedPath().equals(route.getPath());
    }
    return false;
  }

  @Override
  public void authenticate(RoutingContext ctx, Handler<AsyncResult<User>> handler) {
    if (verify == null) {
      handler.handle(Future.failedFuture(new HttpException(500, new IllegalStateException("No callback mounted!"))));
      return;
    }

    if (matchesRoute(ctx, verify)) {
      handler.handle(Future.failedFuture(new HttpException(500, new IllegalStateException("The verify callback route is shaded by the OTPAuthHandler, ensure the callback route is added BEFORE the OTPAuthHandler route!"))));
      return;
    }

    if (matchesRoute(ctx, register)) {
      handler.handle(Future.failedFuture(new HttpException(500, new IllegalStateException("The register callback route is shaded by the OTPAuthHandler, ensure the callback route is added BEFORE the OTPAuthHandler route!"))));
      return;
    }

    if (ctx.user() == null) {
      handler.handle(Future.failedFuture(new HttpException(401)));
    } else {
      Boolean userOtp = ctx.user().get("mfa");
      // user hasn't 2fa yet?
      if (userOtp == null || !userOtp) {
        if (verifyUrl == null) {
          handler.handle(Future.failedFuture(new HttpException(401, "User TOTP verification missing")));
        } else {
          if (ctx.session() != null) {
            String uri = ctx.request().uri();
            ctx.session()
              .put("redirect_uri", uri);
          }

          handler.handle(Future.failedFuture(new HttpException(302, verifyUrl)));
        }
      } else {
        handler.handle(Future.succeededFuture(ctx.user()));
      }
    }
  }

  @Override
  public OtpAuthHandler verifyUrl(String verifyUrl) {
    this.verifyUrl = verifyUrl;
    return this;
  }

  @Override
  public OtpAuthHandler issuer(String issuer) {
    this.issuer = issuer;
    return this;
  }

  @Override
  public OtpAuthHandler label(String label) {
    this.label = label;
    return this;
  }

  @Override
  public OtpAuthHandler setupRegisterCallback(Route route) {
    this.register = route
      // force a post if otherwise
      .method(HttpMethod.POST)
      .handler(ctx -> {
        if (ctx.user().get("username") == null) {
          ctx.fail(new IllegalStateException("User object misses 'username' attribute"));
          return;
        }
        final OtpKey key = otpKeyGen.generate();
        authProvider.createAuthenticator(ctx.user().get("username"), key)
          .onFailure(ctx::fail)
          .onSuccess(authenticator ->
            ctx.json(
              new JsonObject()
                .put("issuer", issuer)
                .put("label", label)
                .put("url", authProvider.generateUri(key, issuer, ctx.user().get("username"), label))));
      });

    return this;
  }

  @Override
  public OtpAuthHandler setupCallback(Route route) {
    this.verify = route
      // force a post if otherwise
      .method(HttpMethod.POST)
      .handler(ctx -> {
        if (ctx.user().get("username") == null) {
          ctx.fail(new IllegalStateException("User object misses 'username' attribute"));
          return;
        }

        if ( ctx.request().getParam("code") == null) {
          ctx.fail(new HttpException(400, "Missing 'code' form attribute"));
          return;
        }
        authProvider.authenticate(new OtpCredentials(ctx.user().get("username"), ctx.request().getParam("code")))
          .onSuccess(user -> {
            User currentUser = ctx.user();
            currentUser.principal().mergeIn(user.principal());
            currentUser.attributes().mergeIn(user.attributes());
            // marker
            currentUser.attributes().put("mfa", "totp");
            String redirect = "/";
            if (ctx.session() != null) {
              // the user has upgraded from unauthenticated to authenticated
              // session should be upgraded as recommended by owasp
              ctx.session().regenerateId();

              String back = ctx.session().get("redirect_uri");
              if (back != null) {
                redirect = back;
              }
            }

            ctx.redirect(redirect);
          })
          .onFailure(err -> ctx.fail(401, err));
      });

    return this;
  }
}
