package io.vertx.ext.web.tests;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@VertxTest
public class RouterValidationTest {

  Vertx vertx;

  @BeforeEach
  public void setUp(Vertx vertx) {
    this.vertx = vertx;
  }

  @Test
  public void addBodyHandler() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
  }

  @Test
  public void addBodyHandlerAndUserHandler() {
    Router router = Router.router(vertx);
    router.route()
      .handler(BodyHandler.create())
      .handler(RoutingContext::end);
  }

  @Test
  public void addBodyHandlerAndUserHandlerBadOrder() {
    Router router = Router.router(vertx);
    assertThrows(IllegalStateException.class, () -> {
      router.route()
        .handler(RoutingContext::end)
        .handler(BodyHandler.create());
    });
  }

  @Test
  public void addBodyHandlerAndAuthn() {
    Router router = Router.router(vertx);
    router.route()
      .handler(BodyHandler.create())
      .handler(OAuth2AuthHandler.create(vertx, OAuth2Auth.create(vertx, new OAuth2Options().setClientId("test-id"))));
  }

  @Test
  public void addBodyHandlerAndAuthnAuthz() {
    Router router = Router.router(vertx);
    router.route()
      .handler(SessionHandler.create(SessionStore.create(vertx)))
      .handler(OAuth2AuthHandler.create(vertx, OAuth2Auth.create(vertx, new OAuth2Options().setClientId("test-id"))))
      .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("my-role")));
  }

  @Test
  public void addBodyHandlerAndAuthnAuthzBadOrder() {
    Router router = Router.router(vertx);
    // will fail as authz depends on authn (so adding authn after authz is clearly a mistake)
    assertThrows(IllegalStateException.class, () -> {
      router.route()
        .handler(SessionHandler.create(SessionStore.create(vertx)))
        .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("my-role")))
        .handler(OAuth2AuthHandler.create(vertx, OAuth2Auth.create(vertx, new OAuth2Options().setClientId("test-id"))));
    });
  }

  @Test
  public void addBodyHandlerAndAuthnAuthzBadOrder2() {
    Router router = Router.router(vertx);
    assertThrows(IllegalStateException.class, () -> {
      router.route()
        .handler(OAuth2AuthHandler.create(vertx, OAuth2Auth.create(vertx, new OAuth2Options().setClientId("test-id"))))
        .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("my-role")))
        .handler(SessionHandler.create(SessionStore.create(vertx)));
    });
  }

  @Test
  public void addSecurityPolicyAndUserHandlers() {
    Router router = Router.router(vertx);
    router.route()
      .handler(CorsHandler.create())
      .subRouter(SockJSHandler.create(vertx).bridge(new SockJSBridgeOptions()));
  }

  @Test
  public void addSecurityPolicyAndUserHandlersBadOrder() {
    Router router = Router.router(vertx);
    assertThrows(IllegalStateException.class, () -> {
      // will fail as the route should be exclusive
      router.route()
        .subRouter(SockJSHandler.create(vertx).bridge(new SockJSBridgeOptions()))
        .handler(CorsHandler.create());
    });
  }

  @Test
  public void addSecurityPolicyAndUserHandlersBadOrder2() {
    Router router = Router.router(vertx);
    assertThrows(IllegalStateException.class, () -> {
      // will fail as the route has a user handler but a policy one is being added after
      router.route()
        .handler(RoutingContext::end)
        .handler(CorsHandler.create());
    });
  }
}
