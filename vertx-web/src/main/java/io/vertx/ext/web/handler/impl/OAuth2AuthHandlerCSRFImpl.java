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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import java.math.BigInteger;

/**
 * An extension of OAuth2AuthHandlerImpl that uses a random state to protect against CSRF.
 * <p>
 * This uses a {@link io.vertx.ext.web.Session} in order to track its state, therefore it is required that
 * a {@link io.vertx.ext.web.handler.SessionHandler} has been configured on this route.
 *
 * @author John Oliver
 */
public class OAuth2AuthHandlerCSRFImpl extends OAuth2AuthHandlerImpl {
  private static final Logger log = LoggerFactory.getLogger(OAuth2AuthHandlerCSRFImpl.class);
  private static final String SESSION_STATE_NAME = "oauth";
  public static final String AUTH_STATE_DATA_NAME = "state";
  public static final String REDIRECT_URL_NAME = "redirect_url";

  private final int stateLength;
  private final PRNG random;

  private static final int STATE_LENGTH = 32;

  public OAuth2AuthHandlerCSRFImpl(io.vertx.core.Vertx vertx, OAuth2Auth authProvider, String callbackURL) {
    this(vertx, authProvider, callbackURL, STATE_LENGTH);
  }

  public OAuth2AuthHandlerCSRFImpl(io.vertx.core.Vertx vertx, OAuth2Auth authProvider, String callbackURL, int stateLength) {
    super(authProvider, callbackURL);
    this.random = new PRNG(vertx);
    this.stateLength = stateLength;
  }

  protected Future<String> getFinalUrl(RoutingContext ctx) {
    Future<String> stateFuture = Future.future();
    @Nullable String key = ctx.request().getParam("state");
    Session session = getSession(ctx);
    if (session == null) {
      stateFuture.fail("Failed to find session, did you add a session handler?");
      return stateFuture;
    }

    JsonObject authData = (JsonObject) session.data().get(SESSION_STATE_NAME);

    if (authData == null) {
      stateFuture.fail("Unable to find auth state");
    } else if (key == null) {
      stateFuture.fail("State not provided");
    } else {
      String redirectUrl = authData.getString(REDIRECT_URL_NAME);
      String state = authData.getString(AUTH_STATE_DATA_NAME);

      if (!key.equals(state)) {
        stateFuture.fail("Session and called state differ");
      } else if (redirectUrl == null) {
        stateFuture.fail("No redirect url");
      } else {
        session.remove(SESSION_STATE_NAME);
        stateFuture.complete(redirectUrl);
      }
    }

    return stateFuture;
  }

  private String generateState() {
    byte[] data = new byte[stateLength];
    random.nextBytes(data);
    return new BigInteger(1, data).toString(32);
  }

  @Override
  protected String formState(RoutingContext ctx) {
    String state = generateState();

    Session session = getSession(ctx);
    if (session == null) {
      return null;
    }

    session.data().put(SESSION_STATE_NAME, new JsonObject()
      .put(AUTH_STATE_DATA_NAME, state)
      .put(REDIRECT_URL_NAME, ctx.normalisedPath()));

    return state;
  }

  private Session getSession(RoutingContext ctx) {
    Session session = ctx.session();
    if (session == null) {
      log.error("No session, did you forget to add a session handler");
    }

    return session;
  }

}
