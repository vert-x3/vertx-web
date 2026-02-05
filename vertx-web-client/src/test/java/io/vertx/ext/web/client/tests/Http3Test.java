package io.vertx.ext.web.client.tests;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.ServerSSLOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientConfig;
import io.vertx.test.core.LinuxOrOsx;
import io.vertx.test.http.HttpTestBase;
import io.vertx.test.tls.Cert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(LinuxOrOsx.class)
public class Http3Test extends HttpTestBase {

  @Test
  public void smokeTest() {
    HttpServer server = vertx.createHttpServer(new HttpServerConfig()
        .setSsl(true)
        .setSslOptions(new ServerSSLOptions().setKeyCertOptions(Cert.SERVER_JKS.get()))
        .addVersion(HttpVersion.HTTP_3))
      .requestHandler(request -> {
        request.response().end();
      });
    server.listen(4043).await();
    WebClient client = WebClient.create(vertx, new WebClientConfig()
      .setVersions(List.of(HttpVersion.HTTP_3))
      .setSslOptions(new ClientSSLOptions().setTrustAll(true))
      .setDefaultHost("localhost")
      .setDefaultPort(4043));
    HttpResponse<Buffer> response = client.get("/test").send().await();
  }
}
