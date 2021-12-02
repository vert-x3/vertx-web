package io.vertx.ext.web;

import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class RouterValidationTest {

  @Rule
  public final RunTestOnContext rule = new RunTestOnContext();

  @Test
  public void addBodyHandler() {
    Router router = Router.router(rule.vertx());
    router.route().handler(BodyHandler.create());
  }

  @Test
  public void addBodyHandlerAndUserHandler() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(BodyHandler.create())
      .handler(RoutingContext::end);
  }

  @Test(expected = AssertionError.class)
  public void addBodyHandlerAndUserHandlerBadOrder() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(RoutingContext::end)
      .handler(BodyHandler.create());
  }

  @Test
  public void addBodyHandlerAndAuthn() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(BodyHandler.create())
      .handler(OAuth2AuthHandler.create(rule.vertx(), OAuth2Auth.create(rule.vertx(), new OAuth2Options().setClientId("test-id"))));
  }

  @Test
  public void addBodyHandlerAndAuthnAuthz() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(SessionHandler.create(SessionStore.create(rule.vertx())))
      .handler(OAuth2AuthHandler.create(rule.vertx(), OAuth2Auth.create(rule.vertx(), new OAuth2Options().setClientId("test-id"))))
      .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("my-role")));
  }

  @Test(expected = AssertionError.class)
  public void addBodyHandlerAndAuthnAuthzBadOrder() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(SessionHandler.create(SessionStore.create(rule.vertx())))
      .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("my-role")))
      // will fail as authz depends on authn (so adding authn after authz is clearly a mistake)
      .handler(OAuth2AuthHandler.create(rule.vertx(), OAuth2Auth.create(rule.vertx(), new OAuth2Options().setClientId("test-id"))));
  }

  @Test(expected = AssertionError.class)
  public void addBodyHandlerAndAuthnAuthzBadOrder2() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(OAuth2AuthHandler.create(rule.vertx(), OAuth2Auth.create(rule.vertx(), new OAuth2Options().setClientId("test-id"))))
      .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("my-role")))
      // will fail as platform handlers should be mounted earlier as they have dependants
      .handler(SessionHandler.create(SessionStore.create(rule.vertx())));
  }

  @Test
  public void addSecurityPolicyAndUserHandlers() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(CorsHandler.create())
      .subRouter(SockJSHandler.create(rule.vertx()).bridge(new SockJSBridgeOptions()));
  }

  @Test(expected = IllegalStateException.class)
  public void addSecurityPolicyAndUserHandlersBadOrder() {
    Router router = Router.router(rule.vertx());
    router.route()
      .subRouter(SockJSHandler.create(rule.vertx()).bridge(new SockJSBridgeOptions()))
      // will fail as the route should be exclusive
      .handler(CorsHandler.create());
  }

  @Test(expected = AssertionError.class)
  public void addSecurityPolicyAndUserHandlersBadOrder2() {
    Router router = Router.router(rule.vertx());
    router.route()
      .handler(RoutingContext::end)
      // will fail as the route has a user handler but a policy one is being added after
      .handler(CorsHandler.create());
  }
}
