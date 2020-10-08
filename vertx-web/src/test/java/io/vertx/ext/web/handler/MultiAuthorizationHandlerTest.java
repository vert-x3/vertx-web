package io.vertx.ext.web.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.WebTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class MultiAuthorizationHandlerTest extends WebTestBase {

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
          .addAuthorizationProvider(createProvider("authzProvider1", RoleBasedAuthorization.create("role1")))
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
          .addAuthorizationProvider(createProvider("authzProvider1", RoleBasedAuthorization.create("role1")))
          .addAuthorizationProvider(createProvider("authzProvider2", RoleBasedAuthorization.create("role2")))
          .addAuthorizationProvider(createProvider("authzProvider3", RoleBasedAuthorization.create("role3")))
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
          .addAuthorizationProvider(createProvider("authzProvider1", RoleBasedAuthorization.create("role1")))
          .addAuthorizationProvider(createProvider("authzProvider2", RoleBasedAuthorization.create("role2")))
          .addAuthorizationProvider(createProvider("authzProvider3", RoleBasedAuthorization.create("role3")))
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

  private AuthorizationProvider createProvider(String id, Authorization authorization) {
    Set<Authorization> _authorizations = new HashSet<>();
    _authorizations.add(authorization);
    return new AuthorizationProvider() {

      @Override
      public String getId() {
        return id;
      }

      @Override
      public void getAuthorizations(User user, Handler<AsyncResult<Void>> handler) {
        user.authorizations().add(getId(), _authorizations);
        handler.handle(Future.succeededFuture());
      }
    };
  }
}
