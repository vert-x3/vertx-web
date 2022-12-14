package io.vertx.ext.web.openapi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.validation.testutils.ValidationTestUtils;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.openapi3.OpenAPI3SchemaParser;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@ExtendWith(VertxExtension.class)
public abstract class BaseRouterBuilderTest {

  public SchemaRouter schemaRouter;
  public SchemaParser parser;
  public HttpServer server;
  public WebClient client;
  public Router router;

  @BeforeEach
  public void setUp(Vertx vertx) {
    schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = OpenAPI3SchemaParser.create(schemaRouter);
    client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(9000).setDefaultHost("localhost"));
  }

  @AfterEach
  public void tearDown() throws InterruptedException {
    if (client != null) {
      client.close();
    }
    if (server != null) {
      CountDownLatch latch = new CountDownLatch(1);
      server.close(v -> latch.countDown());
      latch.await();
    }
  }

  protected Future<Void> startServer(Vertx vertx, RouterBuilder factory,
                                     Map.Entry<Integer, Handler<RoutingContext>>... additionalErrorHandlers) {
    try {
      router = factory.createRouter();
    } catch (Throwable e) {
      e.printStackTrace();
      return Future.failedFuture(e);
    }
    ValidationTestUtils.mountRouterFailureHandler(router);
    router.errorHandler(404, rc -> {
      rc.response()
        .setStatusCode(404)
        .end();
    });
    Arrays.stream(additionalErrorHandlers)
      .forEach(e -> router.errorHandler(e.getKey(), e.getValue()));
    server = vertx
      .createHttpServer()
      .requestHandler(router);
    return server.listen(9000).mapEmpty();
  }

  protected Future<Void> loadBuilderAndStartServer(Vertx vertx, String specUri, VertxTestContext testContext,
                                                   Consumer<RouterBuilder> configurator, Map.Entry<Integer,
    Handler<RoutingContext>>... additionalErrorHandlers) {
    Promise<Void> f = Promise.promise();
    RouterBuilder.create(vertx, specUri, testContext.succeeding(rf -> {
      try {
        configurator.accept(rf);
        startServer(vertx, rf, additionalErrorHandlers).
          onComplete(testContext.succeeding(v -> f.complete()));
      } catch (Exception e) {
        testContext.failNow(e);
        f.fail(e);
      }
    }));
    return f.future();
  }

}
