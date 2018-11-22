package io.vertx.ext.web.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.junit.Test;

public class ChainAuthHandlerTest extends WebTestBase {

  protected ChainAuthHandler chain;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    JsonObject authConfig = new JsonObject().put("properties_path", "classpath:login/loginusers.properties");
    AuthProvider authProvider = ShiroAuth.create(vertx, new ShiroAuthOptions().setType(ShiroAuthRealmType.PROPERTIES).setConfig(authConfig));

    // create a chain
    chain = ChainAuthHandler.create();

    chain
      .append(JWTAuthHandler.create(null))
      .append(BasicAuthHandler.create(authProvider))
      .append(RedirectAuthHandler.create(authProvider));

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(chain);
    router.route().handler(ctx -> ctx.response().end());
  }

  @Test
  public void testWithoutAuthorization() throws Exception {
    // since there is no authorization header the final status code will be 302 because it was
    // the last appended handler
    testRequest(HttpMethod.GET, "/", 302, "Found", "Redirecting to /loginpage.");
  }

  @Test
  public void testWithAuthorization() throws Exception {
    // there is an authorization header, so it should be handled properly
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcw=="),200, "OK", "");
  }

  @Test
  public void testWithBadAuthorization() throws Exception {
    // there is an authorization header, but the token is invalid it should be processed by the basic auth
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcX=="),401, "Unauthorized", "Unauthorized");
  }
}
