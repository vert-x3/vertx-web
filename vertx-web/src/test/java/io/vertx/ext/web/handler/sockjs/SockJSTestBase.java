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

  protected HttpClient client;
  protected HttpServer server;
  protected Router router;
  protected SockJSHandler sockJSHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080));
    router = Router.router(vertx);
    router.route().handler(CookieHandler.create());
    router.route()
        .handler(SessionHandler.create(LocalSessionStore.create(vertx))
            .setNagHttps(false)
            .setSessionTimeout(60 * 60 * 1000));
    addHandlersBeforeSockJSHandler(router);
    SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
    sockJSHandler = SockJSHandler.create(vertx, options);
    router.route("/test/*").handler(sockJSHandler);
    server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router).listen(ar -> latch.countDown());
    awaitLatch(latch);
  }

  protected void addHandlersBeforeSockJSHandler(Router router) {
    // no additional routing handlers by default
  }
}
