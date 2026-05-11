package io.vertx.ext.web.tests.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.auth.properties.PropertyFileAuthorization;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.sstore.SessionStore;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

public class BasicAuthImpersonationTest extends WebTestBase {

  AuthenticationProvider authn;
  AuthorizationProvider authz;

  private static final String USER_SWITCH_KEY = "__vertx.user-switch-ref";

  @Override
  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    super.setUp(vertx);
    authn = PropertyFileAuthentication.create(vertx, "login/loginusers.properties");
    authz = PropertyFileAuthorization.create(vertx, "login/loginusers.properties");
  }

  @Test
  public void testSwitchUser() throws Exception {

    /////////////////////////
    // SETUP
    /////////////////////////

    // keep state
    router.route()
      .handler(SessionHandler.create(SessionStore.create(vertx)));

    // switch users setup
    // there are 2 routes for testing purposes
    router.route("/user-switch/impersonate")
      // this is a high precedence handler
      .handler(ctx -> {
        ctx.userContext()
          .loginHint(ctx.request().getParam("login_hint"))
          .impersonate(ctx.request().getParam("redirect_uri"))
          .onFailure(err -> {
            if (err instanceof HttpException) {
              ctx.fail(err);
            } else {
              ctx.fail(500);
            }
          });
      });

    router.route("/user-switch/undo")
      // this is a high precedence handler
      .handler(ctx -> {
        ctx.userContext()
          .loginHint(ctx.request().getParam("login_hint"))
          .restore(ctx.request().getParam("redirect_uri"))
          .onFailure(err -> {
            if (err instanceof HttpException) {
              ctx.fail(err);
            } else {
              ctx.fail(500);
            }
          });
      });

    // protect everything under /protected
    router.route("/protected/*")
      .handler(BasicAuthHandler.create(authn));

    final AtomicReference<User> userRef = new AtomicReference<>();

    // mount 1st handler under the protected zone (regular user only can read)
    router
      .route("/protected/base")
      .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("read")).addAuthorizationProvider(authz))
      .handler(rc -> {
        assertNotNull(rc.user());
        userRef.set(rc.user());
        rc.end("OK");
      });


    // mount 2nd handler under the protected zone (admin user can write)
    router
      .route("/protected/admin")
      .handler(AuthorizationHandler.create(RoleBasedAuthorization.create("write")).addAuthorizationProvider(authz))
      .handler(rc -> {
        assertNotNull(rc.user());

        // assert that the old and new users are not the same
        User oldUser = userRef.get();
        assertNotNull(oldUser);
        User newUser = rc.user();
        assertFalse(oldUser.equals(newUser));

        // also the old user should be in the session
        User prevUser = rc.session().get(USER_SWITCH_KEY);
        assertNotNull(prevUser);
        assertEquals(prevUser, oldUser);

        rc.response().end("Welcome to the 2nd protected resource!");
      });


    /////////////////////////
    // TEST
    /////////////////////////

    // flow:
    // 1. user not authenticated
    // 2. app starts a redirect to the IdP
    // 3. IdP calls back, user gets to the desired endpoint

    final AtomicReference<String> sessionRef = new AtomicReference<>();

    // 1. user isn't authenticated (no Authorization header, no Session cookie)
    // Expectation:
    //   * A redirect to the IdP, as we're mocking, we need to extract the state of the redirect URL so we can fake the
    //     callback to the app
    //   * We also need to have a session cookie otherwise we lose all the context and cannot have multiple identities
    HttpResponse<Buffer> resp = testRequest(webClient.get("/protected/base").send(), 401, "Unauthorized");
    // in this case we should get a WWW-Authenticate
    String redirectURL = resp.getHeader("WWW-Authenticate");
    assertNotNull(redirectURL);
    // there's no session yet
    String setCookie = resp.headers().get("set-cookie");
    assertNull(setCookie);

    // 3. fake the redirect from the IdP. This happens with a success authn validation, we need to pass the right state
    // Expectations:
    //   * A new session cookie is returned, as the session id is regenerated to prevent replay attacks or privilege
    //     escalation bugs. Old session assumed an un authenticated user, this one is for the authenticated one
    //   * A final redirect happens to avoid caching the callback URL at the user-agent, so the browser will show
    //     the desired original URL
    resp = testRequest(webClient.get("/protected/base")
      .putHeader(HttpHeaders.AUTHORIZATION.toString(), "Basic cmVndWxhcjpyZWd1bGFy")
      .send(), 200, "OK");
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));

    // 4. Confirm that we can get the secured resource
    testRequest(webClient.get("/protected/base")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 200, "OK", "OK");

    //////////////////////////////
    // TEST SWITCHING IDENTITIES
    /////////////////////////////

    // test we can't get the admin resource (we're still base user)
    testRequest(webClient.get("/protected/admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 403, "Forbidden");

    // verify that the switch isn't possible for non authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     an Unauthorized response
    testRequest(webClient.get("/user-switch/impersonate?redirect_uri=/protected/admin&login_hint=admin")
      .send(), 401, "Unauthorized");

    // start the switch

    // flow:
    // 1. call the switch user endpoint
    // 2. a new Oauth2 auth flow starts like before
    // 3. In the end there should be a new user object and the previous one shall be in the session

    // User is authenticated (there is a session and a User) and a redirect to the IdP should happen
    // Expectations:
    //   * A redirect to the IdP should happen. (maybe there's a way to hint the desired user? This doesn't do it)
    resp = testRequest(webClient.get("/user-switch/impersonate?redirect_uri=/protected/admin&login_hint=admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // in this case we should get a redirect, and the session id must change
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    // the session must change
    assertFalse(setCookie.substring(0, setCookie.indexOf(';')).equals(sessionRef.get()));
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
    String destination = resp.getHeader(HttpHeaders.LOCATION.toString());
    assertNotNull(destination);

    // verify that the switch isn't possible for non authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     a redirect to the IdP response
    testRequest(webClient.get("/protected/admin")
      .send(), 401, "Unauthorized");

    // verify that the switch is possible for authn requests
    // Expectations:
    //   * Given that there is no cookie and no authorization header, no user will be present in the request, forcing
    //     a redirect to the IdP response
    resp = testRequest(webClient.get("/protected/admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 401, "Unauthorized");
    // in this case we should get a WWW-Authenticate
    redirectURL = resp.getHeader("WWW-Authenticate");
    assertNotNull(redirectURL);
    // there's no session yet
    setCookie = resp.headers().get("set-cookie");
    assertNull(setCookie);

    // user is authenticated, it now escalates the permissions by re-doing the auth flow to upgrade the user
    // Expectations:
    //   * fake the IdP callback with the right state
    //   * like before ensure that the session id changes (base user -> admin user)
    //   * final redirect to the desired target resource, to avoid user-agents to cache the callback url
    resp = testRequest(webClient.get("/protected/admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .putHeader(HttpHeaders.AUTHORIZATION.toString(), "Basic YWRtaW46YWRtaW4=")
      .send(), 200, "OK");
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));

    ////////////////////////////////////////
    // TEST GET RESOURCE WITH NEW IDENTITY
    ////////////////////////////////////////

    // final call to verify that the desired escalated user can get the final resource
    testRequest(webClient.get("/protected/admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 200, "OK", "Welcome to the 2nd protected resource!");

    ////////////////////////////////////////
    // UNDO IMPERSONATION
    ////////////////////////////////////////

    resp = testRequest(webClient.get("/user-switch/undo?redirect_uri=/protected/base")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .followRedirects(false)
      .send(), 302, "Found");
    // in this case we should get a redirect, and the session id must change
    // session upgrade (secure against replay attacks)
    setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);
    // the session must change
    assertFalse(setCookie.substring(0, setCookie.indexOf(';')).equals(sessionRef.get()));
    sessionRef.set(setCookie.substring(0, setCookie.indexOf(';')));
    destination = resp.getHeader(HttpHeaders.LOCATION.toString());
    assertNotNull(destination);

    // final call to verify that the desired de-escalated user can get the final resource
    testRequest(webClient.get("/protected/base")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 200, "OK", "OK");

    // final call to verify that the desired de-escalated user cannot get the admin resource
    testRequest(webClient.get("/protected/admin")
      .putHeader(HttpHeaders.COOKIE.toString(), sessionRef.get())
      .send(), 403, "Forbidden");
  }
}
