package io.vertx.ext.web.api.contract.openapi3;

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

import java.util.concurrent.CountDownLatch;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RouterFactoryTest extends WebTestWithWebClientBase {

  public Handler<RoutingContext> generateFailureHandler(boolean expected) {
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

  @Test
  public void loadPetStoreAndTestSomething() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    final Router[] router = {null};
    OpenAPI3RouterFactory.createRouterFactoryFromFile(this.vertx, "src/test/resources/swaggers/testSpec.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.succeeded());
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
        boolean throwed = false;
        try {
          router[0] = routerFactory.getRouter();
        } catch (RouterFactoryException e) {
          throwed = true;
        }
        assertTrue("RouterFactoryException not thrown", throwed);

        // Add security handler
        routerFactory.addSecurityHandler("api_key", routingContext -> routingContext.next());
        router[0] = routerFactory.getRouter();

        latch.countDown();
      });
    awaitLatch(latch);

    router[0].route().failureHandler((routingContext -> {
      if (routingContext.statusCode() == 501)
        routingContext.response().setStatusCode(501).setStatusMessage("not implemented").end();
    }));

    startServer(router[0]);

    testRequest(HttpMethod.GET, "/pets", 200, "path mounted!");
    testRequest(HttpMethod.POST, "/pets", 200, "path mounted!");
    testRequest(HttpMethod.GET, "/pets/3", 501, "Not Implemented");

    stopServer();

  }
}
