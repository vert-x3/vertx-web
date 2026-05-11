package io.vertx.ext.web.proxy.tests;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class WebProxyTestBase extends WebTestBase {

  protected HttpServer backendServer;
  protected HttpClient proxyClient;
  protected Router backendRouter;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    super.setUp(vertx);
    backendRouter = Router.router(vertx);
    backendServer = vertx.createHttpServer(getBackendServerOptions());
    proxyClient = vertx.createHttpClient(getProxyClientOptions());
    backendServer.requestHandler(backendRouter).listen().await();
  }

  protected HttpServerOptions getBackendServerOptions() {
    return new HttpServerOptions().setPort(1234).setHost("localhost");
  }

  protected HttpClientOptions getProxyClientOptions() {
    return new HttpClientOptions();
  }

  @Override
  @AfterEach
  public void tearDown() throws Exception {
    if (proxyClient != null) {
      proxyClient.close().await();
    }
    if (backendServer != null) {
      backendServer.close().await();
    }
    super.tearDown();
  }
}
