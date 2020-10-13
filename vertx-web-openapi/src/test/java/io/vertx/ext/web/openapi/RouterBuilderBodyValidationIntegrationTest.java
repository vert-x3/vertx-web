package io.vertx.ext.web.openapi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.testutils.ValidationTestUtils;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Paths;

import static io.vertx.ext.web.validation.testutils.TestRequest.*;

/**
 * This tests check the building of JSON schemas from OAS schemas and validation of JSON
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@ExtendWith(VertxExtension.class)
@SuppressWarnings("unchecked")
public class RouterBuilderBodyValidationIntegrationTest extends BaseRouterBuilderTest {

  final String OAS_PATH = "specs/schemas_test_spec.yaml";

  final Handler<RoutingContext> handler = routingContext -> {
    RequestParameter body = ((RequestParameters) routingContext.get("parsedParameters")).body();
    if (body.isJsonObject())
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("Content-Type", "application/json")
        .end(body.getJsonObject().encode());
    else
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("Content-Type", "application/json")
        .end(body.getJsonArray().encode());
  };

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    startFileServer(vertx, testContext).compose(v ->
      loadBuilderAndStartServer(vertx, OAS_PATH, testContext, routerBuilder -> {
        routerBuilder.operations().forEach(op -> op.handler(handler));
        routerBuilder
          .getSchemaParser()
          .withStringFormatValidator("phone", s -> s.matches("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$"));
      })
    ).onComplete(testContext.succeeding(t -> testContext.completeNow()));
  }

  private Future<Void> startFileServer(Vertx vertx, VertxTestContext testContext) {
    Router router = Router.router(vertx);
    router.route().handler(StaticHandler.create("./src/test/resources/specs/schemas"));
    return testContext.assertComplete(
      vertx.createHttpServer()
        .requestHandler(router)
        .listen(8081)
        .mapEmpty()
    );
  }

  private void assertRequestOk(String uri, String jsonName, Vertx vertx, VertxTestContext testContext, Checkpoint checkpoint) {
    vertx.fileSystem().readFile(Paths.get("src", "test", "resources", "specs", "test_json", "schemas_test", jsonName).toString(), testContext.succeeding(buf -> {
      Object json = Json.decodeValue(buf);
      testRequest(client, HttpMethod.POST, uri)
        .expect(jsonBodyResponse(json), statusCode(200))
        .sendJson(json, testContext, checkpoint);
    }));
  }

  private void assertRequestOk(String uri, String jsonNameRequest, String jsonNameResponse, Vertx vertx, VertxTestContext testContext, Checkpoint checkpoint) {
    vertx.fileSystem().readFile(Paths.get("src", "test", "resources", "specs", "test_json", "schemas_test", jsonNameRequest).toString(), testContext.succeeding(reqBuf ->
      vertx.fileSystem().readFile(Paths.get("src", "test", "resources", "specs", "test_json", "schemas_test", jsonNameResponse).toString(), testContext.succeeding(resBuf -> {
        Object reqJson = Json.decodeValue(reqBuf);
        Object resJson = Json.decodeValue(resBuf);
        testRequest(client, HttpMethod.POST, uri)
          .expect(jsonBodyResponse(resJson), statusCode(200))
          .sendJson(reqJson, testContext, checkpoint);
      }))
    ));
  }

  private void assertRequestFail(String uri, String jsonName, Vertx vertx, VertxTestContext testContext, Checkpoint checkpoint) {
    vertx.fileSystem().readFile(Paths.get("src", "test", "resources", "specs", "test_json", "schemas_test", jsonName).toString(), testContext.succeeding(buf -> {
      Object json = Json.decodeValue(buf);
      testRequest(client, HttpMethod.POST, uri)
        .expect(statusCode(400))
        .expect(ValidationTestUtils.badBodyResponse(BodyProcessorException.BodyProcessorErrorType.VALIDATION_ERROR))
        .sendJson(json, testContext, checkpoint);
    }));
  }


  @Test
  public void test1(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    assertRequestOk("/test1", "test1_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test1", "test1_fail_1.json", vertx, testContext, checkpoint);
    assertRequestFail("/test1", "test1_fail_2.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test2(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);
    assertRequestOk("/test2", "test2_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test2", "test2_fail_1.json", vertx, testContext, checkpoint);
    assertRequestFail("/test2", "test2_fail_2.json", vertx, testContext, checkpoint);
    assertRequestFail("/test2", "test2_fail_3.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test3(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);
    assertRequestOk("/test3", "test2_ok.json", vertx, testContext, checkpoint); // Same as test2
    assertRequestFail("/test3", "test2_fail_1.json", vertx, testContext, checkpoint);
    assertRequestFail("/test3", "test2_fail_2.json", vertx, testContext, checkpoint);
    assertRequestFail("/test3", "test2_fail_3.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test4(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);
    assertRequestOk("/test4", "test2_ok.json", vertx, testContext, checkpoint); // Same as test2
    assertRequestFail("/test4", "test2_fail_1.json", vertx, testContext, checkpoint);
    assertRequestFail("/test4", "test2_fail_2.json", vertx, testContext, checkpoint);
    assertRequestFail("/test4", "test2_fail_3.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test5(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    assertRequestOk("/test5", "test5_ok_1.json", vertx, testContext, checkpoint);
    assertRequestOk("/test5", "test5_ok_2.json", vertx, testContext, checkpoint);
    assertRequestFail("/test5", "test5_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test6(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    assertRequestOk("/test6", "test6_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test6", "test6_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test7(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(5);
    assertRequestOk("/test7", "test2_ok.json", vertx, testContext, checkpoint); // PersonComplex should work
    assertRequestOk("/test7", "test7_ok_1.json", vertx, testContext, checkpoint);
    assertRequestOk("/test7", "test7_ok_2.json", vertx, testContext, checkpoint);
    assertRequestFail("/test7", "test7_fail_1.json", vertx, testContext, checkpoint);
    assertRequestFail("/test7", "test7_fail_2.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test8(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    assertRequestOk("/test8", "test8_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test8", "test8_fail_1.json", vertx, testContext, checkpoint);
    assertRequestFail("/test8", "test8_fail_2.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test9(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    assertRequestOk("/test9", "test6_ok.json", vertx, testContext, checkpoint); // Test6 should work
    assertRequestOk("/test9", "test9_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test9", "test9_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test10(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    assertRequestOk("/test10", "test10_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test10", "test10_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test11(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    assertRequestOk("/test11", "test10_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test11", "test10_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test12(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    assertRequestOk("/test12", "test12_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test12", "test12_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test13(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();
    assertRequestOk("/test13", "test13_ok_request.json", "test13_ok_response.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test14(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    assertRequestOk("/test14", "test14_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test14", "test14_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test15(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    assertRequestOk("/test15", "test15_ok_1.json", vertx, testContext, checkpoint);
    assertRequestOk("/test15", "test15_ok_2.json", vertx, testContext, checkpoint);
    assertRequestFail("/test15", "test15_fail.json", vertx, testContext, checkpoint);
  }

  @Test
  public void test16(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    assertRequestOk("/test16", "test16_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/test16", "test16_fail.json", vertx, testContext, checkpoint);
  }


  @Test
  public void testLocalRelativeRef(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    assertRequestOk("/testLocalRelativeRef", "testLocalRelativeRef_ok.json", vertx, testContext, checkpoint);
    assertRequestFail("/testLocalRelativeRef", "testLocalRelativeRef_fail.json", vertx, testContext, checkpoint);
  }
}
