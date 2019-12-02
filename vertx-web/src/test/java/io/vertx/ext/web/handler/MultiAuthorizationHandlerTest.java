package io.vertx.ext.web.handler;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthorizationProvider;
import io.vertx.ext.auth.RoleBasedAuthorization;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.WebTestBase;

public class MultiAuthorizationHandlerTest extends WebTestBase {

  JWTAuth authProvider;

  @Before
  public void setup() throws Exception {
    JsonObject authConfig = new JsonObject().put("keyStore",
        new JsonObject().put("type", "jceks").put("path", "keystore.jceks").put("password", "secret"));

    authProvider = JWTAuth.create(vertx, new JWTAuthOptions(authConfig));
  }

  @Test
  public void testJWTAuthenticationNoAuthorization() throws Exception {
    // we are testing the following:
    // authentication via jwt
    // no authorization provider is registered
    // no authorization is required on the path
    // => the test should succeed
    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));

    router.route("/protected/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().principal().getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(HttpMethod.GET, "/protected/page1",
        req -> req.putHeader("Authorization",
            "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())),
        200, "OK", "Welcome");
  }

  @Test
  public void testJWTAuthenticationWithAuthorization1() throws Exception {
    // we are testing the following:
    // authentication via jwt
    // no authorization provider is registered
    // an authorization is required on the path
    // => the test should fail
    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));
    router.route("/protected/*").handler(AuthorizationHandler.create(RoleBasedAuthorization.create("role1")));

    router.route("/protected/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().principal().getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(HttpMethod.GET, "/protected/page1",
        req -> req.putHeader("Authorization",
            "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())),
        403, "Forbidden", "Forbidden");
  }

  @Test
  public void testJWTAuthenticationWithAuthorization2() throws Exception {
    // we are testing the following:
    // authentication via jwt
    // one authorization provider is registered
    // an authorization is required on the path
    // => the test should succeed
    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));
    router.route("/protected/*")
        .handler(
            AuthorizationHandler.create(RoleBasedAuthorization.create("role1"))
            .addAuthorizationProvider(AuthorizationProvider.create("authzProvider1", Set.of(RoleBasedAuthorization.create("role1"))))
        );

    router.route("/protected/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().principal().getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(HttpMethod.GET, "/protected/page1",
        req -> req.putHeader("Authorization",
            "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())),
        200, "OK", "Welcome");
  }

  @Test
  public void testJWTAuthenticationWithAuthorization3() throws Exception {
    // we are testing the following:
    // authentication via jwt
    // 3 authorization providers are registered
    // an authorization is required on the path
    // => the test should succeed
    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));
    router.route("/protected/*")
        .handler(
            AuthorizationHandler.create(RoleBasedAuthorization.create("role3"))
            .addAuthorizationProvider(AuthorizationProvider.create("authzProvider1", Set.of(RoleBasedAuthorization.create("role1"))))
            .addAuthorizationProvider(AuthorizationProvider.create("authzProvider2", Set.of(RoleBasedAuthorization.create("role2"))))
            .addAuthorizationProvider(AuthorizationProvider.create("authzProvider3", Set.of(RoleBasedAuthorization.create("role3"))))
        );

    router.route("/protected/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().principal().getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(HttpMethod.GET, "/protected/page1",
        req -> req.putHeader("Authorization",
            "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())),
        200, "OK", "Welcome");
  }

  @Test
  public void testJWTAuthenticationWithAuthorization4() throws Exception {
    // we are testing the following:
    // authentication via jwt
    // 3 authorization providers are registered
    // an authorization is required on the path
    // => the test should fail since no authorization providers provide the correct authorization 
    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));
    router.route("/protected/*")
        .handler(
            AuthorizationHandler.create(RoleBasedAuthorization.create("role4"))
            .addAuthorizationProvider(AuthorizationProvider.create("authzProvider1", Set.of(RoleBasedAuthorization.create("role1"))))
            .addAuthorizationProvider(AuthorizationProvider.create("authzProvider2", Set.of(RoleBasedAuthorization.create("role2"))))
            .addAuthorizationProvider(AuthorizationProvider.create("authzProvider3", Set.of(RoleBasedAuthorization.create("role3"))))
        );

    router.route("/protected/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().principal().getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(HttpMethod.GET, "/protected/page1",
        req -> req.putHeader("Authorization",
            "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions())),
        403, "Forbidden", "Forbidden");
  }

}
