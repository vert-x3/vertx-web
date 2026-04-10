package io.vertx.ext.web.tests;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import io.vertx.junit5.VertxTestContext;
import io.vertx.test.core.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@VertxTest
public class UpgradeTest {

  Vertx vertx;
  final Router router;
  HttpServer server;
  HttpClient client;
  WebSocketClient wsClient;

  public UpgradeTest(Vertx vertx) {
    this.vertx = vertx;
    this.router = Router.router(vertx);
  }

  @BeforeEach
  public void setup() throws Exception {
    server = vertx
      .createHttpServer()
      .requestHandler(router);
    server.listen(0).await(20, TimeUnit.SECONDS);
    client = vertx.createHttpClient(
      new HttpClientOptions()
        .setDefaultPort(server.actualPort())
        .setDefaultHost("localhost"));
    wsClient = vertx.createWebSocketClient(
      new WebSocketClientOptions()
        .setDefaultPort(server.actualPort())
        .setDefaultHost("localhost"));
  }

  @Test
  public void testUpgradeWithAsyncInBetween(VertxTestContext testContext) {
    Checkpoint done = testContext.checkpoint();
    router.route()
      .handler((PlatformHandler) ctx -> {
        ctx.request().pause();
        // delay 5ms to ensure we don't run sequentially
        vertx.setTimer(5L, v -> {
          ctx.request().resume();
          ctx.next();
        });
      })
      .handler((PlatformHandler) ctx -> {
        ctx.request().pause();
        // delay 5ms to ensure we don't run sequentially
        vertx.setTimer(5L, v -> {
          ctx.request().resume();
          ctx.next();
        });
      })
      .handler((ProtocolUpgradeHandler) ctx -> {
        ctx.request()
          .toWebSocket()
          .onFailure(ctx::fail)
          .onSuccess(webSocket -> {
            webSocket.write(Buffer.buffer("OK"))
              .onSuccess(ok -> webSocket.close())
              .onFailure(ctx::fail);
          });
      });

    wsClient.connect("/")
      .onFailure(testContext::failNow)
      .onSuccess(webSocket -> {
        webSocket.frameHandler(System.out::println);
        webSocket.closeHandler(ok -> done.flag());
      });
  }

  @Test
  public void testUpgradeWithLongAwait(VertxTestContext testContext) {
    Checkpoint done = testContext.checkpoint();
    router.route()
      .handler((PlatformHandler) ctx -> {
        ctx.request().pause();

        ctx.response().removeCookie("session", false);
        // delay 5ms to ensure we don't run sequentially
        vertx.setTimer(500L, v -> {
          ctx.request().resume();
          ctx.next();
        });
      })
      .handler((ProtocolUpgradeHandler) ctx -> {
        ctx.request()
          .toWebSocket()
          .onFailure(ctx::fail)
          .onSuccess(webSocket -> {
            webSocket.write(Buffer.buffer("OK"))
              .onSuccess(ok -> webSocket.close())
              .onFailure(ctx::fail);
          });
      });

    WebSocketConnectOptions options = new WebSocketConnectOptions()
      .setURI("/")
      .addHeader("cookie", "session=" + TestUtils.randomAlphaString(32));

    wsClient.connect(options)
      .onFailure(testContext::failNow)
      .onSuccess(webSocket -> {
        webSocket.frameHandler(System.out::println);
        webSocket.closeHandler(ok -> done.flag());
      });
  }
}
