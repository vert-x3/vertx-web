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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Paulo Lopes
 */
public class JWTAuthHandlerTest extends WebTestBase {

  public JWTAuthHandlerTest() {
    super(ReportMode.FORBIDDEN);
  }

  JWTAuth authProvider;

  @Before
  public void setup() throws Exception {
    authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));
  }

  @Test
  public void testLogin() throws Exception {

    Handler<RoutingContext> handler = rc -> {
      Assert.assertNotNull(rc.user());
      Assert.assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome to the protected resource!");
    };

    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));

    router.route("/protected/somepage").handler(handler);

    testRequest(webClient.get("/protected/somepage").send(), 401, "Unauthorized");

    // Now try again with credentials
    testRequest(webClient.get("/protected/somepage").putHeader("Authorization", "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())), 200, "OK", "Welcome to the protected resource!");

  }

  @Test
  public void testLoginFail() throws Exception {

    Handler<RoutingContext> handler = rc -> {
      Assert.fail("should not get here");
      rc.response().end("Welcome to the protected resource!");
    };

    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));

    router.route("/protected/somepage").handler(handler);

    testRequest(HttpMethod.GET, "/protected/somepage", 401, "Unauthorized", null);

    // Now try again with bad token
    final String token = authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions());

    testRequest(webClient.get("/protected/somepage").putHeader("Authorization", "Bearer x" + token), 401, "Unauthorized");

    testRequest(webClient.get("/protected/somepage").putHeader("Authorization", "Basic " + token), 401, "Unauthorized");

  }

  @Test
  public void testLoginWithScopes() throws Exception {

    router.route()
      .handler(JWTAuthHandler.create(authProvider)
        .withScopes(Arrays.asList("a", "b")))
      .handler(RoutingContext::end);

    // Payload as String list
    final JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", String.join(" ", Arrays.asList("a", "b")));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadA)), 200, "OK");

    // Payload as Array
    final JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", new JsonArray().add("a").add("b"));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadB)), 200, "OK");
  }

  @Test
  public void testLoginWithMissingScopes() throws Exception {

    router.route()
      .handler(JWTAuthHandler.create(authProvider)
        .withScopes(Arrays.asList("a", "b", "c")))
      .handler(RoutingContext::end);

    // Payload as String list
    final JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", String.join(" ", Arrays.asList("a", "b")));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadA)), 403, "Forbidden");

    // Payload as Array
    final JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", new JsonArray().add("a").add("b"));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadB)), 403, "Forbidden");
  }

  @Test
  public void testLoginWithScopeDelimiter() throws Exception {

    router.route()
      .handler(JWTAuthHandler.create(authProvider)
        .withScopes(Arrays.asList("a", "b"))
        .scopeDelimiter(","))
      .handler(RoutingContext::end);

    // Payload as String list
    final JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", String.join(" ", Arrays.asList("a", "b")));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadA)), 403, "Forbidden");

    // Payload with right delimiter
    final JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", String.join(",", Arrays.asList("a", "b")));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadB)), 200, "OK");
  }

  @Test
  public void testLoginWithScopesFromMetadata() throws Exception {

    router.route()
      .putMetadata("scopes", Arrays.asList("a", "b"))
      .handler(JWTAuthHandler.create(authProvider))
      .handler(RoutingContext::end);

    // Payload as String list
    final JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", String.join(" ", Arrays.asList("a", "b")));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadA)), 200, "OK");

    // Payload as Array
    final JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", new JsonArray().add("a").add("b"));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadB)), 200, "OK");
  }

  @Test
  public void testLoginWithScopesFromMetadataSingle() throws Exception {

    router.route()
      .putMetadata("scopes", "a")
      .handler(JWTAuthHandler.create(authProvider))
      .handler(RoutingContext::end);

    // Payload as String list
    final JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "a");

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadA)), 200, "OK");

    // Payload as Array
    final JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", new JsonArray().add("a"));

    testRequest(webClient.get("/").putHeader("Authorization", "Bearer " + authProvider.generateToken(payloadB)), 200, "OK");
  }
}
