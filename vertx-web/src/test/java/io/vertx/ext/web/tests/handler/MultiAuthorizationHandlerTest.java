package io.vertx.ext.web.tests.handler;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class MultiAuthorizationHandlerTest extends WebTestBase {

  JWTAuth authProvider;

  @BeforeEach
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
      assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(webClient.get("/protected/page1")
        .putHeader("Authorization",
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
      assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(webClient.get("/protected/page1")
        .putHeader("Authorization",
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
      assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(webClient.get("/protected/page1")
        .putHeader("Authorization",
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
      assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(webClient.get("/protected/page1")
        .putHeader("Authorization",
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
      assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    // login with correct credentials
    testRequest(webClient.get("/protected/page1")
        .putHeader("Authorization",
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
      public Future<Void> getAuthorizations(User user) {
        user.authorizations().put(getId(), _authorizations);
        return Future.succeededFuture();
      }
    };
  }

  @Test
  public void testJWTAuthenticationWithAuthorizationForbiddenHang() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route("/open").handler(RoutingContext::end);
    router.route("/protected1/*").handler(JWTAuthHandler.create(authProvider));
    router.route("/protected1/*").handler(AuthorizationHandler
      .create(RoleBasedAuthorization.create("role2"))
      .addAuthorizationProvider(createProvider("authzProvider1", RoleBasedAuthorization.create("role2"))));
    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));
    router.route("/protected/*").handler(AuthorizationHandler
      .create(RoleBasedAuthorization.create("role1"))
      .addAuthorizationProvider(createProvider("authzProvider1", RoleBasedAuthorization.create("role2"))));

    router.route("/protected1/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    router.route("/protected/page1").handler(rc -> {
      assertNotNull(rc.user());
      assertEquals("paulo", rc.user().attributes().getJsonObject("accessToken").getString("sub"));
      rc.response().end("Welcome");
    });

    AtomicReference<String> session = new AtomicReference<>();

    // login with correct credentials
    HttpResponse<Buffer> res = testRequest(webClient.get("/protected1/page1")
        .putHeader("Authorization",
          "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions()))
        .send(),
      200, "OK", "Welcome");
    String cookie = res.getHeader("Set-Cookie");
    assertNotNull(cookie);
    session.set(cookie);

    // 2nd try it hangs?
    testRequest(webClient.get("/protected/page1")
        .putHeader("Authorization",
          "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions()))
        .putHeader("Cookie", session.get().substring(0, session.get().indexOf(';'))),
      403, "Forbidden", "Forbidden");
  }
}
