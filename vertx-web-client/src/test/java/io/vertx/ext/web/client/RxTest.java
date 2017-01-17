package io.vertx.ext.web.client;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.test.core.VertxTestBase;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import org.junit.Test;
import rx.Observable;
import rx.Single;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RxTest extends VertxTestBase {

  private Vertx vertx;
  private WebClient client;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    vertx = new Vertx(super.vertx);

  }

  @Test
  public void testGet() {
    int times = 5;
    waitFor(times);
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));
    server.requestStream().handler(req -> {
      req.response().setChunked(true).end("some_content");
    });
    try {
      server.listen(ar -> {
        client = WebClient.wrap(vertx.createHttpClient(new HttpClientOptions()));
        Single<HttpResponse<Buffer>> single = client
          .get(8080, "localhost", "/the_uri")
          .rxSend();
        for (int i = 0; i < times; i++) {
          single.subscribe(resp -> {
            assertEquals("some_content", resp.body().toString("UTF-8"));
            complete();
          }, this::fail);
        }
      });
      await();
    } finally {
      server.close();
    }
  }

  @Test
  public void testPost() {
    int times = 5;
    waitFor(times);
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));
    server.requestStream().handler(req -> {
      req.bodyHandler(buff -> {
        assertEquals("onetwothree", buff.toString());
        req.response().end();
      });
    });
    try {
      server.listen(ar -> {
        client = WebClient.wrap(vertx.createHttpClient(new HttpClientOptions()));
        Observable<Buffer> stream = Observable.just(Buffer.buffer("one"), Buffer.buffer("two"), Buffer.buffer("three"));
        Single<HttpResponse<Buffer>> single = client
          .post(8080, "localhost", "/the_uri")
          .rxSendStream(stream);
        for (int i = 0; i < times; i++) {
          single.subscribe(resp -> {
            complete();
          }, this::fail);
        }
      });
      await();
    } finally {
      server.close();
    }
  }

  @Test
  public void testResponseMissingBody() throws Exception {
    int times = 5;
    waitFor(times);
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));
    server.requestStream().handler(req -> {
      req.response().setStatusCode(403).end();
    });
    try {
      server.listen(ar -> {
        client = WebClient.wrap(vertx.createHttpClient(new HttpClientOptions()));
        Single<HttpResponse<Buffer>> single = client
          .get(8080, "localhost", "/the_uri")
          .rxSend();
        for (int i = 0; i < times; i++) {
          single.subscribe(resp -> {
            assertEquals(403, resp.statusCode());
            assertNull(resp.body());
            complete();
          }, this::fail);
        }
      });
      await();
    } finally {
      server.close();
    }
  }
}
