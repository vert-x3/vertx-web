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

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paulo Lopes
 * @author John Oliver
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

  private interface OAuth2AuthHandlerProvider {
    OAuth2AuthHandler get(OAuth2Auth oauth2, Vertx vertx);
  }

  public void testHappyFlow(OAuth2AuthHandlerProvider oAuth2AuthHandlerProvider) throws Exception {

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

    // create a oauth2 handler on our domain to the callback: "http://localhost:8080/callback"
    OAuth2AuthHandler oauth2Handler = oAuth2AuthHandlerProvider.get(oauth2, vertx);

    // setup the callback handler for receiving the callback
    oauth2Handler.setupCallback(router.route());

    // protect everything under /protected
    router.route("/protected/*").handler(oauth2Handler);
    // mount some handler under the protected zone
    router.route("/protected/somepage").handler(rc -> {
      assertNotNull(rc.user());
      rc.response().end("Welcome to the protected resource!");
    });


    final CountDownLatch redirectLatch = new CountDownLatch(1);

    testRequest(HttpMethod.GET, "/protected/somepage", null, resp -> {
      // in this case we should get a redirect
      redirectURL = resp.getHeader("Location");
      assertNotNull(redirectURL);
      redirectLatch.countDown();

    }, 302, "Found", null);

    redirectLatch.await();
    String state = getState();

    // fake the redirect
    testRequest(HttpMethod.GET, "/callback?state=" + state + "&code=1", null, redirectResp -> {
    }, 200, "OK", "Welcome to the protected resource!");


    server.close();
  }

  private String getState() throws UnsupportedEncodingException {
    String url = URLDecoder.decode(redirectURL, "UTF-8");
    Matcher matcher = Pattern.compile("state=([^&]+)").matcher(url);
    matcher.find();
    return matcher.group(1);
  }

  @Test
  public void testHappyFlowWithStandardHandler() throws Exception {
    testHappyFlow((oauth2, vertx) -> OAuth2AuthHandler.create(oauth2, "http://localhost:8080/callback"));
  }

  @Test
  public void testHappyFlowWithCsrfHandler() throws Exception {
    testHappyFlow((oauth2, vertx) -> OAuth2AuthHandler.createCSRFProtected(vertx, oauth2, "http://localhost:8080/callback"));
  }

}
