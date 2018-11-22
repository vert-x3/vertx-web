package io.vertx.ext.web.api.contract.openapi3;

import io.swagger.v3.oas.models.Operation;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.ApiWebTestBase;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This tests are about OpenAPI3RouterFactory behaviours
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3RouterFactoryTest extends ApiWebTestBase {

  private OpenAPI3RouterFactory routerFactory;
  private HttpServer fileServer;
  private HttpServer securedFileServer;

  private Handler<RoutingContext> generateFailureHandler(boolean expected) {
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

  private void startServer() throws InterruptedException {
    Router router = routerFactory.getRouter();
    server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router).listen(onSuccess(res -> latch.countDown()));
    awaitLatch(latch);
  }

  private void startFileServer() throws InterruptedException {
    Router router = Router.router(vertx);
    router.route().handler(StaticHandler.create("src/test/resources"));
    CountDownLatch latch = new CountDownLatch(1);
    fileServer = vertx.createHttpServer(new HttpServerOptions().setPort(8081))
      .requestHandler(router).listen(onSuccess(res -> latch.countDown()));
    awaitLatch(latch);
  }

  private void startSecuredFileServer() throws InterruptedException {
    Router router = Router.router(vertx);
    router.route()
      .handler((RoutingContext ctx) -> {
        if (ctx.request().getHeader("Authorization") == null) ctx.fail(HttpStatus.SC_FORBIDDEN);
        else ctx.next();
      })
      .handler(StaticHandler.create("src/test/resources"));
    CountDownLatch latch = new CountDownLatch(1);
    securedFileServer = vertx.createHttpServer(new HttpServerOptions().setPort(8081))
      .requestHandler(router).listen(onSuccess(res -> latch.countDown()));
    awaitLatch(latch);
  }

  private void stopServer() throws Exception {
    routerFactory = null;
    CountDownLatch latch = new CountDownLatch(3);
    stopServer(latch, fileServer);
    stopServer(latch, securedFileServer);
    stopServer(latch, server);
    awaitLatch(latch);
    fileServer = null;
    server = null;
  }

  private void stopServer(CountDownLatch latch, HttpServer server) {
    if (server == null) {
      latch.countDown();
    } else {
      server.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
    }
  }

  private void assertThrow(Runnable r, Class exception) {
    try {
      r.run();
      assertTrue(exception.getName() + " not thrown", false);
    } catch (Exception e) {
      assertTrue(exception.getName() + " not thrown. Thrown: " + e.getClass().getName(), e.getClass().equals(exception));
    }
  }

  private void assertNotThrow(Runnable r, Class exception) {
    try {
      r.run();
    } catch (Exception e) {
      assertFalse(exception.getName() + " not thrown. Thrown: " + e.getClass().getName(), e.getClass().equals(exception));
    }
  }

  private void assertNotThrow(Runnable r) {
    try {
      r.run();
    } catch (Exception e) {
      assertTrue("Exception " + e + " is thrown", false);
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
      } catch (IllegalStateException ignored) {
      }
    }
    super.tearDown();
  }

  @Test
  public void loadSpecFromFile() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.succeeded());
        assertNotNull(openAPI3RouterFactoryAsyncResult.result());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  @Test
  public void failLoadSpecFromFile() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/aaa.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.failed());
        assertEquals(RouterFactoryException.class, openAPI3RouterFactoryAsyncResult.cause().getClass());
        assertEquals(RouterFactoryException.ErrorType.INVALID_SPEC_PATH, ((RouterFactoryException) openAPI3RouterFactoryAsyncResult.cause()).type());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  @Test
  public void loadWrongSpecFromFile() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/bad_spec.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.failed());
        assertEquals(RouterFactoryException.class, openAPI3RouterFactoryAsyncResult.cause().getClass());
        assertEquals(RouterFactoryException.ErrorType.SPEC_INVALID, ((RouterFactoryException) openAPI3RouterFactoryAsyncResult.cause()).type());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  @Test
  public void loadSpecFromURL() throws Exception {
    startFileServer();
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "http://localhost:8081/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.succeeded());
        assertNotNull(openAPI3RouterFactoryAsyncResult.result());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  @Test
  public void loadSpecFromURLWithAuthorizationValues() throws Exception {
    startSecuredFileServer();
    CountDownLatch latch = new CountDownLatch(1);
    JsonObject authValue = new JsonObject()
      .put("value", "Bearer xx.yy.zz")
      .put("keyName", "Authorization")
      .put("type", "header");
    OpenAPI3RouterFactory.create(
      this.vertx,
      "http://localhost:8081/swaggers/router_factory_test.yaml",
      Collections.singletonList(authValue),
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.succeeded());
        assertNotNull(openAPI3RouterFactoryAsyncResult.result());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  @Test
  public void failLoadSpecFromURL() throws Exception {
    startFileServer();
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "http://localhost:8081/swaggers/does_not_exist.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.failed());
        assertEquals(RouterFactoryException.class, openAPI3RouterFactoryAsyncResult.cause().getClass());
        assertEquals(RouterFactoryException.ErrorType.INVALID_SPEC_PATH, ((RouterFactoryException) openAPI3RouterFactoryAsyncResult.cause()).type());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  private RouterFactoryOptions HANDLERS_TESTS_OPTIONS = new RouterFactoryOptions()
    .setRequireSecurityHandlers(false);

  @Test
  public void mountHandlerTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.addHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage("OK")
          .end());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", 200, "OK");
  }

  @Test
  public void mountFailureHandlerTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory
          .addHandlerByOperationId("listPets", routingContext -> routingContext.fail(null))
          .addFailureHandlerByOperationId("listPets", routingContext -> routingContext
            .response()
            .setStatusCode(500)
            .setStatusMessage("ERROR")
            .end());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", 500, "ERROR");
  }

  @Test
  public void mountMultipleHandlers() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory
          .addHandlerByOperationId("listPets", routingContext ->
            routingContext.put("message", "A").next()
          )
          .addHandlerByOperationId("listPets", routingContext -> {
            routingContext.put("message", routingContext.get("message") + "B");
            routingContext.fail(500);
          })
          .addFailureHandlerByOperationId("listPets", routingContext ->
            routingContext.put("message", routingContext.get("message") + "E").next()
          )
          .addFailureHandlerByOperationId("listPets", routingContext -> routingContext
            .response()
            .setStatusCode(500)
            .setStatusMessage(routingContext.get("message"))
            .end());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", 500, "ABE");
  }

  @Test
  public void mountSecurityHandlers() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

        routerFactory.addHandlerByOperationId("listPetsSecurity", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(routingContext.get("first_level") + "-" +
            routingContext.get("second_level") + "-" + routingContext.get("third_level_one") +
            "-" + routingContext.get("third_level_two") + "-Done")
          .end());

        routerFactory.addSecurityHandler("api_key",
          routingContext -> routingContext.put("first_level", "User").next()
        );

        routerFactory.addSecuritySchemaScopeValidator("second_api_key", "moderator",
          routingContext -> routingContext.put("second_level", "Moderator").next()
        );

        routerFactory.addSecuritySchemaScopeValidator("third_api_key", "admin",
          routingContext -> routingContext.put("third_level_one", "Admin").next()
        );

        routerFactory.addSecuritySchemaScopeValidator("third_api_key", "useless",
          routingContext -> routingContext.put("third_level_one", "Wrong!").next()
        );

        routerFactory.addSecuritySchemaScopeValidator("third_api_key", "super_admin",
          routingContext -> routingContext.put("third_level_two", "SuperAdmin").next()
        );

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets_security_test", 200, "User-Moderator-Admin-SuperAdmin-Done");
  }

  @Test
  public void mountMultipleSecurityHandlers() throws Exception {
    final Handler<RoutingContext> firstHandler = routingContext -> routingContext.put("firstHandler", "OK").next();
    final Handler<RoutingContext> secondHandler = routingContext -> routingContext.put("secondHandler", "OK").next();
    final Handler<RoutingContext> secondApiKey = routingContext -> routingContext.put("secondApiKey", "OK").next();
    final Handler<RoutingContext> thirdApiKey = routingContext -> routingContext.put("thirdApiKey", "OK").next();

    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

        routerFactory.addHandlerByOperationId("listPetsSecurity", routingContext ->
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage("First handler: " + routingContext.get("firstHandler") + ", Second handler: " + routingContext.get("secondHandler") + ", Second api key: " + routingContext.get("secondApiKey") + ", Third api key: " + routingContext.get("thirdApiKey"))
            .end()
        );

        routerFactory.addSecurityHandler("api_key", firstHandler);
        routerFactory.addSecurityHandler("api_key", secondHandler);
        routerFactory.addSecurityHandler("second_api_key", secondApiKey);
        routerFactory.addSecurityHandler("third_api_key", thirdApiKey);
        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets_security_test", 200, "First handler: OK, Second handler: OK, Second api key: OK, Third api key: OK");
  }

  @Test
  public void requireSecurityHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

        routerFactory.addHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(routingContext.get("message") + "OK")
          .end());

        latch.countDown();
      });
    awaitLatch(latch);

    assertThrow(routerFactory::getRouter, RouterFactoryException.class);

    routerFactory.addSecurityHandler("api_key", RoutingContext::next);

    routerFactory.addSecurityHandler("second_api_key",
      RoutingContext::next
    );

    routerFactory.addSecurityHandler("third_api_key",
      RoutingContext::next
    );

    assertNotThrow(routerFactory::getRouter, RouterFactoryException.class);
  }


  @Test
  public void testGlobalSecurityHandler() throws Exception {
    final Handler<RoutingContext> handler = routingContext -> {
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage(((routingContext.get("message") != null) ? routingContext.get("message") + "-OK" : "OK"))
        .end();
    };

    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/global_security_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(true));

        routerFactory.addHandlerByOperationId("listPetsWithoutSecurity", handler);
        routerFactory.addHandlerByOperationId("listPetsWithOverride", handler);
        routerFactory.addHandlerByOperationId("listPetsWithoutOverride", handler);

        latch.countDown();
      });
    awaitLatch(latch);

    assertThrow(routerFactory::getRouter, RouterFactoryException.class);

    routerFactory.addSecurityHandler("global_api_key",
      routingContext -> routingContext.put("message", "Global").next()
    );

    routerFactory.addSecurityHandler("api_key",
      routingContext -> routingContext.put("message", "Local").next()
    );

    startServer();

    testRequest(HttpMethod.GET, "/petsWithoutSecurity", 200, "OK");
    testRequest(HttpMethod.GET, "/petsWithOverride", 200, "Local-OK");
    testRequest(HttpMethod.GET, "/petsWithoutOverride", 200, "Global-OK");
  }

  @Test
  public void notRequireSecurityHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));

        routerFactory.addHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(routingContext.get("message") + "OK")
          .end());

        latch.countDown();
      });
    awaitLatch(latch);

    assertNotThrow(() -> routerFactory.getRouter(), RouterFactoryException.class);
  }

  @Test
  public void mountValidationFailureHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.addHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(((RequestParameters) routingContext.get("parsedParameters")).queryParameter("limit").toString())
          .end());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets?limit=hello", 400, "Bad Request");
    testRequest(HttpMethod.GET, "/pets?limit=10", 200, "10");
  }

  @Test
  public void mountCustomValidationFailureHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);
        routerFactory.setValidationFailureHandler(routingContext ->
          routingContext
            .response()
            .setStatusCode(400)
            .setStatusMessage("Very very Bad Request")
            .end()
        );

        routerFactory.addHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(((RequestParameters) routingContext.get("parsedParameters")).queryParameter("limit").toString())
          .end());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets?limit=hello", 400, "Very very Bad Request");
    testRequest(HttpMethod.GET, "/pets?limit=10", 200, "10");
  }

  @Test
  public void notMountValidationFailureHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(
          new RouterFactoryOptions()
            .setRequireSecurityHandlers(false)
            .setMountValidationFailureHandler(false)
        );

        routerFactory.addHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage(((RequestParameters) routingContext.get("parsedParameters")).queryParameter("limit").toString())
          .end());

        routerFactory.addFailureHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode((routingContext.failure() instanceof ValidationException) ? 400 : 500)
          .setStatusMessage((routingContext.failure() instanceof ValidationException) ? "Very very Bad Request" : "Error")
          .end());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets?limit=hello", 400, "Very very Bad Request");
    testRequest(HttpMethod.GET, "/pets?limit=10", 200, "10");
  }


  @Test
  public void mountNotImplementedHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(
          new RouterFactoryOptions()
            .setRequireSecurityHandlers(false)
            .setMountNotImplementedHandler(true)
        );

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", 501, "Not Implemented");
  }

  @Test
  public void mountCustomNotImplementedHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(
          new RouterFactoryOptions()
            .setMountNotImplementedHandler(true)
            .setRequireSecurityHandlers(false)
        );

        routerFactory.setNotImplementedFailureHandler(routingContext ->
          routingContext
            .response()
            .setStatusCode(501)
            .setStatusMessage("We are too lazy to implement this operation")
            .end()
        );

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", 501, "We are too lazy to implement this operation");
  }

  @Test
  public void notMountNotImplementedHandler() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(
          new RouterFactoryOptions()
            .setMountNotImplementedHandler(false)
        );

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", 404, "Not Found");
  }

  @Test
  public void addGlobalHandlersTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));

        routerFactory.addGlobalHandler(rc -> {
          rc.response().putHeader("header-from-global-handler", "some dummy data");
          rc.next();
        });
        routerFactory.addGlobalHandler(rc -> {
            rc.response().putHeader("header-from-global-handler", "some more dummy data");
            rc.next();
        });

        routerFactory.addHandlerByOperationId("listPets", routingContext -> routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage("OK")
          .end());

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", null,
      response -> assertEquals(response.getHeader("header-from-global-handler"),
        "some more dummy data"), 200, "OK", null);
  }

  @Test
  public void exposeConfigurationTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false).setOperationModelKey("fooBarKey"));

        routerFactory.addHandlerByOperationId("listPets", routingContext -> {
          Operation operation = routingContext.get("fooBarKey");

          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .end(operation.getOperationId());
        });

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/pets", 200, "OK", "listPets");
  }

  @Test
  public void consumesTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/produces_consumes_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(false));

        routerFactory.addHandlerByOperationId("consumesTest", routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          if (params.body() != null && params.body().isJsonObject()) {
            routingContext
              .response()
              .setStatusCode(200)
              .setStatusMessage("OK")
              .putHeader("Content-Type", "application/json")
              .end(params.body().getJsonObject().encode());
          } else {
            routingContext
              .response()
              .setStatusCode(200)
              .setStatusMessage("OK")
              .end();
          }
        });

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    // Json consumes test
    JsonObject obj = new JsonObject("{\"name\":\"francesco\"}");
    testRequestWithJSON(HttpMethod.POST, "/consumesTest", obj.toBuffer(), 200, "OK", obj.toBuffer());

    // Form consumes tests
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("name", "francesco");
    testRequestWithForm(HttpMethod.POST, "/consumesTest", FormType.FORM_URLENCODED, form, 200, "OK");
    testRequestWithForm(HttpMethod.POST, "/consumesTest", FormType.MULTIPART, form, 404, "Not Found");
  }

  @Test
  public void producesTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/produces_consumes_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(false));

        routerFactory.addHandlerByOperationId("producesTest", routingContext -> {
          if (((RequestParameters) routingContext.get("parsedParameters")).queryParameter("fail").getBoolean())
            routingContext.response().putHeader("content-type", "text/plain").setStatusCode(500).end("Hate it");
          else
            routingContext.response().setStatusCode(200).end("{}"); // ResponseContentTypeHandler does the job for me
        });

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    List<String> acceptableContentTypes = Stream.of("application/json", "text/plain").collect(Collectors.toList());
    testRequestWithResponseContentTypeCheck(HttpMethod.GET, "/producesTest", 200, "application/json", acceptableContentTypes);
    testRequestWithResponseContentTypeCheck(HttpMethod.GET, "/producesTest?fail=true", 500, "text/plain", acceptableContentTypes);

  }

  @Test
  public void mountHandlersOrderTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/test_order_spec.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        assertTrue(openAPI3RouterFactoryAsyncResult.succeeded());
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(false));

        routerFactory.addHandlerByOperationId("showSpecialProduct", routingContext ->
          routingContext.response().setStatusMessage("special").end()
        );
        routerFactory.addFailureHandlerByOperationId("showSpecialProduct", generateFailureHandler(false));

        routerFactory.addHandlerByOperationId("showProductById", routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          routingContext.response().setStatusMessage(params.pathParameter("id").getInteger().toString()).end();
        });
        routerFactory.addFailureHandlerByOperationId("showProductById", generateFailureHandler(false));

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/product/special", 200, "special");
    testRequest(HttpMethod.GET, "/product/123", 200, "123");

    stopServer();
  }

  @Test
  public void mountHandlerEncodedTest() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/router_factory_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.addHandlerByOperationId("encodedParamTest", routingContext -> {
          RequestParameters params = routingContext.get("parsedParameters");
          assertEquals("a:b", params.pathParameter("p1").toString());
          assertEquals("a:b", params.queryParameter("p2").toString());
          routingContext
            .response()
            .setStatusCode(200)
            .setStatusMessage(params.pathParameter("p1").toString())
            .end();
        });

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(HttpMethod.GET, "/foo/a%3Ab?p2=a%3Ab", 200, "a:b");
  }

  /**
   * Tests that user can supply customised BodyHandler
   *
   * @throws Exception
   */
  @Test
  public void customBodyHandlerTest() throws Exception {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/upload_test.yaml",
        openAPI3RouterFactoryAsyncResult -> {
          try {
            if (openAPI3RouterFactoryAsyncResult.succeeded()) {
              routerFactory = openAPI3RouterFactoryAsyncResult.result();
              routerFactory.setOptions(new RouterFactoryOptions().setRequireSecurityHandlers(false));

              routerFactory.setBodyHandler(BodyHandler.create("my-uploads"));

              routerFactory.addHandlerByOperationId("upload", (h) -> h.response().setStatusCode(201).end());
            } else {
              fail(openAPI3RouterFactoryAsyncResult.cause());
            }
          } finally {
            latch.countDown();
          }
        });
      awaitLatch(latch);

      startServer();

      // We're not uploading a real file, just triggering BodyHandler
      MultiMap form = MultiMap.caseInsensitiveMultiMap();

      assertFalse(Paths.get("./my-uploads").toFile().exists());

      testRequestWithForm(HttpMethod.POST, "/upload", FormType.MULTIPART, form, 201, "Created");

      // BodyHandler should create this custom directory for us
      assertTrue(Paths.get("./my-uploads").toFile().exists());
    } finally {
      Paths.get("./my-uploads").toFile().deleteOnExit();
    }
  }
}
