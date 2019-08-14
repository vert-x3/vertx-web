package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.ApiWebTestBase;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class OpenAPI3ServiceExtensionTest extends ApiWebTestBase {

  private OpenAPI3RouterFactory routerFactory;

  private RouterFactoryOptions HANDLERS_TESTS_OPTIONS = new RouterFactoryOptions()
    .setRequireSecurityHandlers(false)
    .setMountNotImplementedHandler(false);

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
    router = routerFactory.getRouter();
    server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router).listen(onSuccess(res -> {
      latch.countDown();
    }));
    awaitLatch(latch);
  }

  private void stopServer() throws Exception {
    routerFactory = null;
    if (server != null) {
      CountDownLatch latch = new CountDownLatch(1);
      server.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
      awaitLatch(latch);
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
      } catch (IllegalStateException e) {
      }
    }
    super.tearDown();
  }

  @Test
  public void operationExtension() throws Exception {
    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    MessageConsumer<JsonObject> serviceConsumer = serviceBinder.register(TestService.class, service);

    AnotherTestService anotherService = AnotherTestService.create(vertx);
    final ServiceBinder anotherServiceBinder = new ServiceBinder(vertx).setAddress("anotherAddress");
    MessageConsumer<JsonObject> anotherServiceConsumer = anotherServiceBinder.register(AnotherTestService.class, anotherService);

    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/extension_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.mountServicesFromExtensions();

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequestWithJSON(
      HttpMethod.POST,
      "/testA",
      new JsonObject().put("hello", "Ciao").put("name", "Francesco").toBuffer(),
      200,
      "OK",
      new JsonObject().put("result", "Ciao Francesco!").toBuffer()
    );

    testRequestWithJSON(
      HttpMethod.POST,
      "/testB",
      new JsonObject().put("hello", "Ciao").put("name", "Francesco").toBuffer(),
      200,
      "OK",
      new JsonObject().put("result", "Ciao Francesco?").toBuffer()
    );

    testRequestWithJSON(
      HttpMethod.POST,
      "/testC",
      new JsonObject().put("hello", "Ciao").put("name", "Francesco").toBuffer(),
      200,
      "OK",
      new JsonObject().put("content-type", "application/json").put("anotherResult", "Francesco Ciao?").toBuffer()
    );

    testRequestWithJSON(
      HttpMethod.POST,
      "/testD",
      new JsonObject().put("hello", "Ciao").put("name", "Francesco").toBuffer(),
      200,
      "OK",
      new JsonObject().put("content-type", "application/json").put("anotherResult", "Francesco Ciao?").toBuffer()
    );

    serviceBinder.unregister(serviceConsumer);
    anotherServiceBinder.unregister(anotherServiceConsumer);
  }

  @Test
  public void pathExtension() throws Exception {
    PathExtensionTestService service = new PathExtensionTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    MessageConsumer<JsonObject> serviceConsumer = serviceBinder.register(PathExtensionTestService.class, service);

    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/extension_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.mountServicesFromExtensions();

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(
      HttpMethod.GET,
      "/testPathLevel",
      200,
      "pathLevelGet"
    );

    testRequest(
      HttpMethod.POST,
      "/testPathLevel",
      200,
      "pathLevelPost"
    );

    serviceBinder.unregister(serviceConsumer);
  }

  @Test
  public void pathAndOperationExtensionMerge() throws Exception {
    PathExtensionTestService service = new PathExtensionTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    MessageConsumer<JsonObject> serviceConsumer = serviceBinder.register(PathExtensionTestService.class, service);

    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/extension_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.mountServicesFromExtensions();

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(
      HttpMethod.GET,
      "/testMerge",
      200,
      "getPathLevel"
    );

    testRequest(
      HttpMethod.POST,
      "/testMerge",
      200,
      "postPathLevel"
    );

    serviceBinder.unregister(serviceConsumer);
  }

  @Test
  public void pathAndOperationExtensionMapsMerge() throws Exception {
    PathExtensionTestService service = new PathExtensionTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    MessageConsumer<JsonObject> serviceConsumer = serviceBinder.register(PathExtensionTestService.class, service);

    CountDownLatch latch = new CountDownLatch(1);
    OpenAPI3RouterFactory.create(this.vertx, "src/test/resources/swaggers/extension_test.yaml",
      openAPI3RouterFactoryAsyncResult -> {
        routerFactory = openAPI3RouterFactoryAsyncResult.result();
        routerFactory.setOptions(HANDLERS_TESTS_OPTIONS);

        routerFactory.mountServicesFromExtensions();

        latch.countDown();
      });
    awaitLatch(latch);

    startServer();

    testRequest(
      HttpMethod.GET,
      "/testMerge2",
      200,
      "getPathLevel"
    );

    testRequest(
      HttpMethod.POST,
      "/testMerge2",
      200,
      "postPathLevel"
    );

    serviceBinder.unregister(serviceConsumer);
  }

}
