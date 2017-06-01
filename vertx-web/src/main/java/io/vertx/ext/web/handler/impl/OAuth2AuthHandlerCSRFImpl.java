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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.RoutingContext;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An extension of OAuth2AuthHandlerImpl that uses a random state to protect against CSRF.
 * <p>
 * This has a number of caveats, it uses a shared cluster map to keep state parameters. This means that:
 * <ul>
 * <li>There is a potential race condition if a user manages to complete their authentication before this map completes
 * persisting the state across the cluster, and a user is returned to a cluster node that does not yet have the state.
 * In this case authentication would fail. This should be very unlikely but is possible.</li>
 * <li>The state is stored by default for 1 hour, so if a user takes longer than an hour to complete their authentication,
 * it will fail.</li>
 * <li>As a state is stored for every request in progress, there is a potential for overwhelming the server by a malicious
 * party creating a large number of authentication requests. This could bloat server side memory usage if the state map fills.</li>
 * </ul>
 *
 * @author John Oliver
 */
public class OAuth2AuthHandlerCSRFImpl extends OAuth2AuthHandlerImpl {
  private static final Logger log = LoggerFactory.getLogger(OAuth2AuthHandlerCSRFImpl.class);
  public static final String MAP_NAME = "o2auth-state-map";

  private final io.vertx.core.Vertx vertx;
  private final long ttl;
  private final int stateLength;
  private final PRNG random;

  public static final int STATE_LENGTH = 32;
  public static final long DEFAULT_TTL = 1000 * 60 * 60;

  public OAuth2AuthHandlerCSRFImpl(io.vertx.core.Vertx vertx, OAuth2Auth authProvider, String callbackURL) {
    this(vertx, authProvider, callbackURL, DEFAULT_TTL, STATE_LENGTH);
  }

  public OAuth2AuthHandlerCSRFImpl(io.vertx.core.Vertx vertx, OAuth2Auth authProvider, String callbackURL, long ttl, int stateLength) {
    super(authProvider, callbackURL);
    this.random = new PRNG(vertx);
    this.vertx = vertx;
    this.ttl = ttl;
    this.stateLength = stateLength;
  }

  private void getSharedMap(Handler<AsyncMap<String, String>> asyncResultHandler) {
    vertx
      .sharedData()
      .<String, String>getClusterWideMap(MAP_NAME, result -> {
        if (result.failed()) {
          log.error("Failed to create shared auth state map", result.cause());
        } else {
          asyncResultHandler.handle(result.result());
        }
      });
  }

  protected Future<String> getFinalUrl(RoutingContext ctx) {
    Future<String> stateFuture = Future.future();
    @Nullable String key = ctx.request().getParam("state");

    getStateForKey(key, state -> {
      if (state.failed()) {
        stateFuture.fail(state.cause());
      } else {
        stateFuture.complete(state.result());
        removeStateForKey(key);
      }
    });

    return stateFuture;
  }

  private String generateState() {
    byte[] data = new byte[stateLength];
    random.nextBytes(data);
    return new BigInteger(1, data).toString(32);
  }

  protected String formState(String redirectURL) {
    String key = generateState();
    put(key, redirectURL);
    return key;
  }

  private void getStateForKey(@Nullable String state, Handler<AsyncResult<String>> handler) {
    if (!vertx.isClustered()) {
      String redirectUrl = getLocalMap().get(state).getString("state");
      if (redirectUrl == null) {
        handler.handle(Future.failedFuture("State does not exist"));
      } else {
        handler.handle(Future.succeededFuture(redirectUrl));
      }
    } else {
      getSharedMap(map -> map.get(state, handler));
    }
  }

  private void removeStateForKey(@Nullable String state) {
    if (!vertx.isClustered()) {
      getLocalMap().remove(state);
    } else {
      getSharedMap(map -> map.remove(state, removed -> {
        if (removed.failed()) {
          log.error("Failed to remove state", removed.cause());
        }
      }));
    }
  }

  private void put(String state, String redirectURL) {
    if (!vertx.isClustered()) {
      addToLocalMap(state, redirectURL);
    } else {
      getSharedMap(map -> {
        map.put(state, redirectURL, ttl, result -> {
          if (result.failed()) {
            log.error("Failed to create shared state", result.cause());
          }
        });
      });
    }
  }

  private void addToLocalMap(String state, String redirectURL) {
    LocalMap<String, JsonObject> map = getLocalMap();
    map.put(state, new JsonObject()
      .put("state", redirectURL)
      .put("expire_time", System.currentTimeMillis() + ttl));
    enforceTtlOnLocalMap(map);
  }

  private void enforceTtlOnLocalMap(LocalMap<String, JsonObject> map) {
    long timeCutoff = System.currentTimeMillis() - ttl;
    List<String> oldEntries = map
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().getLong("expire_time") < timeCutoff)
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());

    oldEntries.forEach(map::remove);
  }

  private LocalMap<String, JsonObject> getLocalMap() {
    return vertx.sharedData().getLocalMap(MAP_NAME);
  }
}
