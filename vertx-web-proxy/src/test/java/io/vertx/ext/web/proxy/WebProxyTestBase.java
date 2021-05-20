package io.vertx.ext.web.proxy;


import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.WebTestBase;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class WebProxyTestBase extends WebTestBase {

  protected HttpServer backendServer;
  protected HttpClient proxyClient;
  protected Router backendRouter;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    backendRouter = Router.router(vertx);
    backendServer = vertx.createHttpServer(getBackendServerOptions());
    proxyClient = vertx.createHttpClient(getProxyClientOptions());
    CountDownLatch latch = new CountDownLatch(1);
    backendServer.requestHandler(backendRouter).listen(onSuccess(res -> latch.countDown()));
    awaitLatch(latch);
  }

  protected HttpServerOptions getBackendServerOptions() {
    return new HttpServerOptions().setPort(1234).setHost("localhost");
  }

  protected HttpClientOptions getProxyClientOptions() {
    return new HttpClientOptions();
  }

  @Override
  public void tearDown() throws Exception {
    if (proxyClient != null) {
      CountDownLatch latch = new CountDownLatch(1);
      proxyClient.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
      awaitLatch(latch);
    }
    if (client != null) {
      CountDownLatch latch = new CountDownLatch(1);
      client.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
      awaitLatch(latch);
    }
    if (server != null) {
      CountDownLatch latch = new CountDownLatch(1);
      server.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
      awaitLatch(latch);
    }
    if (backendServer != null) {
      CountDownLatch latch = new CountDownLatch(1);
      backendServer.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
      awaitLatch(latch);
    }
    super.tearDown();
  }

}
