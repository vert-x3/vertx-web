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

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.WebTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paulo Lopes
 */
public class APIKeyHandlerTest extends WebTestBase {

  AuthenticationProvider authProvider;

  @Before
  public void setup() throws Exception {
    authProvider = (credentials, resultHandler) -> {
      if ("APIKEY".equals(credentials.getString("token"))) {
        resultHandler.handle(Future.succeededFuture(User.create(new JsonObject())));
      } else {
        resultHandler.handle(Future.failedFuture("Uknown APIKEY"));
      }
    };
  }

  @Test
  public void testHeader() throws Exception {

    router.route("/protected/*").handler(APIKeyHandler.create(authProvider));

    router.route("/protected/somepage").handler(rc -> {
      assertNotNull(rc.user());
      rc.response().end("Welcome to the protected resource!");
    });

    testRequest(HttpMethod.GET, "/protected/somepage", null, null, 401, "Unauthorized", null);
    // Now try again with API Key
    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("x-api-key", "APIKEY"), 200, "OK", "Welcome to the protected resource!");
    // Now try again with wrong API Key
    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("x-api-key", "APIKEY2"), 401, "Unauthorized", null);
  }

  @Test
  public void testCustomHeader() throws Exception {

    router.route("/protected/*").handler(APIKeyHandler.create(authProvider).header("xyz-api-key"));

    router.route("/protected/somepage").handler(rc -> {
      assertNotNull(rc.user());
      rc.response().end("Welcome to the protected resource!");
    });

    testRequest(HttpMethod.GET, "/protected/somepage", null, null, 401, "Unauthorized", null);
    // Now try again with API Key
    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("xyz-api-key", "APIKEY"), 200, "OK", "Welcome to the protected resource!");
    // Now try again with wrong API Key
    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("xyz-api-key", "APIKEY2"), 401, "Unauthorized", null);
  }

  @Test
  public void testCustomParam() throws Exception {

    router.route("/protected/*").handler(APIKeyHandler.create(authProvider).parameter("api_key"));

    router.route("/protected/somepage").handler(rc -> {
      assertNotNull(rc.user());
      rc.response().end("Welcome to the protected resource!");
    });

    testRequest(HttpMethod.GET, "/protected/somepage", null, null, 401, "Unauthorized", null);
    // Now try again with API Key
    testRequest(HttpMethod.GET, "/protected/somepage?api_key=APIKEY", null, 200, "OK", "Welcome to the protected resource!");
    // Now try again with wrong API Key
    testRequest(HttpMethod.GET, "/protected/somepage?api_key=APIKEY2", null, 401, "Unauthorized", null);
  }

  @Test
  public void testCustomCookie() throws Exception {

    router.route("/protected/*").handler(APIKeyHandler.create(authProvider).cookie("api-key"));

    router.route("/protected/somepage").handler(rc -> {
      assertNotNull(rc.user());
      rc.response().end("Welcome to the protected resource!");
    });

    testRequest(HttpMethod.GET, "/protected/somepage", null, null, 401, "Unauthorized", null);
    // Now try again with API Key
    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("Cookie", "api-key=APIKEY"), 200, "OK", "Welcome to the protected resource!");
    // Now try again with wrong API Key
    testRequest(HttpMethod.GET, "/protected/somepage", req -> req.putHeader("Cookie", "api-key=APIKEY2"), 401, "Unauthorized", null);
  }
}
