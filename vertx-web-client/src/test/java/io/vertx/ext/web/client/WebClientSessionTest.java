package io.vertx.ext.web.client;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

public class WebClientSessionTest extends WebClientTestBase {

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
      .onFailure(t -> fail(t));
    await(20, TimeUnit.SECONDS);
  }

  @Test
  public void testWithAuthentication() {
    OpenIDConnectAuth.discover(vertx, new OAuth2Options()
        .setFlow(OAuth2FlowType.PASSWORD)
        .setSite("https://stage.ipification.com/auth/realms/ipification")
        .setClientId("admin-cli")
        .setJWTOptions(new JWTOptions()
          .setAudience(Collections.singletonList("admin-cli"))),
      handler -> {
        if (handler.succeeded()) {
          WebClientSession session = WebClientSession.create(WebClient.create(vertx), handler.result());
          session.withAuthentication(new JsonObject()
            .put("username", "test")
            .put("password", "secret"))
            .getAbs("https://hookb.in/RZZbOP6z16TNb9zzbD3G")
            .send(result -> {
              if(result.succeeded()) {
                complete();
              } else {
                result.cause().printStackTrace();
                fail(result.cause());
              }
            });
        } else {
          handler.cause().printStackTrace();
          fail(handler.cause());
        }
      });
    await(20, TimeUnit.SECONDS);
  }
}
