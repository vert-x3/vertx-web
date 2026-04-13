package io.vertx.ext.web.tests;

import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

@VertxTest
public class RoutingContextNullCurrentRouteTest {

  static final int PORT = 9091;
  private Vertx vertx;

  @BeforeEach
  public void before(Vertx v) throws Exception {
    vertx = v;
    vertx.deployVerticle(TestVerticle.class.getName())
      .await(20, TimeUnit.SECONDS);
  }

  @Test
  public void test() {
    HttpClient client = vertx.createHttpClient(new HttpClientOptions().setConnectTimeout(10000));
    WebClient webClient = WebClient.wrap(client);
    webClient.get(PORT, "127.0.0.1", "/test")
      .send()
      .expecting(HttpResponseExpectation.status(HttpURLConnection.HTTP_NO_CONTENT))
      .await();
  }

  public static class TestVerticle extends VerticleBase {

    @Override
    public Future<?> start() throws Exception {

      Router router = Router.router(vertx);
      router.get("/test").handler(routingCount ->
        vertx.setTimer(5000, timerId -> {
          HttpServerResponse response = routingCount.response();
          if (routingCount.currentRoute() == null) {
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
              .end();
          } else {
            response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT)
              .end();
          }
        }));

      return vertx.createHttpServer()
        .requestHandler(router)
        .listen(PORT);
    }
  }
}
