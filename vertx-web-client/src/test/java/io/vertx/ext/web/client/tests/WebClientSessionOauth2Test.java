package io.vertx.ext.web.client.tests;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.OAuth2WebClient;
import io.vertx.ext.web.client.OAuth2WebClientOptions;
import io.vertx.ext.web.client.WebClientSession;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.*;

public class WebClientSessionOauth2Test extends WebClientTestBase {

  private static final JsonObject fixture = new JsonObject(
    "{" +
      "  \"access_token\": \"4adc339e0\"," +
      "  \"refresh_token\": \"ec1a59d298\"," +
      "  \"token_type\": \"bearer\"," +
      "  \"expires_in\": 7200" +
      "}");

  private static final JsonObject fixtureExpires = new JsonObject(
    "{" +
      "  \"access_token\": \"4adc339e0\"," +
      "  \"refresh_token\": \"ec1a59d298\"," +
      "  \"token_type\": \"bearer\"," +
      "  \"expires_in\": 1" +
      "}");

  private static final JsonObject loggedOutFixture = new JsonObject(
    "{" +
      "  \"access_token\": \"1ghs4sq2e\"," +
      "  \"refresh_token\": \"kjt62s3asw5\"," +
      "  \"token_type\": \"bearer\"," +
      "  \"expires_in\": 7200" +
      "}");

  private static final Oauth2Credentials oauthConfig = new Oauth2Credentials().setFlow(OAuth2FlowType.CLIENT);

  @Test
  public void testRequestHeaders() {
    WebClientSession session = WebClientSession.create(webClient).addHeader(AUTHORIZATION, "v3rtx");
    HttpRequest<Buffer> request = session.get(DEFAULT_TEST_URI);

    server.requestHandler(serverRequest -> {
      int authHeaderCount = serverRequest.headers().getAll(AUTHORIZATION).size();
      serverRequest.response().end(Integer.toString(authHeaderCount));
    });

    startServer();

    Supplier<Future<Void>> requestAndverifyResponse = () -> request.send()
      .compose(response -> "1".equals(response.bodyAsString()) ? succeededFuture()
        : failedFuture("Request contains Authorization header multiple times " + response.bodyAsString()));

    requestAndverifyResponse.get().compose(v -> requestAndverifyResponse.get()).await();
  }

  @Test
  public void testWithAuthentication() {
    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
        assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
        req.response().putHeader("Content-Type", "application/json").end(fixture.encode());
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path())) {
        assertEquals("Bearer " + fixture.getString("access_token"), req.getHeader("Authorization"));
        req.response().end();
      } else {
        req.response().setStatusCode(400).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(WebClientSession.create(webClient), oauth2);

    HttpResponse<Buffer> result = oauth2WebClient
      .withCredentials(oauthConfig)
      .get(8080, "localhost", "/protected/path")
      .send().await();

    assertEquals(200, result.statusCode());
    assertEquals(fixture.getString("access_token"), oauth2WebClient.getUser().principal().getString("access_token"));
  }

  @Test
  public void testWithAuthenticationWithoutSession() {
    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
        assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
        req.response().putHeader("Content-Type", "application/json").end(fixture.encode());
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path())) {
        assertEquals("Bearer " + fixture.getString("access_token"), req.getHeader("Authorization"));
        req.response().end();
      } else {
        req.response().setStatusCode(400).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(webClient, oauth2);

    HttpResponse<Buffer> result = oauth2WebClient
      .withCredentials(oauthConfig)
      .get(8080, "localhost", "/protected/path")
      .send().await();

    assertEquals(200, result.statusCode());
  }

  @Test
  public void testWithoutAuthenticationWithoutSession() {
    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(webClient, oauth2);

    try {
      oauth2WebClient
        .get(8080, "localhost", "/protected/path")
        .send().await();
      fail("Should require credentials");
    } catch (Exception ignored) {
    }
  }

  @Test
  public void testWithAuthenticationWithoutSession2() {
    final AtomicInteger counter = new AtomicInteger(0);

    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
        if (counter.incrementAndGet() == 2) {
          fail("Should only request a token 1 time");
        } else {
          assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
          req.response().putHeader("Content-Type", "application/json").end(fixture.encode());
        }
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path())) {
        assertEquals("Bearer " + fixture.getString("access_token"), req.getHeader("Authorization"));
        req.response().end();
      } else {
        req.response().setStatusCode(400).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(webClient, oauth2);

    oauth2WebClient
      .withCredentials(oauthConfig);

    HttpResponse<Buffer> result1 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result1.statusCode());

    // again, but this time we should not get a token
    HttpResponse<Buffer> result2 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result2.statusCode());
  }

  @Test
  public void testWithAuthenticationWithoutSessionExpired() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);

    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
        if (counter.incrementAndGet() == 3) {
          fail("Should only request a token 2 times");
        } else {
          assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
          req.response().putHeader("Content-Type", "application/json").end(fixtureExpires.copy().put("calls", counter).encode());
        }
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path())) {
        assertEquals("Bearer " + fixtureExpires.getString("access_token"), req.getHeader("Authorization"));
        req.response().end();
      } else {
        req.response().setStatusCode(400).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(webClient, oauth2);

    oauth2WebClient
      .withCredentials(oauthConfig);

    HttpResponse<Buffer> result1 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result1.statusCode());

    // sleep so the user expires
    Thread.sleep(2000L);

    // again, but this time we should get a new token
    HttpResponse<Buffer> result2 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result2.statusCode());
  }

  @Test
  public void testWithAuthenticationWithoutSessionExpiredFailsRefreshForceReauthentication() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);

    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
        if (counter.incrementAndGet() == 4) {
          fail("Should only request a token 3 times");
        } else {
          assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
          if (counter.get() == 2) {
            // fake a bad refresh response
            req.response().setStatusCode(401).end();
          } else {
            req.response().putHeader("Content-Type", "application/json").end(fixtureExpires.copy().put("calls", counter).encode());
          }
        }
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path())) {
        assertEquals("Bearer " + fixtureExpires.getString("access_token"), req.getHeader("Authorization"));
        req.response().end();
      } else {
        req.response().setStatusCode(400).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(webClient, oauth2);

    oauth2WebClient
      .withCredentials(oauthConfig);

    HttpResponse<Buffer> result1 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result1.statusCode());

    // sleep so the user expires
    Thread.sleep(2000L);

    // again
    HttpResponse<Buffer> result2 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result2.statusCode());
  }

  @Test
  public void testWithAuthenticationWithoutSessionExpiredWithLeeway() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);

    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
        if (counter.incrementAndGet() == 2) {
          fail("Should only request a token 1 time");
        } else {
          assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
          req.response().putHeader("Content-Type", "application/json").end(fixtureExpires.copy().put("calls", counter).encode());
        }
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path())) {
        assertEquals("Bearer " + fixtureExpires.getString("access_token"), req.getHeader("Authorization"));
        req.response().end();
      } else {
        req.response().setStatusCode(400).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(webClient, oauth2, new OAuth2WebClientOptions().setLeeway(5));

    oauth2WebClient
      .withCredentials(oauthConfig);

    HttpResponse<Buffer> result1 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result1.statusCode());

    // sleep so the user expires
    Thread.sleep(2000L);

    // again, but this time we should not get a token (leeway covers it)
    HttpResponse<Buffer> result2 = oauth2WebClient
      .get(8080, "localhost", "/protected/path")
      .send().await();
    assertEquals(200, result2.statusCode());
  }

  @Test
  public void tokenInvalidatedByProvider() {
    final AtomicBoolean retry = new AtomicBoolean();

    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path()) && !retry.get()) {
        assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
        req.response().putHeader("Content-Type", "application/json").end(loggedOutFixture.encode());
      } else if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path()) && retry.get()) {
        assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
        req.response().putHeader("Content-Type", "application/json").end(fixture.encode());
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path()) && retry.get()) {
        assertEquals("Bearer " + fixture.getString("access_token"), req.getHeader("Authorization"));
        req.response().end();
      } else {
        retry.set(true);
        req.response().setStatusCode(401).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(
        WebClientSession.create(webClient),
        oauth2,
        new OAuth2WebClientOptions().setRenewTokenOnForbidden(true));

    HttpResponse<Buffer> result = oauth2WebClient
      .withCredentials(oauthConfig)
      .get(8080, "localhost", "/protected/path")
      .send().await();

    assertEquals(200, result.statusCode());
  }

  @Test
  public void tokenInvalidatedByProviderAlways401() {
    final AtomicBoolean retry = new AtomicBoolean();

    server = vertx.createHttpServer().requestHandler(req -> {
      if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path()) && !retry.get()) {
        assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
        req.response().putHeader("Content-Type", "application/json").end(loggedOutFixture.encode());
      } else if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path()) && retry.get()) {
        assertEquals("Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=", req.getHeader("Authorization"));
        req.response().putHeader("Content-Type", "application/json").end(fixture.encode());
      } else if (req.method() == HttpMethod.GET && "/protected/path".equals(req.path())) {
        retry.set(true);
        req.response().setStatusCode(401).end();
      }
    });

    server.listen(8080).await();

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    OAuth2WebClient oauth2WebClient =
      OAuth2WebClient.create(
        WebClientSession.create(webClient),
        oauth2,
        new OAuth2WebClientOptions().setRenewTokenOnForbidden(true));

    HttpResponse<Buffer> result = oauth2WebClient
      .withCredentials(oauthConfig)
      .get(8080, "localhost", "/protected/path")
      .send().await();

    // this one will fail as we fail to refresh request after request
    assertEquals(401, result.statusCode());
  }
}
