package io.vertx.ext.web.tests.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.tests.WebTestBase2;
import io.vertx.ext.web.sstore.LocalSessionStore;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChainAuthHandlerAndTest extends WebTestBase2 {

  private AuthenticationProvider authProvider;
  protected ChainAuthHandler chain;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);

    authProvider = PropertyFileAuthentication.create(vertx, "login/loginusers.properties");
    AuthenticationHandler redirectAuthHandler = RedirectAuthHandler.create(authProvider);

    // create a chain
    chain = ChainAuthHandler.all()
      .add(JWTAuthHandler.create(null))
      .add(BasicAuthHandler.create(authProvider))
      .add(redirectAuthHandler);

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(chain);
    router.route().handler(ctx -> ctx.response().end());
  }

  @Test
  public void testWithoutAuthorization() throws Exception {
    // since there is no authorization header the final status code will be 401 since all handlers are required it will
    // fail at the very first one.
    testRequest(HttpMethod.GET, "/", 401, "Unauthorized");
  }

  @Test
  public void testWithAuthorization() throws Exception {
    // there is an authorization header, so it should be handled properly
    // however it will not be accepted as first we required a jwt and that is enough to fail
    testRequest(webClient.get("/").putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcw=="),401, "Unauthorized", "Unauthorized");
  }

  @Test
  public void testWithBadAuthorization() throws Exception {
    // there is an authorization header, but the token is invalid it should be processed by the basic auth
    testRequest(webClient.get("/").putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcX=="),401, "Unauthorized", "Unauthorized");
  }

  @Test
  public void testWithBasicAuthAsLastHandlerInChain() throws Exception {
    // after removing the RedirectAuthHandler, we check if the chain correctly returns the WWW-Authenticate Header, since now the BasicAuthHandler is the last handler in the chain
    router.clear();

    // create a chain
    chain = ChainAuthHandler.all()
      .add(JWTAuthHandler.create(null))
      .add(BasicAuthHandler.create(authProvider));

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(chain);
    router.route().handler(ctx -> ctx.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcX==").send(), 401, "Unauthorized", "Unauthorized");
    assertEquals("Basic realm=\"vertx-web\"", resp.getHeader("WWW-Authenticate"));
  }
}
