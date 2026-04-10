package io.vertx.ext.web.tests;

import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.junit5.VertxTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@VertxTest
public class Router100ContinueTest {

  Vertx vertx;
  Router router;
  HttpServer server;
  HttpClient client;

  @BeforeEach
  public void setup(Vertx vertx) throws Exception {
    this.vertx = vertx;
    this.router = Router.router(vertx);
    server = vertx
      .createHttpServer()
      .requestHandler(router);
    server.listen(0).await(20, TimeUnit.SECONDS);
    client = vertx
      .createHttpClient(new HttpClientOptions().setDefaultPort(server.actualPort()).setDefaultHost("localhost"));
  }

  @Test
  public void testContinue(VertxTestContext testContext) {
    router.route()
      .handler(BodyHandler.create())
      .handler(ctx -> {
        assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onFailure(testContext::failNow)
      .onSuccess(req -> {
        req
          .response()
          .onFailure(testContext::failNow)
          .onSuccess(res -> {
            assertEquals(200, res.statusCode());
            testContext.completeNow();
          });

        req
          .putHeader(HttpHeaders.EXPECT, "100-continue")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onFailure(testContext::failNow))
          .sendHead()
          .onFailure(testContext::failNow);
      });
  }

  @Test
  public void testBadExpectation(VertxTestContext testContext) {
    router.route()
      .handler(BodyHandler.create())
      .handler(ctx -> {
        assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onFailure(testContext::failNow)
      .onSuccess(req -> {
        req
          .response()
          .onFailure(testContext::failNow)
          .onSuccess(res -> {
            assertEquals(417, res.statusCode());
            testContext.completeNow();
          });

        req
          .putHeader(HttpHeaders.EXPECT, "lets-go")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onFailure(testContext::failNow))
          .sendHead()
          .onFailure(testContext::failNow);
      });
  }

  @Test
  public void testExpectButTooLarge(VertxTestContext testContext) {
    router.route()
      .handler(BodyHandler.create().setBodyLimit(1))
      .handler(ctx -> {
        assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onFailure(testContext::failNow)
      .onSuccess(req -> {
        req
          .response()
          .onFailure(testContext::failNow)
          .onSuccess(res -> {
            // entity too large
            assertEquals(413, res.statusCode());
            testContext.completeNow();
          });

        req
          .putHeader(HttpHeaders.EXPECT, "100-continue")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onFailure(testContext::failNow))
          .sendHead()
          .onFailure(testContext::failNow);
      });
  }
}
