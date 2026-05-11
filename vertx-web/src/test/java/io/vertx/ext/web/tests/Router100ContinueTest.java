package io.vertx.ext.web.tests;

import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import io.vertx.test.core.TestUtils;
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
  public void testContinue(Checkpoint checkpoint, Checkpoint checkpoint2, Checkpoint checkpoint3) {
    router.route()
      .handler(BodyHandler.create())
      .handler(ctx -> {
        assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onComplete(TestUtils.onSuccess(req -> {
        req
          .response()
          .expecting(HttpResponseExpectation.SC_OK)
          .onComplete(checkpoint);

        req
          .putHeader(HttpHeaders.EXPECT, "100-continue")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onComplete(checkpoint3))
          .sendHead()
          .onComplete(checkpoint2);
      }));
  }

  @Test
  public void testBadExpectation(Checkpoint checkpoint, Checkpoint checkpoint2) {
    router.route()
      .handler(BodyHandler.create())
      .handler(ctx -> {
        assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onComplete(TestUtils.onSuccess2(req -> {
        req
          .response()
          .expecting(HttpResponseExpectation.SC_EXPECTATION_FAILED)
          .onComplete(checkpoint);

        req
          .putHeader(HttpHeaders.EXPECT, "lets-go")
          .setChunked(true)
          .sendHead()
          .onComplete(checkpoint2);
      }));
  }

  @Test
  public void testExpectButTooLarge(Checkpoint checkpoint, Checkpoint checkpoint2, Checkpoint checkpoint3) {
    router.route()
      .handler(BodyHandler.create().setBodyLimit(1))
      .handler(ctx -> {
        assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onComplete(TestUtils.onSuccess2(req -> {
        req
          .response()
          .expecting(HttpResponseExpectation.SC_REQUEST_ENTITY_TOO_LARGE)
          .onComplete(checkpoint);

        req
          .putHeader(HttpHeaders.EXPECT, "100-continue")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onComplete(checkpoint3))
          .sendHead()
          .onComplete(checkpoint2);
      }));
  }
}
