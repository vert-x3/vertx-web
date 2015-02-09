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

package io.vertx.ext.apex.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.sstore.LocalSessionStore;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.Session;
import io.vertx.ext.apex.sstore.SessionStore;
import io.vertx.ext.auth.AuthService;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BasicAuthHandlerTest extends AuthHandlerTestBase {

  protected AtomicReference<String> sessionCookie = new AtomicReference<>();

  @Test
  public void testLoginDefaultRealm() throws Exception {
    doLogin(BasicAuthHandler.DEFAULT_REALM);
  }

  @Test
  public void testLoginNonDefaultRealm() throws Exception {
    doLogin("aardvarks");
  }

  private void doLogin(String realm) throws Exception {

    Handler<RoutingContext> handler = rc -> {
      Session sess = rc.session();
      assertNotNull(sess);
      assertTrue(sess.isLoggedIn());
      rc.response().end("Welcome to the protected resource!");
    };

    router.route().handler(BodyHandler.create());
    router.route().handler(CookieHandler.create());
    SessionStore store = LocalSessionStore.create(vertx);
    router.route().handler(SessionHandler.create(store));
    JsonObject authConfig = new JsonObject().put("properties_path", "classpath:login/loginusers.properties");
    AuthService authService = AuthService.create(vertx, authConfig);
    router.route("/protected").handler(BasicAuthHandler.create(authService, realm));

    router.route("/protected/somepage").handler(handler);

    testRequest(HttpMethod.GET, "/protected/somepage", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertNotNull(setCookie);
      sessionCookie.set(setCookie);
      String wwwAuth = resp.headers().get("WWW-Authenticate");
      assertNotNull(wwwAuth);
      assertEquals("Basic realm=\"" + realm + "\"", wwwAuth);
    }, 401, "Unauthorized", null);

    // Now try again with credentials
    testRequest(HttpMethod.GET, "/protected/somepage", req -> {
      req.putHeader("cookie", sessionCookie.get());
      req.putHeader("Authorization", "Basic dGltOnNhdXNhZ2Vz");
    }, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertEquals(sessionCookie.get(), setCookie);
      String wwwAuth = resp.headers().get("WWW-Authenticate");
      assertNull(wwwAuth);

    }, 200, "OK", "Welcome to the protected resource!");

  }

  @Test
  public void loginFail() throws Exception {

    String realm = "apex";

    Handler<RoutingContext> handler = rc -> {
      Session sess = rc.session();
      assertNotNull(sess);
      assertTrue(sess.isLoggedIn());
      rc.response().end("Welcome to the protected resource!");
    };

    router.route().handler(BodyHandler.create());
    router.route().handler(CookieHandler.create());
    SessionStore store = LocalSessionStore.create(vertx);
    router.route().handler(SessionHandler.create(store));
    JsonObject authConfig = new JsonObject().put("properties_path", "classpath:login/loginusers.properties");
    AuthService authService = AuthService.create(vertx, authConfig);
    router.route("/protected").handler(BasicAuthHandler.create(authService));

    router.route("/protected/somepage").handler(handler);

    testRequest(HttpMethod.GET, "/protected/somepage", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertNotNull(setCookie);
      sessionCookie.set(setCookie);
      String wwwAuth = resp.headers().get("WWW-Authenticate");
      assertNotNull(wwwAuth);
      assertEquals("Basic realm=\"" + realm + "\"", wwwAuth);
    }, 401, "Unauthorized", null);

    // Now try again with bad credentials
    testRequest(HttpMethod.GET, "/protected/somepage", req -> {
      req.putHeader("cookie", sessionCookie.get());
      req.putHeader("Authorization", "Basic dGltOn5hdXdhZ2Vz");
    }, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertEquals(sessionCookie.get(), setCookie);
      String wwwAuth = resp.headers().get("WWW-Authenticate");
      assertNotNull(wwwAuth);
      assertEquals("Basic realm=\"" + realm + "\"", wwwAuth);

    }, 401, "Unauthorized", null);

  }

  @Override
  protected AuthHandler createAuthHandler(AuthService authService) {
    return BasicAuthHandler.create(authService);
  }
}
