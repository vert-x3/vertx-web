package io.vertx.serviceproxy;

import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;

public class SockJSProxyTestBase extends VertxTestBase {
  private HttpServer server;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // create bridge for sockjs-client proxy messages
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions allAccessOptions =
      new BridgeOptions().addInboundPermitted(new PermittedOptions()).addOutboundPermitted(new PermittedOptions());
    sockJSHandler.bridge(allAccessOptions);
    Router router = Router.router(vertx);
    router.route("/eventbus/*").handler(sockJSHandler);
    HttpServerOptions serverOptions = new HttpServerOptions().setPort(8080).setHost("localhost");
    server = vertx.createHttpServer(serverOptions);
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router::accept).listen(onSuccess(res -> latch.countDown()));
    awaitLatch(latch);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    CountDownLatch latch = new CountDownLatch(1);
    server.close(res -> {
      assertTrue(res.succeeded());
      latch.countDown();
    });
  }
}
