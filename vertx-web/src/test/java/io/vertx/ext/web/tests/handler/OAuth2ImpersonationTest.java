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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.authorization.ScopeAuthorization;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.sstore.SessionStore;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Paulo Lopes
 */
public class OAuth2ImpersonationTest extends WebTestBase {

  private static final String USER_SWITCH_KEY = "__vertx.user-switch-ref";

  // mock an oauth2 server using code auth code flow
  OAuth2Auth oauth2;

  private static final JsonObject fixture_base = new JsonObject(
    "{" +
      "  \"sub\": \"base\"," +
      "  \"access_token\": \"base\"," +
      "  \"refresh_token\": \"base\"," +
      "  \"token_type\": \"bearer\"," +
      "  \"scope\": \"read\"," +
      "  \"expires_in\": 7200" +
      "}");

  private static final JsonObject fixture_admin = new JsonObject(
    "{" +
      "  \"sub\": \"admin\"," +
      "  \"access_token\": \"admin\"," +
      "  \"refresh_token\": \"admin\"," +
      "  \"token_type\": \"bearer\"," +
      "  \"scope\": \"read write\"," +
      "  \"expires_in\": 7200" +
      "}");

  private HttpServer server;

  @Override
  @AfterEach
  public void tearDown(VertxTestContext testContext) throws Exception {
    server.close();

    super.tearDown(testContext);
  }

  @Override
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);

    oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:10000"));

    final AtomicBoolean base = new AtomicBoolean(true);

    server = vertx.createHttpServer();

    server
      .requestHandler(req -> {
        if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
          req
            .setExpectMultipart(true)
            .bodyHandler(buffer ->
              req
                .response()
                .putHeader("Content-Type", "application/json")
                .end(base.compareAndSet(true, false) ? fixture_base.encode() : fixture_admin.encode()));
        } else if (req.method() == HttpMethod.POST && "/oauth/revoke".equals(req.path())) {
          req.setExpectMultipart(true).bodyHandler(buffer -> req.response().end());
        } else {
          req.response().setStatusCode(400).end();
        }
      })
      .listen(10000)
      .await();
  }

  @Test
  public void testSwitchUser() throws Exception {

    /////////////////////////
    // SETUP
    /////////////////////////

    // keep state
    router.route()
      .handler(SessionHandler.create(SessionStore.create(vertx)));

    // switch users setup
    // there are 2 routes for testing purposes
    router.route("/user-switch/impersonate")
      // this is a high precedence handler
      .handler(ctx -> {
        ctx.userContext()
          .loginHint(ctx.request().getParam("login_hint"))
          .impersonate(ctx.request().getParam("redirect_uri"))
          .onFailure(err -> {
            if (err instanceof HttpException) {
              ctx.fail(err);
            } else {
              ctx.fail(500);
            }
          });
      });

    router.route("/user-switch/undo")
      // this is a high precedence handler
      .handler(ctx -> {
        ctx.userContext()
          .loginHint(ctx.request().getParam("login_hint"))
          .restore(ctx.request().getParam("redirect_uri"))
          .onFailure(err -> {
            if (err instanceof HttpException) {
              ctx.fail(err);
            } else {
              ctx.fail(500);
            }
          });
      });

    // create an oauth2 handler on our domain to the callback: "http://localhost:8080/callback" and
    // protect everything under /protected
    router.route("/protected/*")
      .handler(OAuth2AuthHandler.create(vertx, oauth2, "http://localhost:8080/callback").setupCallback(router.route("/callback")));

    final AtomicReference<User> userRef = new AtomicReference<>();

    // mount 1st handler under the protected zone (regular user only can read)
    router
      .route("/protected/base")
      .handler(
        AuthorizationHandler
          .create(PermissionBasedAuthorization.create("read"))
          .addAuthorizationProvider(ScopeAuthorization.create()))
      .handler(rc -> {
        assertNotNull(rc.user());
        userRef.set(rc.user());
        rc.end("OK");
      });


    // mount 2nd handler under the protected zone (admin user can write)
    router
      .route("/protected/admin")
      .handler(
        AuthorizationHandler
          .create(PermissionBasedAuthorization.create("write"))
          .addAuthorizationProvider(ScopeAuthorization.create()))
      .handler(rc -> {
        assertNotNull(rc.user());
        System.out.println(rc.user().principal().encodePrettily());

        // assert that the old and new users are not the same
        User oldUser = userRef.get();
        assertNotNull(oldUser);
        User newUser = rc.user();
        assertFalse(oldUser.equals(newUser));

        // also the old user should be in the session
        User prevUser = rc.session().get(USER_SWITCH_KEY);
        assertNotNull(prevUser);
        assertEquals(prevUser, oldUser);

        rc.response().end("Welcome to the 2nd protected resource!");
      });


    /////////////////////////
    // TEST
    /////////////////////////

    // flow:
    // 1. user not authenticated
    // 2. app starts a redirect to the IdP
    // 3. IdP calls back, user gets to the desired endpoint

    final AtomicReference<String> stateRef = new AtomicReference<>();
    final AtomicReference<String> sessionRef = new AtomicReference<>();

    // 1. user isn't authenticated (no Authorization header, no Session cookie)
    // Expectation:
    //   * A redirect to the IdP, as we're mocking, we need to extract the state of the redirect URL so we can fake the
    //     callback to the app
    //   * We also need to have a session cookie otherwise we lose all the context and cannot have multiple identities
    HttpResponse<Buffer> resp = testRequest(webClient.get("/protected/base").followRedirects(false).send(), 302, "Found");
    // in this case we should get a redirect
    String redirectURL = resp.getHeader("Location");
    assertNotNull(redirectURL);
    String[] parts = redirectURL.substring(redirectURL.indexOf('?') + 1).split("&");

    for (String part : parts) {
      if (part.startsWith("state=")) {
        stateRef.set(part.substring(6));
      }
    }

    String setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);

    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));

    // 3. fake the redirect from the IdP. This happens with a success authn validation, we need to pass the right state
    // Expectations:
    //   * A new session cookie is returned, as the session id is regenerated to prevent replay attacks or privilege
    //     escalation bugs. Old session assumed an un authenticated user, this one is for the authenticated one
    //   * A final redirect happens to avoid caching the callback URL at the user-agent, so the browser will show
    //     the desired original URL
    resp = testRequest(webClient.get("/callback?state=" + stateRef.get() + "&code=1")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
    String destination = resp.getHeader(HttpHeaders.LOCATION.toString());
    stateRef.set(destination);

    // 4. Confirm that we can get the secured resource
    testRequest(webClient.get(stateRef.get())
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 200, "OK", "OK");

    //////////////////////////////
    // TEST SWITCHING IDENTITIES
    /////////////////////////////

    // test we can't get the admin resource (we're still base user)
    testRequest(webClient.get("/protected/admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 403, "Forbidden");

    // verify that the switch isn't possible for non authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     an Unauthorized response
    testRequest(webClient.get("/user-switch/impersonate?redirect_uri=/protected/admin&login_hint=admin")
      .send(), 401, "Unauthorized");

    // start the switch

    // flow:
    // 1. call the switch user endpoint
    // 2. a new Oauth2 auth flow starts like before
    // 3. In the end there should be a new user object and the previous one shall be in the session

    // User is authenticated (there is a session and a User) and a redirect to the IdP should happen
    // Expectations:
    //   * A redirect to the IdP should happen. (maybe there's a way to hint the desired user? This doesn't do it)
    resp = testRequest(webClient.get("/user-switch/impersonate?redirect_uri=/protected/admin&login_hint=admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // in this case we should get a redirect, and the session id must change
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    // the session must change
    assertFalse(setCookie.substring(0, setCookie.indexOf(';')).equals(sessionRef.get()));
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
    destination = resp.getHeader(HttpHeaders.LOCATION.toString());
    stateRef.set(destination);

    // verify that the switch isn't possible for non authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     a redirect to the IdP response
    resp = testRequest(webClient.get(stateRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // in this case we should get a redirect
    redirectURL = resp.getHeader("Location");
    assertNotNull(redirectURL);
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    // the session must be different
    assertFalse(setCookie.substring(0, setCookie.indexOf(';')).equals(sessionRef.get()));

    // verify that the switch is possible for authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     a redirect to the IdP response
    resp = testRequest(webClient.get(stateRef.get())
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // in this case we should get a redirect
    redirectURL = resp.getHeader("Location");
    assertNotNull(redirectURL);
    parts = redirectURL.substring(redirectURL.indexOf('?') + 1).split("&");
    boolean hintSeen = false;

    for (String part : parts) {
      if (part.startsWith("state=")) {
        stateRef.set(part.substring(6));
      }
      if (part.startsWith("login_hint=")) {
        hintSeen = true;
      }
    }

    // we should respect the hint
    assertTrue(hintSeen);

    // we're on the right session
    setCookie = resp.headers().get("set-cookie");
    assertNull(setCookie);

    // user is authenticated, it now escalates the permissions by re-doing the auth flow to upgrade the user
    // Expectations:
    //   * fake the IdP callback with the right state
    //   * like before ensure that the session id changes (base user -> admin user)
    //   * final redirect to the desired target resource, to avoid user-agents to cache the callback url
    resp = testRequest(webClient.get("/callback?state=" + stateRef.get() + "&code=1")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
    destination = resp.getHeader(HttpHeaders.LOCATION.toString());
    stateRef.set(destination);

    ////////////////////////////////////////
    // TEST GET RESOURCE WITH NEW IDENTITY
    ////////////////////////////////////////

    // final call to verify that the desired escalated user can get the final resource
    testRequest(webClient.get(stateRef.get())
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 200, "OK", "Welcome to the 2nd protected resource!");

    ////////////////////////////////////////
    // UNDO IMPERSONATION
    ////////////////////////////////////////

    resp = testRequest(webClient.get("/user-switch/undo?redirect_uri=/protected/base")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // in this case we should get a redirect, and the session id must change
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    // the session must change
    assertFalse(setCookie.substring(0, setCookie.indexOf(';')).equals(sessionRef.get()));
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
    destination = resp.getHeader(HttpHeaders.LOCATION.toString());
    stateRef.set(destination);

    // final call to verify that the desired de-escalated user can get the final resource
    testRequest(webClient.get(stateRef.get())
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 200, "OK", "OK");

    // final call to verify that the desired de-escalated user cannot get the admin resource
    testRequest(webClient.get("/protected/admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 403, "Forbidden");
  }
}
