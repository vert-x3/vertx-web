package io.vertx.ext.web;

import io.vertx.core.http.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.handler.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class Router100ContinueTest {

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
  public void testContinue(TestContext should) {
    final Async test = should.async();
    router.route()
      .handler(BodyHandler.create())
      .handler(ctx -> {
        should.assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onFailure(should::fail)
      .onSuccess(req -> {
        req
          .response()
          .onFailure(should::fail)
          .onSuccess(res -> {
            should.assertEquals(200, res.statusCode());
            test.complete();
          });

        req
          .putHeader(HttpHeaders.EXPECT, "100-continue")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onFailure(should::fail))
          .sendHead()
          .onFailure(should::fail);
      });
  }

  @Test
  public void testBadExpectation(TestContext should) {
    final Async test = should.async();
    router.route()
      .handler(BodyHandler.create())
      .handler(ctx -> {
        should.assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onFailure(should::fail)
      .onSuccess(req -> {
        req
          .response()
          .onFailure(should::fail)
          .onSuccess(res -> {
            should.assertEquals(417, res.statusCode());
            test.complete();
          });

        req
          .putHeader(HttpHeaders.EXPECT, "lets-go")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onFailure(should::fail))
          .sendHead()
          .onFailure(should::fail);
      });
  }

  @Test
  public void testExpectButTooLarge(TestContext should) {
    final Async test = should.async();
    router.route()
      .handler(BodyHandler.create().setBodyLimit(1))
      .handler(ctx -> {
        should.assertEquals("DATA", ctx.body().asString());
        ctx.end();
      });

    client.request(HttpMethod.POST, "/")
      .onFailure(should::fail)
      .onSuccess(req -> {
        req
          .response()
          .onFailure(should::fail)
          .onSuccess(res -> {
            // entity too large
            should.assertEquals(413, res.statusCode());
            test.complete();
          });

        req
          .putHeader(HttpHeaders.EXPECT, "100-continue")
          .setChunked(true)
          .continueHandler(v ->
            req
              .end("DATA")
              .onFailure(should::fail))
          .sendHead()
          .onFailure(should::fail);
      });
  }
}
