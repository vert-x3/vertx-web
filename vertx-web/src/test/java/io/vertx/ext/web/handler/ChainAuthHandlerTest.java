package io.vertx.ext.web.handler;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.htdigest.HtdigestAuth;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.junit.Test;

import java.util.List;

public class ChainAuthHandlerTest extends WebTestBase {

  private AuthenticationProvider authProvider;
  protected ChainAuthHandler chain;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    authProvider = PropertyFileAuthentication.create(vertx, "login/loginusers.properties");
    AuthenticationHandler redirectAuthHandler = RedirectAuthHandler.create(authProvider);

    // create a chain
    chain = ChainAuthHandler.any()
      .add(JWTAuthHandler.create(null))
      .add(BasicAuthHandler.create(authProvider))
      .add(redirectAuthHandler);

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
    // there is an authorization header, but the token is invalid it should be processed by the last handler (redirect)
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcX=="),302, "Found", null);
  }

  @Test
  public void testWithBasicAuthAsLastHandlerInChain() throws Exception {
    // after removing the RedirectAuthHandler, we check if the chain correctly returns the WWW-Authenticate Header, since now the BasicAuthHandler is the last handler in the chain
    router.clear();

    // create a chain
    chain = ChainAuthHandler.any()
      .add(JWTAuthHandler.create(null))
      .add(BasicAuthHandler.create(authProvider));

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(chain);
    router.route().handler(ctx -> ctx.response().end());

    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Authorization", "Basic dGltOmRlbGljaW91czpzYXVzYWdlcX=="), resp -> assertEquals("Basic realm=\"vertx-web\"", resp.getHeader("WWW-Authenticate")),401, "Unauthorized", "Unauthorized");
  }

  @Test
  public void testWithMultipleWWWAuthenticate() throws Exception {
    router.clear();

    // create a chain
    chain = ChainAuthHandler.any()
      .add(BasicAuthHandler.create(authProvider))
      .add(DigestAuthHandler.create(vertx, HtdigestAuth.create(vertx)));

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(chain);
    router.route().handler(ctx -> ctx.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      assertNotNull(resp.getHeader("WWW-Authenticate"));
      List<String> headers = resp.headers().getAll("WWW-Authenticate");
      assertNotNull(headers);
      assertEquals(2, headers.size());
      assertTrue(headers.get(0).startsWith("Basic realm=\"vertx-web\""));
      assertTrue(headers.get(1).startsWith("Digest realm=\"testrealm@host.com\""));
    },401, "Unauthorized", "Unauthorized");
  }

  @Test
  public void testWithPostAuthenticationAction() throws Exception {
    router.clear();

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    chain = ChainAuthHandler.any()
      // Direct login is implemented as a post-authentication action
      .add(FormLoginHandler.create(authProvider).setDirectLoggedInOKURL("/welcome"));

    router.post("/login")
      .handler(BodyHandler.create())
      .handler(chain);

    testRequest(HttpMethod.POST, "/login", req -> {
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String str =
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + FormLoginHandler.DEFAULT_USERNAME_PARAM + "\"\r\n\r\ntim\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + FormLoginHandler.DEFAULT_PASSWORD_PARAM + "\"\r\n\r\ndelicious:sausages\r\n" +
        "--" + boundary + "--\r\n";
      buffer.appendString(str);
      req.putHeader("content-length", String.valueOf(buffer.length()));
      req.putHeader("content-type", "multipart/form-data; boundary=" + boundary);
      req.write(buffer);
    }, resp -> {
      MultiMap headers = resp.headers();
      // session will be upgraded
      String setCookie = headers.get("set-cookie");
      assertNotNull(setCookie);
      // client will be redirected
      assertTrue(headers.contains(HttpHeaders.LOCATION, "/welcome", false));
    }, 302, "Found", null);
  }

  @Test
  public void testScopesEnforcedInAllChain() throws Exception {
    router.clear();

    JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    chain = ChainAuthHandler.all()
      .add(JWTAuthHandler.create(jwtAuth).withScope("admin"))
      .add(JWTAuthHandler.create(jwtAuth).withScope("superuser"));

    router.route()
      .handler(chain)
      .handler(RoutingContext::end);

    // user has both scopes -> 200
    JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "admin superuser");
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadA)), 200, "OK", null);

    // user has only one scope -> 403
    JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "admin");
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadB)), 403, "Forbidden", null);
  }

  @Test
  public void testScopesEnforcedForPreAuthenticatedUserInAnyChain() throws Exception {
    router.clear();

    JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    // Parent route: authenticate without scope requirement
    router.route().handler(JWTAuthHandler.create(jwtAuth));

    // Child route: chain with scope requirement
    chain = ChainAuthHandler.any()
      .add(JWTAuthHandler.create(jwtAuth).withScope("admin"));

    router.route("/protected/*")
      .handler(chain)
      .handler(RoutingContext::end);

    // Token with scope "read" -> expect 403
    JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "read");
    testRequest(HttpMethod.GET, "/protected/resource", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadA)), 403, "Forbidden", null);

    // Token with scope "admin" -> expect 200
    JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "admin");
    testRequest(HttpMethod.GET, "/protected/resource", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadB)), 200, "OK", null);
  }

  @Test
  public void testScopesEnforcedInAllChainForPreAuthenticatedUser() throws Exception {
    router.clear();

    JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    // Parent route: authenticate without scope requirement
    router.route().handler(JWTAuthHandler.create(jwtAuth));

    // Child route: chain requiring both scopes
    chain = ChainAuthHandler.all()
      .add(JWTAuthHandler.create(jwtAuth).withScope("admin"))
      .add(JWTAuthHandler.create(jwtAuth).withScope("superuser"));

    router.route("/protected/*")
      .handler(chain)
      .handler(RoutingContext::end);

    // Token with both scopes -> expect 200
    JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "admin superuser");
    testRequest(HttpMethod.GET, "/protected/resource", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadA)), 200, "OK", null);

    // Token with only one scope -> expect 403
    JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "admin");
    testRequest(HttpMethod.GET, "/protected/resource", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadB)), 403, "Forbidden", null);
  }

  @Test
  public void testAnyChainWithNonScopedHandlerPassesThroughForPreAuthenticatedUser() throws Exception {
    router.clear();

    JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    // Parent route: authenticate without scope requirement
    router.route().handler(JWTAuthHandler.create(jwtAuth));

    // Child route: chain with only a non-scoped handler (BasicAuthHandler)
    chain = ChainAuthHandler.any()
      .add(BasicAuthHandler.create(authProvider));

    router.route("/protected/*")
      .handler(chain)
      .handler(RoutingContext::end);

    // Pre-authenticated JWT user -> expect 200 (no ScopedAuthentication handler in chain)
    JsonObject payload = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "read");
    testRequest(HttpMethod.GET, "/protected/resource", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payload)), 200, "OK", null);
  }

  @Test
  public void testAnyChainWithMixedHandlersEnforcesScopesForPreAuthenticatedUser() throws Exception {
    router.clear();

    JWTAuth jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    // Parent route: authenticate without scope requirement
    router.route().handler(JWTAuthHandler.create(jwtAuth));

    // Child route: chain with BasicAuthHandler (non-scoped) and JWT with scope
    chain = ChainAuthHandler.any()
      .add(BasicAuthHandler.create(authProvider))
      .add(JWTAuthHandler.create(jwtAuth).withScope("admin"));

    router.route("/protected/*")
      .handler(chain)
      .handler(RoutingContext::end);

    // Token with scope "read" -> expect 403
    JsonObject payloadA = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "read");
    testRequest(HttpMethod.GET, "/protected/resource", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadA)), 403, "Forbidden", null);

    // Token with scope "admin" -> expect 200
    JsonObject payloadB = new JsonObject()
      .put("sub", "Paulo")
      .put("scope", "admin");
    testRequest(HttpMethod.GET, "/protected/resource", req -> req.putHeader("Authorization", "Bearer " + jwtAuth.generateToken(payloadB)), 200, "OK", null);
  }

}
