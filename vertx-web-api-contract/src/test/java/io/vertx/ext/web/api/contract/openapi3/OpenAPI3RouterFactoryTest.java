package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestWithWebClientBase;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.validation.ValidationException;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RouterFactoryTest extends WebTestWithWebClientBase {

  public static Handler<RoutingContext> generateFailureHandler(boolean expected) {
    return routingContext -> {
      Throwable failure = routingContext.failure();
      if (failure instanceof ValidationException) {
        if (!expected) {
          failure.printStackTrace();
        }
        routingContext.response().setStatusCode(400).setStatusMessage("failure:" + ((ValidationException) failure)
          .type().name()).end();
      } else {
        failure.printStackTrace();
        routingContext.response().setStatusCode(500).setStatusMessage("unknownfailure:" + failure.toString()).end();
      }
    };
  }

  private void startServer(Router router) throws Exception {
    server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router::accept).listen(onSuccess(res -> {
      latch.countDown();
    }));
    awaitLatch(latch);
  }

  private void stopServer() throws Exception {
    if (server != null) {
      CountDownLatch latch = new CountDownLatch(1);
      server.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
      awaitLatch(latch);
    }
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stopServer(); // Have to stop default server of WebTestBase
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080));
  }

  @Override
  public void tearDown() throws Exception {
    if (client != null) {
      try {
        client.close();
      } catch (IllegalStateException e) {
      }
    }
    super.tearDown();
  }

  private static class PetStoreTestRouterBuilder {

    private final CompletableFuture<Router> router = new CompletableFuture<>();

    public void buildRouter(final AsyncResult<OpenAPI3RouterFactory> openAPI3RouterFactoryAsyncResult) {
      if (openAPI3RouterFactoryAsyncResult.failed()) {
        router.completeExceptionally(new IllegalStateException("RouterFactory result failed"));
      } else {
        OpenAPI3RouterFactory routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.mountOperationsWithoutHandlers(true);

        routerFactory.addHandlerByOperationId("listPets", routingContext -> {
          routingContext.response().setStatusMessage("path mounted!").end();
        });
        routerFactory.addFailureHandlerByOperationId("listPets", generateFailureHandler(false));

        routerFactory.addHandler(HttpMethod.POST, "/pets", routingContext -> {
          routingContext.response().setStatusMessage("path mounted!").end();
        });
        routerFactory.addFailureHandler(HttpMethod.POST, "/pets", generateFailureHandler(false));

        //Test if router generation throw error if no handler is set for security validation
        boolean thrown = false;
        try {
          routerFactory.getRouter();
        } catch (RouterFactoryException e) {
          thrown = true;
        }

        if (!thrown) {
          router.completeExceptionally(new AssertionError("RouterFactoryException not thrown"));
        } else {
          // Add security handler
          routerFactory.addSecurityHandler("api_key", routingContext -> routingContext.next());

          router.complete(routerFactory.getRouter());
        }
      }
    }

    public Router getRouter() {
      try {
        return router.get(5, TimeUnit.SECONDS);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        throw new RuntimeException(e);
      }
    }

  }

  @Test
  public void loadPetStoreFromContentAndTestSomething() throws Exception {
    final String content = new String(Files.readAllBytes(Paths.get("src/test/resources/swaggers/testSpec.yaml")));
    final PetStoreTestRouterBuilder testRouterBuilder = new PetStoreTestRouterBuilder();

    OpenAPI3RouterFactory.createRouterFactoryFromContent(this.vertx, content, testRouterBuilder::buildRouter);

    testPetStoreRouter(testRouterBuilder);

  }

  @Test
  public void loadPetStoreFromFileAndTestSomething() throws Exception {
    final PetStoreTestRouterBuilder routerBuilder = new PetStoreTestRouterBuilder();
    OpenAPI3RouterFactory.createRouterFactoryFromFile(this.vertx, "src/test/resources/swaggers/testSpec.yaml", routerBuilder::buildRouter);
    testPetStoreRouter(routerBuilder);
  }

  public void testPetStoreRouter(io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactoryTest.PetStoreTestRouterBuilder testRouterBuilder) throws Exception {
    final io.vertx.ext.web.Router router = testRouterBuilder.getRouter();
    router.route().failureHandler((routingContext -> {
      if (routingContext.statusCode() == 501)
        routingContext.response().setStatusCode(501).setStatusMessage("not implemented").end();
    }));

    startServer(router);

    testRequest(HttpMethod.GET, "/pets", 200, "path mounted!");
    testRequest(HttpMethod.POST, "/pets", 200, "path mounted!");
    testRequest(HttpMethod.GET, "/pets/3", 501, "Not Implemented");

    stopServer();
  }

}
