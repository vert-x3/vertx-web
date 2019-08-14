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

package io.vertx.ext.web.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paulo Lopes
 */
public class JWTAuthHandlerTest extends WebTestBase {

  JWTAuth authProvider;

  @Before
  public void setup() throws Exception {
    JsonObject authConfig = new JsonObject().put("keyStore", new JsonObject()
        .put("type", "jceks")
        .put("path", "keystore.jceks")
        .put("password", "secret"));

    authProvider = JWTAuth.create(vertx, new JWTAuthOptions(authConfig));
  }

  @Test
  public void testLogin() throws Exception {

    Handler<RoutingContext> handler = rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().principal().getString("sub"));
      rc.response().end("Welcome to the protected resource!");
    };

    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));

    router.route("/protected/somepage").handler(handler);

    testRequest(HttpMethod.GET, "/protected/somepage", null, resp -> {
    }, 401, "Unauthorized", null);

    // Now try again with credentials
    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("Authorization", "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())), 200, "OK", "Welcome to the protected resource!");

  }

  @Test
  public void testLoginFail() throws Exception {

    Handler<RoutingContext> handler = rc -> {
      fail("should not get here");
      rc.response().end("Welcome to the protected resource!");
    };

    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));

    router.route("/protected/somepage").handler(handler);

    testRequest(HttpMethod.GET, "/protected/somepage", null, 401, "Unauthorized", null);

    // Now try again with bad token
    final String token = authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions());

    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("Authorization", "Bearer x" + token), 401, "Unauthorized", null);

    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("Authorization", "Basic " + token), 401, "Unauthorized", null);

  }
}
