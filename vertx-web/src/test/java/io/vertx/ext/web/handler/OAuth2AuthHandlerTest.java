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

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author Paulo Lopes
 */
public class OAuth2AuthHandlerTest extends WebTestBase {

  private static final JsonObject fixture = new JsonObject(
      "{" +
          "  \"access_token\": \"4adc339e0\"," +
          "  \"refresh_token\": \"ec1a59d298\"," +
          "  \"token_type\": \"bearer\"," +
          "  \"expires_in\": 7200" +
          "}");

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  private String redirectURL = null;

  @Test
  public void testHappyFlow() throws Exception {

    // lets mock a oauth2 server using code auth code flow
    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, OAuth2FlowType.AUTH_CODE, new OAuth2ClientOptions()
        .setClientID("client-id")
        .setClientSecret("client-secret")
        .setSite("http://localhost:10000"));

    final CountDownLatch latch = new CountDownLatch(1);

    HttpServer server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
        req.setExpectMultipart(true).bodyHandler(buffer -> {
          req.response().putHeader("Content-Type", "application/json").end(fixture.encode());
        });
      } else if (req.method() == HttpMethod.POST && "/oauth/revoke".equals(req.path())) {
        req.setExpectMultipart(true).bodyHandler(buffer -> {
          req.response().end();
        });
      } else {
        req.response().setStatusCode(400).end();
      }
    }).listen(10000, ready -> {
      if (ready.failed()) {
        throw new RuntimeException(ready.cause());
      }
      // ready
      latch.countDown();
    });

    latch.await();

    // create a oauth2 handler on our domain: "http://localhost:8080"
    OAuth2AuthHandler oauth2Handler = OAuth2AuthHandler.create(oauth2, "http://localhost:8080");

    // setup the callback handler for receiving the callback
    oauth2Handler.setupCallback(router.get("/callback"));

    // protect everything under /protected
    router.route("/protected/*").handler(oauth2Handler);
    // mount some handler under the protected zone
    router.route("/protected/somepage").handler(rc -> {
      assertNotNull(rc.user());
      rc.response().end("Welcome to the protected resource!");
    });


    testRequest(HttpMethod.GET, "/protected/somepage", null, resp -> {
      // in this case we should get a redirect
      redirectURL = resp.getHeader("Location");
      assertNotNull(redirectURL);
    }, 302, "Found", null);

    // fake the redirect
    testRequest(HttpMethod.GET, "/callback?state=/protected/somepage&code=1", null, resp -> {
    }, 200, "OK", "Welcome to the protected resource!");

    server.close();
  }
}
