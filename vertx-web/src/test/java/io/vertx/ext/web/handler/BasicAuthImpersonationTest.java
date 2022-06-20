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

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.auth.properties.PropertyFileAuthorization;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.web.handler.UserSwitchHandler.USER_SWITCH_KEY;

/**
 * @author Paulo Lopes
 */
public class BasicAuthImpersonationTest extends WebTestBase {

  AuthenticationProvider authn;
  AuthorizationProvider authz;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    authn = PropertyFileAuthentication.create(vertx, "login/loginusers.properties");
    authz = PropertyFileAuthorization.create(vertx, "login/loginusers.properties");
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
    // there are 2 routes
    router.route("/user-switch/impersonate")
      // this is a high precedence handler
      .handler(UserSwitchHandler.impersonate());

    router.route("/user-switch/undo")
      // this is a high precedence handler
      .handler(UserSwitchHandler.undo());

    // protect everything under /protected
    router.route("/protected/*")
      .handler(BasicAuthHandler.create(authn));

    final AtomicReference<User> userRef = new AtomicReference<>();

    // mount 1st handler under the protected zone (regular user only can read)
    router
      .route("/protected/base")
        .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("read")).addAuthorizationProvider(authz))
      .handler(rc -> {
        assertNotNull(rc.user());
        userRef.set(rc.user());
        rc.end("OK");
      });


    // mount 2nd handler under the protected zone (admin user can write)
    router
      .route("/protected/admin")
      .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("write")).addAuthorizationProvider(authz))
      .handler(rc -> {
        assertNotNull(rc.user());

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

    final AtomicReference<String> sessionRef = new AtomicReference<>();

    // 1. user isn't authenticated (no Authorization header, no Session cookie)
    // Expectation:
    //   * A redirect to the IdP, as we're mocking, we need to extract the state of the redirect URL so we can fake the
    //     callback to the app
    //   * We also need to have a session cookie otherwise we lose all the context and cannot have multiple identities
    testRequest(HttpMethod.GET, "/protected/base", null, resp -> {
      // in this case we should get a WWW-Authenticate
      String redirectURL = resp.getHeader("WWW-Authenticate");
      assertNotNull(redirectURL);
      // there's no session yet
      String setCookie = resp.headers().get("set-cookie");
      assertNull(setCookie);
    }, 401, "Unauthorized", null);

    // 3. fake the redirect from the IdP. This happens with a success authn validation, we need to pass the right state
    // Expectations:
    //   * A new session cookie is returned, as the session id is regenerated to prevent replay attacks or privilege
    //     escalation bugs. Old session assumed an un authenticated user, this one is for the authenticated one
    //   * A final redirect happens to avoid caching the callback URL at the user-agent, so the browser will show
    //     the desired original URL
    testRequest(
      HttpMethod.GET,
      "/protected/base",
      req -> {
        req.putHeader(HttpHeaders.AUTHORIZATION, "Basic cmVndWxhcjpyZWd1bGFy");
      }, resp -> {
        // session upgrade (secure against replay attacks)
        String setCookie = resp.headers().get("set-cookie");
        assertNotNull(setCookie);

        sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
      }, 200, "OK", null);

    // 4. Confirm that we can get the secured resource
    testRequest(
      HttpMethod.GET,
      "/protected/base",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
      }, 200, "OK", "OK");

    //////////////////////////////
    // TEST SWITCHING IDENTITIES
    /////////////////////////////

    // test we can't get the admin resource (we're still base user)
    testRequest(
      HttpMethod.GET,
      "/protected/admin",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
      }, 403, "Forbidden", null);

    // verify that the switch isn't possible for non authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     an Unauthorized response
    testRequest(
      HttpMethod.GET,
      "/user-switch/impersonate?redirect_uri=/protected/admin&login_hint=admin",
      req -> {
      }, resp -> {
      }, 401, "Unauthorized", null);

    // start the switch

    // flow:
    // 1. call the switch user endpoint
    // 2. a new Oauth2 auth flow starts like before
    // 3. In the end there should be a new user object and the previous one shall be in the session

    // User is authenticated (there is a session and a User) and a redirect to the IdP should happen
    // Expectations:
    //   * A redirect to the IdP should happen. (maybe there's a way to hint the desired user? This doesn't do it)
    testRequest(
      HttpMethod.GET,
      "/user-switch/impersonate?redirect_uri=/protected/admin&login_hint=admin",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
        // in this case we should get a redirect, and the session id must change
        // session upgrade (secure against replay attacks)
        String setCookie = resp.headers().get("set-cookie");
        assertNotNull(setCookie);
        // the session must change
        assertFalse(setCookie.substring(0, setCookie.indexOf(';')).equals(sessionRef.get()));

        sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));

        String destination = resp.getHeader(HttpHeaders.LOCATION);
        assertNotNull(destination);
      }, 302, "Found", null);

    // verify that the switch isn't possible for non authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     a redirect to the IdP response
    testRequest(
      HttpMethod.GET,
      "/protected/admin",
      req -> {
      }, resp -> {
      }, 401, "Unauthorized", null);

    // verify that the switch is possible for authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     a redirect to the IdP response
    testRequest(
      HttpMethod.GET,
      "/protected/admin",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
        // in this case we should get a WWW-Authenticate
        String redirectURL = resp.getHeader("WWW-Authenticate");
        assertNotNull(redirectURL);
        // there's no session yet
        String setCookie = resp.headers().get("set-cookie");
        assertNull(setCookie);
      }, 401, "Unauthorized", null);

    // user is authenticated, it now escalates the permissions by re-doing the auth flow to upgrade the user
    // Expectations:
    //   * fake the IdP callback with the right state
    //   * like before ensure that the session id changes (base user -> admin user)
    //   * final redirect to the desired target resource, to avoid user-agents to cache the callback url
    testRequest(
      HttpMethod.GET,
      "/protected/admin",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
        req.putHeader(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4=");
      }, resp -> {
        // session upgrade (secure against replay attacks)
        String setCookie = resp.headers().get("set-cookie");
        assertNotNull(setCookie);

        sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
      }, 200, "OK", null);

    ////////////////////////////////////////
    // TEST GET RESOURCE WITH NEW IDENTITY
    ////////////////////////////////////////

    // final call to verify that the desired escalated user can get the final resource
    testRequest(
      HttpMethod.GET,
      "/protected/admin",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
      }, 200, "OK", "Welcome to the 2nd protected resource!");

    ////////////////////////////////////////
    // UNDO IMPERSONATION
    ////////////////////////////////////////

    testRequest(
      HttpMethod.GET,
      "/user-switch/undo?redirect_uri=/protected/base",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
        // in this case we should get a redirect, and the session id must change
        // session upgrade (secure against replay attacks)
        String setCookie = resp.headers().get("set-cookie");
        assertNotNull(setCookie);
        // the session must change
        assertFalse(setCookie.substring(0, setCookie.indexOf(';')).equals(sessionRef.get()));

        sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));

        String destination = resp.getHeader(HttpHeaders.LOCATION);
        assertNotNull(destination);
      }, 302, "Found", null);

    // final call to verify that the desired de-escalated user can get the final resource
    testRequest(
      HttpMethod.GET,
      "/protected/base",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
      }, 200, "OK", "OK");

    // final call to verify that the desired de-escalated user cannot get the admin resource
    testRequest(
      HttpMethod.GET,
      "/protected/admin",
      req -> {
        req.putHeader(HttpHeaders.COOKIE, sessionRef.get());
      }, resp -> {
      }, 403, "Forbidden", null);
  }
}
