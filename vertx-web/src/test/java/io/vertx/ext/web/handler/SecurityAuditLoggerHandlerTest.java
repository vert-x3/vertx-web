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
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Kind of hard to test this!
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SecurityAuditLoggerHandlerTest extends WebTestBase {

  JWTAuth authProvider;

  @Before
  public void setup() throws Exception {
    authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testPlainRequest() throws Exception {
    router.route().handler(SecurityAuditLoggerHandler.create());
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/somedir", 200, "OK");
    // [REQUEST epoch="1674824895327" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /somedir" status=200] OK
  }

  @Test
  public void testPlainRequestError() throws Exception {
    router.route().handler(SecurityAuditLoggerHandler.create());
    router.route().handler(rc -> {
      throw new RuntimeException("foo");
    });
    testRequest(HttpMethod.GET, "/somedir", 500, "Internal Server Error");
    // [REQUEST epoch="1674824993981" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /somedir" status=500] FAIL
  }

  @Test
  public void testAuthRequestNoToken() throws Exception {
    router.route()
      .handler(SecurityAuditLoggerHandler.create())
      .handler(JWTAuthHandler.create(authProvider))
      .handler(RoutingContext::end);

    testRequest(HttpMethod.GET, "/protected/foo",401, "Unauthorized");
    // [REQUEST epoch="1674825160736" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/foo" status=401] FAIL

  }

  @Test
  public void testAuthRequestValidToken() throws Exception {
    router.route()
      .handler(SecurityAuditLoggerHandler.create())
      .handler(JWTAuthHandler.create(authProvider))
      .handler(ctx -> ctx.end("OK"));

    testRequest(HttpMethod.GET, "/protected/foo", req -> req.putHeader("Authorization", "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())), 200, "OK", "OK");
    // [AUTHENTICATION epoch="1674825292685" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/foo" token="********************************..."] OK
    // [REQUEST epoch="1674825292688" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/foo" status=200] OK
  }

  @Test
  public void testAuthRequestInvalidToken() throws Exception {
    router.route()
      .handler(SecurityAuditLoggerHandler.create())
      .handler(JWTAuthHandler.create(authProvider))
      .handler(ctx -> ctx.end("OK"));

    testRequest(HttpMethod.GET, "/protected/foo", req -> req.putHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"), 401, "Unauthorized", null);
    // [AUTHENTICATION epoch="1674825394738" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/foo" token="********************************..."] FAIL
    // [REQUEST epoch="1674825292688" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/foo" status=200] OK
  }

  @Test
  public void testAuthRequestAuthz() throws Exception {
    router.route()
      .handler(SecurityAuditLoggerHandler.create());

    // we are testing the following:
    // authentication via jwt
    // 3 authorization providers are registered
    // an authorization is required on the path
    // => the test should succeed
    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));
    router.route("/protected/*")
      .handler(
        AuthorizationHandler.create(RoleBasedAuthorization.create("role3"))
          .addAuthorizationProvider(createProvider("authzProvider1", RoleBasedAuthorization.create("role1")))
          .addAuthorizationProvider(createProvider("authzProvider2", RoleBasedAuthorization.create("role2")))
          .addAuthorizationProvider(createProvider("authzProvider3", RoleBasedAuthorization.create("role3")))
      );

    router.route("/protected/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().get().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(HttpMethod.GET, "/protected/page1",
      req -> req.putHeader("Authorization",
        "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())),
      200, "OK", "Welcome");


    // [AUTHENTICATION epoch="1674825632005" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/page1" token="********************************..."] OK
    // [AUTHORIZATION epoch="1674825632007" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/page1" subject="paulo" authorization="ROLE[role3]"] OK
    // [REQUEST epoch="1674825632010" source="127.0.0.1" destination="127.0.0.1" resource="HTTP/1.1 GET /protected/page1" status=200] OK
  }

  private AuthorizationProvider createProvider(String id, Authorization authorization) {
    return new AuthorizationProvider() {
      @Override
      public String getId() {
        return null;
      }

      @Override
      public Future<Void> getAuthorizations(User user) {
        user.authorizations().put(id, authorization);
        return Future.succeededFuture();
      }
    };
  }
}
