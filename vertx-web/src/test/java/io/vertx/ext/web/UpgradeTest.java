package io.vertx.ext.web;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.handler.*;
import io.vertx.test.core.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class UpgradeTest {

  @Rule
  public final RunTestOnContext rule = new RunTestOnContext();

  final Router router = Router.router(rule.vertx());

  HttpServer server;
  HttpClient client;

  @Before
  public void setup(TestContext should) {
    final Async setup = should.async();
    rule.vertx()
      .createHttpServer()
      .requestHandler(router)
      .listen(0)
      .onSuccess(server -> {
        this.server = server;
        this.client = rule.vertx()
          .createHttpClient(new HttpClientOptions().setDefaultPort(server.actualPort()).setDefaultHost("localhost"));
        setup.complete();
      })
      .onFailure(should::fail);
  }

  @Test
  public void testUpgradeWithAsyncInBetween(TestContext should) {
    final Async test = should.async();
    router.route()
      .handler((PlatformHandler) ctx -> {
        ctx.request().pause();
        // delay 5ms to ensure we don't run sequentially
        rule.vertx().setTimer(5L, v -> {
          ctx.request().resume();
          ctx.next();
        });
      })
      .handler((PlatformHandler) ctx -> {
        ctx.request().pause();
        // delay 5ms to ensure we don't run sequentially
        rule.vertx().setTimer(5L, v -> {
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

    client.webSocket("/")
      .onFailure(should::fail)
      .onSuccess(webSocket -> {
        webSocket.frameHandler(System.out::println);
        webSocket.closeHandler(ok -> test.complete());
      });
  }

  @Test
  public void testUpgradeWithLongAwait(TestContext should) {
    final Async test = should.async();
    router.route()
      .handler((PlatformHandler) ctx -> {
        ctx.request().pause();

        ctx.response().removeCookie("session", false);
        // delay 5ms to ensure we don't run sequentially
        rule.vertx().setTimer(500L, v -> {
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

    client.webSocket(options)
      .onFailure(should::fail)
      .onSuccess(webSocket -> {
        webSocket.frameHandler(System.out::println);
        webSocket.closeHandler(ok -> test.complete());
      });
  }
}
