package io.vertx.ext.web.api.contract.openapi3;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestWithWebClientBase;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    stopServer();
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
    OpenAPI3RouterFactory.createRouterFactoryFromFile(this.vertx, "src/test/resources/swaggers/base_router_factory_test.yaml",
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

  }

  @Test
  public void testConsumesProduces() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    final Router[] router = {null};
    boolean failProduce[] = {false};
    OpenAPI3RouterFactory.createRouterFactoryFromFile(this.vertx, "src/test/resources/swaggers/produces_consumes_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        OpenAPI3RouterFactory factory = openAPI3RouterFactoryAsyncResult.result();
        factory.mountOperationsWithoutHandlers(false);
        factory.addHandlerByOperationId("consumesTest", routingContext -> {
          routingContext.response().setStatusCode(200).setStatusMessage("ok").end();
        });

        factory.addHandlerByOperationId("producesTest", routingContext -> {
          if (failProduce[0])
            routingContext.response().putHeader("content-type", "text/plain").setStatusCode(500).end();
          else
            routingContext.response().end("{}"); // ResponseContentTypeHandler does the job for me
        });

        router[0] = factory.getRouter();
        router[0].route().order(0).handler(ResponseContentTypeHandler.create());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer(router[0]);

    // Json consumes test
    testRequestWithJSON(HttpMethod.POST, "/consumesTest", new JsonObject("{\"name\":\"francesco\"}"), 200, "ok");

    // Form consumes tests
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("name", "francesco");
    testRequestWithForm(HttpMethod.POST, "/consumesTest", FormType.FORM_URLENCODED, form, 200, "ok");
    testRequestWithForm(HttpMethod.POST, "/consumesTest", FormType.MULTIPART, form, 404, "Not Found");

    // Produces tests
    List<String> acceptableContentTypes = Stream.of("application/json", "text/plain").collect(Collectors.toList());
    testRequestWithResponseContentTypeCheck(HttpMethod.GET, "/producesTest", 200, "application/json", acceptableContentTypes);
    failProduce[0] = true; // So lazy way
    testRequestWithResponseContentTypeCheck(HttpMethod.GET, "/producesTest", 500, "text/plain", acceptableContentTypes);
  }

  @Test
  public void loadSpecFromURL() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.createRouterFactoryFromURL(this.vertx, "https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertFalse(openAPI3RouterFactoryAsyncResult.failed());
        latch.countDown();
      });
    awaitLatch(latch);
  }
}
