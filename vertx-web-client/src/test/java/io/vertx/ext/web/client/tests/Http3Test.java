package io.vertx.ext.web.client.tests;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.ServerSSLOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.test.tls.Cert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(VertxExtension.class)
public class Http3Test {

  @Test
  public void smokeTest(Vertx vertx) {
    HttpServer server = vertx.createHttpServer(
        new HttpServerConfig().setVersions(HttpVersion.HTTP_3),
        new ServerSSLOptions().setKeyCertOptions(Cert.SERVER_JKS.get()))
      .requestHandler(request -> {
        request.response().end();
      });
    server.listen(4043, "localhost").await();
    WebClient client = WebClient.create(vertx,
      new WebClientConfig()
        .setVersions(List.of(HttpVersion.HTTP_3))
        .setDefaultHost("localhost")
        .setDefaultPort(4043),
      new ClientSSLOptions().setTrustAll(true));
    HttpResponse<Buffer> response = client.get("/test").send().await();
  }
}
