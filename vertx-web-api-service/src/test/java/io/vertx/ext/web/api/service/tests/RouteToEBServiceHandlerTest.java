package io.vertx.ext.web.api.service.tests;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.api.service.RouteToEBServiceHandler;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.impl.UserContextInternal;
import static io.vertx.ext.web.validation.builder.Bodies.json;
import static io.vertx.ext.web.validation.builder.Parameters.param;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.ext.web.validation.tests.BaseValidationHandlerTest;
import static io.vertx.ext.web.validation.tests.testutils.TestRequest.bodyResponse;
import static io.vertx.ext.web.validation.tests.testutils.TestRequest.emptyResponse;
import static io.vertx.ext.web.validation.tests.testutils.TestRequest.jsonBodyResponse;
import static io.vertx.ext.web.validation.tests.testutils.TestRequest.statusCode;
import static io.vertx.ext.web.validation.tests.testutils.TestRequest.statusMessage;
import static io.vertx.ext.web.validation.tests.testutils.TestRequest.testRequest;
import io.vertx.json.schema.JsonSchema;
import static io.vertx.json.schema.common.dsl.Schemas.anyOf;
import static io.vertx.json.schema.common.dsl.Schemas.arraySchema;
import static io.vertx.json.schema.common.dsl.Schemas.intSchema;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.ref;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@ExtendWith(VertxExtension.class)
public class RouteToEBServiceHandlerTest extends BaseValidationHandlerTest {

  MessageConsumer<JsonObject> consumer;

  @TempDir
  Path tempDir;

  @AfterEach
  public void tearDown() {
    if (consumer != null) consumer.unregister();
  }

  @Test
  public void serviceProxyTypedTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    AnotherTestService service = new AnotherTestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(AnotherTestService.class, service);

    router
      .post("/testE/:id")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandlerBuilder.create(schemaRepo)
          .pathParameter(param("id", intSchema()))
          .body(json(objectSchema().property("value", intSchema())))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testE")
      );

    router
      .post("/testF/:id")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandlerBuilder.create(schemaRepo)
          .pathParameter(param("id", intSchema()))
          .body(json(
            anyOf(
              objectSchema().property("value", intSchema()),
              arraySchema().items(intSchema())
            )
          ))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testF")
      );


    testRequest(client, HttpMethod.POST, "/testE/123")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(jsonBodyResponse(new JsonObject().put("id", 123).put("value", 1)))
      .sendJson(new JsonObject().put("value", 1), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testF/123")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(jsonBodyResponse(new JsonArray().add(1 + 123).add(2 + 123).add(3 + 123)))
      .sendJson(new JsonArray().add(1).add(2).add(3), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testF/123")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(jsonBodyResponse(new JsonObject().put("id", 123).put("value", 1)))
      .sendJson(new JsonObject().put("value", 1), testContext, checkpoint);
  }

  @Test
  public void serviceProxyDataObjectTest(Vertx vertx, VertxTestContext testContext) throws IOException {
    Checkpoint checkpoint = testContext.checkpoint();

    AnotherTestService service = new AnotherTestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(AnotherTestService.class, service);

    JsonSchema filterSchema = JsonSchema.of(
      new JsonObject(Buffer.buffer(Files.readAllBytes(Paths.get(
        "src", "test",
        "resources", "filter.json")))));
    schemaRepo.dereference("app://filter.json", filterSchema);

    router
      .post("/test")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandlerBuilder.create(schemaRepo)
          .body(json(ref(JsonPointer.fromURI(URI.create("app://filter.json")))))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testDataObject")
      );

    FilterData data = FilterData.generate();

    JsonObject result = data.toJson().copy();
    result.remove("message");

    testRequest(client, HttpMethod.POST, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(jsonBodyResponse(result))
      .sendJson(data.toJson(), testContext, checkpoint);
  }

  @Test
  public void emptyOperationResultTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(TestService.class, service);

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testEmptyServiceResponse")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(emptyResponse())
      .send(testContext, checkpoint);
  }

  @Test
  public void authorizedUserTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(TestService.class, service);

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(rc -> {
        ((UserContextInternal) rc.userContext()).setUser(User.fromName("slinkydeveloper")); // Put user mock into context
        rc.next();
      })
      .handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testUser")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(jsonBodyResponse(new JsonObject().put("result", "Hello slinkydeveloper!")))
      .send(testContext, checkpoint);
  }

  @Test
  public void extraPayloadTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(TestService.class, service);

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(
        RouteToEBServiceHandler
          .build(vertx.eventBus(), "someAddress", "extraPayload")
          .extraPayloadMapper(rc -> new JsonObject().put("username", "slinkydeveloper"))
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(jsonBodyResponse(new JsonObject().put("result", "Hello slinkydeveloper!")))
      .send(testContext, checkpoint);
  }

  @Test
  public void serviceProxyManualFailureTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    FailureTestService service = new FailureTestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder
      .setIncludeDebugInfo(true)
      .register(FailureTestService.class, service);

    router
      .post("/testFailure")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandlerBuilder.create(schemaRepo)
          .body(json(
            objectSchema()
              .requiredProperty("hello", stringSchema())
              .requiredProperty("name", stringSchema())
              .allowAdditionalProperties(false)
          )).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testFailure")
      ).failureHandler(
        rc -> rc.response().setStatusCode(501).setStatusMessage(rc.failure().getMessage()).end()
      );

    router
      .post("/testException")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandlerBuilder.create(schemaRepo)
          .body(json(
            objectSchema()
              .requiredProperty("hello", stringSchema())
              .requiredProperty("name", stringSchema())
              .allowAdditionalProperties(false)
          )).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testException")
      );

    testRequest(client, HttpMethod.POST, "/testFailure")
      .expect(statusCode(501), statusMessage("error for Francesco"))
      .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testException")
      .expect(statusCode(500), statusMessage("Unknown failure: (RECIPIENT_FAILURE,-1)"))
      .sendJson(new JsonObject().put("hello", "Ciao").put("name", "Francesco"), testContext, checkpoint);
  }


  @Test
  public void binaryDataTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    BinaryTestService service = new BinaryTestServiceImpl();
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder
      .setIncludeDebugInfo(true)
      .register(BinaryTestService.class, service);

    router
      .get("/test")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "binaryTest")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(bodyResponse(Buffer.buffer(new byte[]{(byte) 0xb0}), "application/octet-stream"))
      .send(testContext, checkpoint);
  }

  @Test
  public void authorizationPropagationTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(TestService.class, service);

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(rc -> {
        // patch the request to include authorization header
        rc.request().headers().add(HttpHeaders.AUTHORIZATION, "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        rc.next();
      })
      .handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testAuthorization")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(jsonBodyResponse(new JsonObject().put("result", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==")))
      .send(testContext, checkpoint);
  }

  @Test
  public void filePathResponseTest(Vertx vertx, VertxTestContext testContext) throws IOException {
    Checkpoint checkpoint = testContext.checkpoint();

    byte[] fileContent = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
    Path tempFile = tempDir.resolve("test-file.bin");
    Files.write(tempFile, fileContent);

    vertx.eventBus().<JsonObject>consumer("fileAddress", msg -> {
      ServiceResponse resp = ServiceResponse.completedWithFilePath(
        tempFile.toAbsolutePath().toString(), "application/octet-stream");
      msg.reply(resp.toJson());
    });

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "fileAddress", "testFile")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(bodyResponse(Buffer.buffer(fileContent), "application/octet-stream"))
      .send(testContext, checkpoint);
  }

  @Test
  public void filePathWithDeleteAfterSendTest(Vertx vertx, VertxTestContext testContext) throws IOException {
    Checkpoint checkpoint = testContext.checkpoint();

    byte[] fileContent = "delete-me".getBytes();
    Path tempFile = tempDir.resolve("delete-after-send.bin");
    Files.write(tempFile, fileContent);

    vertx.eventBus().<JsonObject>consumer("fileDeleteAddress", msg -> {
      ServiceResponse resp = ServiceResponse.completedWithFilePath(
        tempFile.toAbsolutePath().toString(), "application/octet-stream", true);
      msg.reply(resp.toJson());
    });

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "fileDeleteAddress", "testFileDelete")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(bodyResponse(Buffer.buffer(fileContent), "application/octet-stream"))
      .expect(res -> {
        // Give a moment for async delete to complete
        vertx.setTimer(500, id -> {
          vertx.fileSystem().exists(tempFile.toAbsolutePath().toString()).onComplete(ar -> {
            testContext.verify(() -> {
              org.junit.jupiter.api.Assertions.assertTrue(ar.succeeded());
              org.junit.jupiter.api.Assertions.assertFalse(ar.result(), "File should have been deleted after send");
              checkpoint.flag();
            });
          });
        });
      })
      .send(testContext);
  }

  @Test
  public void filePathNotFoundTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    vertx.eventBus().<JsonObject>consumer("fileMissingAddress", msg -> {
      ServiceResponse resp = ServiceResponse.completedWithFilePath(
        "/nonexistent/path/file.bin", "application/octet-stream");
      msg.reply(resp.toJson());
    });

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "fileMissingAddress", "testFileMissing")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(500))
      .send(testContext, checkpoint);
  }

  @Test
  public void filePathWithCustomHeadersTest(Vertx vertx, VertxTestContext testContext) throws IOException {
    Checkpoint checkpoint = testContext.checkpoint();

    byte[] fileContent = "cached-content".getBytes();
    Path tempFile = tempDir.resolve("cached-file.bin");
    Files.write(tempFile, fileContent);

    vertx.eventBus().<JsonObject>consumer("fileHeadersAddress", msg -> {
      ServiceResponse resp = ServiceResponse.completedWithFilePath(
        tempFile.toAbsolutePath().toString(), "image/jpeg");
      resp.putHeader("Cache-Control", "public, max-age=3600");
      msg.reply(resp.toJson());
    });

    router
      .get("/test")
      .handler(
        ValidationHandlerBuilder.create(schemaRepo).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "fileHeadersAddress", "testFileHeaders")
      );

    testRequest(client, HttpMethod.GET, "/test")
      .expect(statusCode(200), statusMessage("OK"))
      .expect(bodyResponse(Buffer.buffer(fileContent), "image/jpeg"))
      .expect(res -> {
        org.junit.jupiter.api.Assertions.assertEquals(
          "public, max-age=3600", res.getHeader("Cache-Control"));
      })
      .send(testContext, checkpoint);
  }
}
