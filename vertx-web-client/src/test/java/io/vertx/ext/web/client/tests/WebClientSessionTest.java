package io.vertx.ext.web.client.tests;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClientSession;
import org.junit.jupiter.api.Test;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

import java.util.function.Supplier;

public class WebClientSessionTest extends WebClientJUnit5TestBase {

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
}
