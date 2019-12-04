package io.vertx.ext.web.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.junit.Test;

public class ChainAuthHandlerTest extends WebTestBase {

  private AuthHandler redirectAuthHandler;

  protected ChainAuthHandler chain;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    AuthProvider authProvider = PropertyFileAuthentication.create(vertx, "login/loginusers.properties");
    redirectAuthHandler = RedirectAuthHandler.create(authProvider);

    // create a chain
    chain = ChainAuthHandler.create();

    chain
      .append(JWTAuthHandler.create(null))
      .append(BasicAuthHandler.create(authProvider))
      .append(redirectAuthHandler);

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

  @Test
  public void testWithBasicAuthAsLastHandlerInChain() throws Exception {
    // after removing the RedirectAuthHandler, we check if the chain correctly returns the WWW-Authenticate Header, since now the BasicAuthHandler is the last handler in the chain
    chain.remove(redirectAuthHandler);
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcX=="), resp -> assertEquals("Basic realm=\"vertx-web\"", resp.getHeader("WWW-Authenticate")),401, "Unauthorized", "Unauthorized");
    chain.append(redirectAuthHandler); // given we cannot guarantee test order, we append the redirectAuthHandler again
  }
}
