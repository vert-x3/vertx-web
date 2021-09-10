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

import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

/**
 * A stateless interceptor for session management that operates on the {@code HttpContext}
 */
public class OAuth2AwareInterceptor implements Handler<HttpContext<?>> {

  private final Set<HttpContext<?>> dejaVu = new HashSet<>();
  private final WebClientOauth2Aware parentClient;

  public OAuth2AwareInterceptor(WebClientOauth2Aware webClientOauth2Aware) {
    this.parentClient = webClientOauth2Aware;
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
        if (dejaVu.contains(context)) {
          // already seen, clear and continue without recovery
          dejaVu.remove(context);
          context.next();
        } else {
          // we need some stop condition so we don't go into an infinite loop
          dejaVu.add(context);
          parentClient
            .oauth2Auth()
            .authenticate(parentClient.getCredentials())
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
          parentClient
            .oauth2Auth()
            .refresh(parentClient.getUser())
            .onSuccess(userResult -> {
              parentClient.setUser(userResult);
              context.requestOptions().putHeader(AUTHORIZATION, "Bearer " + userResult.principal().getString("access_token"));
              promise.complete();
            })
            .onFailure(error -> {
              // Refresh token failed, we can try standard authentication
              parentClient
                .oauth2Auth()
                .authenticate(parentClient.getCredentials())
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
        parentClient
          .oauth2Auth()
          .authenticate(parentClient.getCredentials())
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
}
