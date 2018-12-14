package io.vertx.ext.web.handler.sockjs;

import java.util.concurrent.CountDownLatch;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.test.core.VertxTestBase;

public abstract class SockJSTestBase extends VertxTestBase {

  protected int numServers = 1;
  protected HttpClient client;
  protected HttpServer[] servers;
  protected SockJSHandler sockJSHandler;

  protected Router createRouter() {
    Router router = Router.router(vertx);
    router.route().handler(CookieHandler.create());
    router.route()
      .handler(SessionHandler.create(LocalSessionStore.create(vertx))
        .setNagHttps(false)
        .setSessionTimeout(60 * 60 * 1000));
    addHandlersBeforeSockJSHandler(router);
    return router;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    servers = new HttpServer[numServers];
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080).setKeepAlive(false));
    SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
    sockJSHandler = SockJSHandler.create(vertx, options);
    for (int i = 0;i < servers.length;i++) {
      Router router = createRouter();
      router.route("/test/*").handler(sockJSHandler);
      servers[i] = vertx
        .createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"))
        .requestHandler(router);
      CountDownLatch latch = new CountDownLatch(1);
      servers[i].listen(ar -> latch.countDown());
      awaitLatch(latch);
    }
  }

  protected void addHandlersBeforeSockJSHandler(Router router) {
    // no additional routing handlers by default
  }
}
