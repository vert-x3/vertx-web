package io.vertx.ext.web.openapi;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.service.*;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.vertx.ext.web.validation.testutils.TestRequest.*;

/**
 * These tests are about OpenAPI3RouterBuilder and Service Proxy integrations
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@SuppressWarnings("unchecked")
public class RouterBuilderApiServiceIntegrationTest extends BaseRouterBuilderTest {

  private final RouterBuilderOptions HANDLERS_TESTS_OPTIONS = new RouterBuilderOptions()
    .setRequireSecurityHandlers(false)
    .setMountNotImplementedHandler(false);

  List<MessageConsumer<JsonObject>> consumers = new ArrayList<>();

  @AfterEach
  public void stopServices() {
    consumers.forEach(MessageConsumer::unregister);
  }

  @Test
  public void serviceProxyManualTest(Vertx vertx, VertxTestContext testContext) {
    TestService service = new TestServiceImpl(vertx);

    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumers.add(serviceBinder.register(TestService.class, service));

    loadBuilderAndStartServer(vertx, "src/test/resources/specs/service_proxy_test.yaml", testContext, routerBuilder -> {
      routerBuilder.setOptions(HANDLERS_TESTS_OPTIONS);

      routerBuilder.operation("testA").routeToEventBus("someAddress");
    }).onComplete(h ->
      testRequest(client, HttpMethod.POST, "/testA")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(new JsonObject().put("result", "Ciao Francesco!")))
        .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext)
    );
  }

  @Test
  public void serviceProxyWithReflectionsTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumers.add(serviceBinder.register(TestService.class, service));

    loadBuilderAndStartServer(vertx, "src/test/resources/specs/service_proxy_test.yaml", testContext, routerBuilder -> {
      routerBuilder.setOptions(HANDLERS_TESTS_OPTIONS);
      routerBuilder.mountServiceInterface(service.getClass(), "someAddress");
    }).onComplete(h -> {
      testRequest(client, HttpMethod.POST, "/testA")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(new JsonObject().put("result", "Ciao Francesco!")))
        .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);
      testRequest(client, HttpMethod.POST, "/testB")
        .expect(statusCode(200))
        .expect(jsonBodyResponse(new JsonObject().put("result", "Ciao Francesco?")))
        .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);
    });
  }

  @Test
  public void operationExtension(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    consumers.add(serviceBinder.register(TestService.class, service));

    AnotherTestService anotherService = AnotherTestService.create(vertx);
    final ServiceBinder anotherServiceBinder = new ServiceBinder(vertx).setAddress("anotherAddress");
    consumers.add(anotherServiceBinder.register(AnotherTestService.class, anotherService));

    loadBuilderAndStartServer(vertx, "src/test/resources/specs/extension_test.yaml", testContext, routerBuilder -> {
      routerBuilder.setOptions(HANDLERS_TESTS_OPTIONS);
      routerBuilder.mountServicesFromExtensions();
    }).onComplete(h -> {
      testRequest(client, HttpMethod.POST, "/testA")
        .expect(jsonBodyResponse(new JsonObject().put("result", "Ciao Francesco!")), statusCode(200))
        .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/testB")
        .expect(jsonBodyResponse(new JsonObject().put("result", "Ciao Francesco?")), statusCode(200))
        .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/testC")
        .expect(jsonBodyResponse(new JsonObject().put("content-type", "application/json").put("anotherResult", "Francesco Ciao?")), statusCode(200))
        .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);

      testRequest(client, HttpMethod.POST, "/testD")
        .expect(jsonBodyResponse(new JsonObject().put("content-type", "application/json").put("anotherResult", "Francesco Ciao?")), statusCode(200))
        .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);
    });
  }

  @Test
  public void pathExtension(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    PathExtensionTestService service = new PathExtensionTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    consumers.add(serviceBinder.register(PathExtensionTestService.class, service));

    loadBuilderAndStartServer(vertx, "src/test/resources/specs/extension_test.yaml", testContext, routerBuilder -> {
      routerBuilder.setOptions(HANDLERS_TESTS_OPTIONS);
      routerBuilder.mountServicesFromExtensions();
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/testPathLevel")
        .expect(statusCode(200), statusMessage("pathLevelGet"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.POST, "/testPathLevel")
        .expect(statusCode(200), statusMessage("pathLevelPost"))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void pathAndOperationExtensionMerge(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    PathExtensionTestService service = new PathExtensionTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    consumers.add(serviceBinder.register(PathExtensionTestService.class, service));

    loadBuilderAndStartServer(vertx, "src/test/resources/specs/extension_test.yaml", testContext, routerBuilder -> {
      routerBuilder.setOptions(HANDLERS_TESTS_OPTIONS);
      routerBuilder.mountServicesFromExtensions();
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/testMerge")
        .expect(statusCode(200), statusMessage("getPathLevel"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.POST, "/testMerge")
        .expect(statusCode(200), statusMessage("postPathLevel"))
        .send(testContext, checkpoint);
    });
  }

  @Test
  public void pathAndOperationExtensionMapsMerge(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    PathExtensionTestService service = new PathExtensionTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("address");
    consumers.add(serviceBinder.register(PathExtensionTestService.class, service));

    loadBuilderAndStartServer(vertx, "src/test/resources/specs/extension_test.yaml", testContext, routerBuilder -> {
      routerBuilder.setOptions(HANDLERS_TESTS_OPTIONS);
      routerBuilder.mountServicesFromExtensions();
    }).onComplete(h -> {
      testRequest(client, HttpMethod.GET, "/testMerge2")
        .expect(statusCode(200), statusMessage("getPathLevel"))
        .send(testContext, checkpoint);
      testRequest(client, HttpMethod.POST, "/testMerge2")
        .expect(statusCode(200), statusMessage("postPathLevel"))
        .send(testContext, checkpoint);
    });
  }
}
