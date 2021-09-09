package io.vertx.ext.web.client;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

public class WebClientSessionOauth2Test extends WebClientTestBase {

  private static final JsonObject fixture = new JsonObject(
    "{" +
      "  \"access_token\": \"4adc339e0\"," +
      "  \"refresh_token\": \"ec1a59d298\"," +
      "  \"token_type\": \"bearer\"," +
      "  \"expires_in\": 7200" +
      "}");

  private static final Oauth2Credentials oauthConfig = new Oauth2Credentials();

  @Test
  public void testRequestHeaders() throws Exception {
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

    requestAndverifyResponse.get().compose(v -> requestAndverifyResponse.get()).onSuccess(resp -> complete())
      .onFailure(this::fail);
    await(20, TimeUnit.SECONDS);
  }

  @Test
  public void testWithAuthentication() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

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
    }).listen(8080, ready -> {
      if (ready.failed()) {
        throw new RuntimeException(ready.cause());
      }
      // ready
      latch.countDown();
    });

    awaitLatch(latch);

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setFlow(OAuth2FlowType.CLIENT)
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    WebClientOAuth2 webClientOAuth2 =
      WebClientOAuth2.create(WebClientSession.create(webClient), oauth2);

    final CountDownLatch latchClient = new CountDownLatch(1);

    webClientOAuth2
      .withCredentials(oauthConfig)
      .get(8080, "localhost", "/protected/path")
      .send(result -> {
        if (result.failed()) {
          fail(result.cause());
        } else {
          assertEquals(200, result.result().statusCode());
          latchClient.countDown();
        }
      });

    awaitLatch(latchClient);
  }

  @Test
  public void testWithAuthenticationWithoutSession() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

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
    }).listen(8080, ready -> {
      if (ready.failed()) {
        throw new RuntimeException(ready.cause());
      }
      // ready
      latch.countDown();
    });

    awaitLatch(latch);

    OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
      .setFlow(OAuth2FlowType.CLIENT)
      .setClientId("client-id")
      .setClientSecret("client-secret")
      .setSite("http://localhost:8080"));

    WebClientOAuth2 webClientOAuth2 =
      WebClientOAuth2.create(webClient, oauth2);

    final CountDownLatch latchClient = new CountDownLatch(1);

    webClientOAuth2
      .withCredentials(oauthConfig)
      .get(8080, "localhost", "/protected/path")
      .send(result -> {
        if (result.failed()) {
          fail(result.cause());
        } else {
          assertEquals(200, result.result().statusCode());
          latchClient.countDown();
        }
      });

    awaitLatch(latchClient);
  }
}
