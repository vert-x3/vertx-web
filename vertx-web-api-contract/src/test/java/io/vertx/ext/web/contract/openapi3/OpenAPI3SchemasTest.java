package io.vertx.ext.web.contract.openapi3;

import io.swagger.oas.models.OpenAPI;
import io.swagger.parser.v3.OpenAPIV3Parser;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenAPI3RouterFactoryImpl;
import io.vertx.ext.web.api.validation.WebTestValidationBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3SchemasTest extends WebTestValidationBase {

  OpenAPI spec;
  OpenAPI3RouterFactory routerFactory;

  final Handler<RoutingContext> handler = routingContext -> {
    routingContext.response().setStatusCode(200).end("OK");
  };

  @Rule
  public ExternalResource resource = new ExternalResource() {
    @Override
    protected void before() throws Throwable {
      spec = loadSwagger("./src/test/resources/swaggers/schemas_test_spec.yaml");
    }

    @Override
    protected void after() {}
  };

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stopServer(); // Have to stop default server of WebTestBase
    routerFactory = new OpenAPI3RouterFactoryImpl(this.vertx, spec);
    routerFactory.enableValidationFailureHandler(true);
    routerFactory.setValidationFailureHandler(generateFailureHandler());
    routerFactory.mountOperationsWithoutHandlers(false);
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
    stopServer();
    super.tearDown();
  }



  private OpenAPI loadSwagger(String filename) throws IOException {
    return new OpenAPIV3Parser().readContents(String.join("\n", Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8)), null, null).getOpenAPI();
  }

  public Handler<RoutingContext> generateFailureHandler() {
    return routingContext -> routingContext.response().setStatusCode(400).end("ValidationException");
  }

  private void startServer() throws Exception {
    router = routerFactory.getRouter();
    server = this.vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router::accept).listen(onSuccess(res -> {
      latch.countDown();
    }));
    awaitLatch(latch);
  }

  private void stopServer() throws Exception {
    if (server != null) {
      CountDownLatch latch = new CountDownLatch(1);
      try {
        server.close((asyncResult) -> {
          latch.countDown();
        });
      } catch (IllegalStateException e) { // Server is already open
        latch.countDown();
      }
      awaitLatch(latch);
    }
  }

  private void assertRequestOk(String uri, String jsonName) throws Exception {
    String jsonString = String.join("", Files.readAllLines(Paths.get("./test_json", "schemas_test", jsonName), StandardCharsets.UTF_8));
    testRequestWithJSON(HttpMethod.GET, uri, new JsonObject(jsonString), 200, "OK");
  };

  private void assertRequestFail(String uri, String jsonName) throws Exception {
    String jsonString = String.join("", Files.readAllLines(Paths.get("./test_json", "schemas_test", jsonName), StandardCharsets.UTF_8));
    testRequestWithJSON(HttpMethod.GET, uri, new JsonObject(jsonString), 400, "ValidationException");
  };

  @Test
  public void test1() throws Exception {
    routerFactory.addHandlerByOperationId("test1", handler);
    startServer();
    assertRequestOk("/test1", "test1_ok.json");
    assertRequestFail("/test1", "test1_fail_1.json");
    assertRequestFail("/test1", "test1_fail_2.json");
  }

  @Test
  public void test2() throws Exception {
    routerFactory.addHandlerByOperationId("test2", handler);
    startServer();
    assertRequestOk("/test2", "test2_ok.json");
    assertRequestFail("/test2", "test2_fail_1.json");
    assertRequestFail("/test2", "test2_fail_2.json");
    assertRequestFail("/test2", "test2_fail_3.json");
  }

  @Test
  public void test3() throws Exception {
    routerFactory.addHandlerByOperationId("test3", handler);
    startServer();
    assertRequestOk("/test3", "test2_ok.json"); // Same as test2
    assertRequestFail("/test3", "test2_fail_1.json");
    assertRequestFail("/test3", "test2_fail_2.json");
    assertRequestFail("/test3", "test2_fail_3.json");
  }

  @Test
  public void test4() throws Exception {
    routerFactory.addHandlerByOperationId("test4", handler);
    startServer();
    assertRequestOk("/test4", "test2_ok.json"); // Same as test2
    assertRequestFail("/test4", "test2_fail_1.json");
    assertRequestFail("/test4", "test2_fail_2.json");
    assertRequestFail("/test4", "test2_fail_3.json");
  }

  @Test
  public void test5() throws Exception {
    routerFactory.addHandlerByOperationId("test5", handler);
    startServer();
    assertRequestOk("/test5", "test5_ok_1.json");
    assertRequestOk("/test5", "test5_ok_2.json");
    assertRequestFail("/test5", "test5_fail.json");
  }

  @Test
  public void test6() throws Exception {
    routerFactory.addHandlerByOperationId("test6", handler);
    startServer();
    assertRequestOk("/test6", "test6_ok.json");
    assertRequestFail("/test6", "test6_fail.json");
  }

  @Test
  public void test7() throws Exception {
    routerFactory.addHandlerByOperationId("test7", handler);
    startServer();
    assertRequestOk("/test7", "test2_ok.json"); // PersonComplex should work
    assertRequestOk("/test7", "test7_ok_1.json");
    assertRequestOk("/test7", "test7_ok_2.json");
    assertRequestFail("/test7", "test7_fail_1.json");
    assertRequestFail("/test7", "test7_fail_2.json");
  }

  @Test
  public void test8() throws Exception {
    routerFactory.addHandlerByOperationId("test8", handler);
    startServer();
    assertRequestOk("/test8", "test8_ok.json");
    assertRequestFail("/test8", "test8_fail_1.json");
    assertRequestFail("/test8", "test8_fail_2.json");
  }

  @Test
  public void test9() throws Exception {
    routerFactory.addHandlerByOperationId("test9", handler);
    startServer();
    assertRequestOk("/test9", "test6_ok.json"); // Test6 should work
    assertRequestOk("/test9", "test9_ok.json");
    assertRequestFail("/test8", "test9_fail.json");
  }

}
