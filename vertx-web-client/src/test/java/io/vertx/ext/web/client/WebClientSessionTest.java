package io.vertx.ext.web.client;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Test;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

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
    await(200, TimeUnit.SECONDS);
  }
}
