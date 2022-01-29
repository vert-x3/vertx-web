/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.auth.User;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

/**
 * A stateless interceptor for session management that operates on the {@code HttpContext}
 */
public class OAuth2AwareInterceptor implements Handler<HttpContext<?>> {

  private final Set<HttpContext<?>> dejaVu = new HashSet<>();
  private final Oauth2WebClientAware parentClient;
  private final AtomicReference<Future<User>> pendingAuth = new AtomicReference<>();
  private final boolean failFast;

  public OAuth2AwareInterceptor(Oauth2WebClientAware webClientOauth2Aware, boolean failFast) {
    this.parentClient = webClientOauth2Aware;
    this.failFast = failFast;
  }

  @Override
  public void handle(HttpContext<?> context) {
    switch (context.phase()) {
      case CREATE_REQUEST:
        createRequest(context)
          .onFailure(context::fail)
          .onSuccess(done -> context.next());
        break;
      case DISPATCH_RESPONSE:
        processResponse(context);
        break;
      default:
        context.next();
        break;
    }
  }

  private void processResponse(HttpContext<?> context) {
    switch (context.response().statusCode()) {
      case 401:
        if (!parentClient.isRenewTokenOnForbidden() || dejaVu.contains(context)) {
          // already seen, clear and continue without recovery
          dejaVu.remove(context);
          context.next();
        } else {
          // we need some stop condition so we don't go into an infinite loop
          dejaVu.add(context);
          authenticate()
            .onSuccess(userResult -> {
              // update the user
              parentClient.setUser(userResult);
              context.createRequest(context.requestOptions());
            })
            .onFailure(err -> {
              dejaVu.remove(context);
              parentClient.setUser(null);
              context.fail(err);
            });
        }
        break;
      default:
        // already seen, clear and continue without recovery
        dejaVu.remove(context);
        context.next();
    }
  }

  private Future<Void> createRequest(HttpContext<?> context) {

    Promise<Void> promise = Promise.promise();
    if (parentClient.getCredentials() != null) {
      if (parentClient.getUser() != null) {
        if (parentClient.getUser().expired(parentClient.getLeeway())) {
          //Token has expired we need to invalidate the session
          refreshToken()
            .onSuccess(userResult -> {
              parentClient.setUser(userResult);
              context.requestOptions().putHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
              promise.complete();
            })
            .onFailure(error -> {
              // Refresh token failed, we can try standard authentication
              authenticate()
                .onSuccess(userResult -> {
                  parentClient.setUser(userResult);
                  context.requestOptions().putHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
                  promise.complete();
                })
                .onFailure(errorAuth -> {
                  //Refresh token did not work and failed to obtain new authentication token, we need to fail
                  parentClient.setUser(null);
                  promise.fail(errorAuth);
                });
            });
        } else {
          //User is not expired, access_token is valid
          context.requestOptions().putHeader(AUTHORIZATION, "Bearer " + parentClient.getUser().principal().getString("access_token"));
          promise.complete();
        }
      } else {
        authenticate()
          .onSuccess(userResult -> {
            parentClient.setUser(userResult);
            context.requestOptions().putHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
            promise.complete();
          })
          .onFailure(promise::fail);
      }
    } else {
      promise.fail("Missing client credentials");
    }

    return promise.future();
  }

  private Future<User> authenticate() {
    final Future<User> pendingAuthFuture = pendingAuth.get();
    if (pendingAuthFuture != null) {
      if (failFast) {
        return Future.failedFuture("OAuth2 web client authentication in progress and client is configured to fail fast");
      } else {
        return pendingAuthFuture;
      }
    }
    final Future<User> newAuthFuture = parentClient
      .oauth2Auth()
      .authenticate(parentClient.getCredentials());
    pendingAuth.set(newAuthFuture);
    newAuthFuture.onComplete(userAsyncResult -> pendingAuth.set(null));
    return newAuthFuture;
  }

  private Future<User> refreshToken() {
    final Future<User> pendingAuthFuture = pendingAuth.get();
    if (pendingAuthFuture != null) {
      if (failFast) {
        return Future.failedFuture("OAuth2 web client token refresh in progress and client is configured to fail fast");
      } else {
        return pendingAuthFuture;
      }
    }
    final Future<User> newRefreshTokenFuture = parentClient
      .oauth2Auth()
      .refresh(parentClient.getUser());
    pendingAuth.set(newRefreshTokenFuture);
    newRefreshTokenFuture.onComplete(userAsyncResult -> pendingAuth.set(null));
    return newRefreshTokenFuture;
  }
}
