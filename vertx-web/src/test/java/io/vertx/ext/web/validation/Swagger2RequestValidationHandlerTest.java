package io.vertx.ext.web.validation;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.validation.impl.Swagger2RequestValidationHandlerImpl;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class Swagger2RequestValidationHandlerTest extends WebTestValidationBase {

  private Swagger petStore;
  private Swagger vertxTestSpec;

  public Swagger2RequestValidationHandlerTest() {
    //TODO
    petStore = this.loadSwagger("src/test/resources/swaggers/pet-store.json");
    vertxTestSpec = this.loadSwagger("src/test/resources/swaggers/vertx-tests-spec.json");
  }

  private Swagger loadSwagger(String filename) {
    Buffer file = vertx().fileSystem().readFileBlocking(filename);
    return new SwaggerParser().parse(file.toString(Charset.forName("utf-8")));
  }


  @Test
  public void testOperationObjectParsingWithNoParams() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(petStore.getPath("/store/inventory").getGet());
    router.get("/store/inventory").handler(validationHandler);
    router.get("/store/inventory").handler(routingContext -> {
      routingContext.response().setStatusMessage("ok")
        .end();
    }).failureHandler(generateFailureHandler());
    testRequest(HttpMethod.GET, "/store/inventory", 200, "ok");
  }

  @Test
  public void testStringQueryParam() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(vertxTestSpec.getPath("/hello/world").getGet());
    router.get("/hello/world").handler(validationHandler);
    router.get("/hello/world").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("aaa").get(0))
        .end();
    }).failureHandler(generateFailureHandler());
    testRequest(HttpMethod.GET, "/hello/world?aaa=helloworld", 200, "helloworld");
  }

  @Test
  public void testIntQueryParam() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(vertxTestSpec.getPath("/query/int").getGet());
    router.get("/query/int").handler(validationHandler);
    router.get("/query/int").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("param1").get(0))
        .end();
    }).failureHandler(generateFailureHandler());
    String value = getSuccessSample(ParameterType.INT);
    testRequest(HttpMethod.GET, "/query/int?param1=" + value, 200, value);
  }

  @Test
  public void testIntMaximumMinimumQueryParam() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(vertxTestSpec.getPath("/query/int/maximumminimum").getGet());
    router.get("/query/int/maximumminimum").handler(validationHandler);
    router.get("/query/int/maximumminimum").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("param1").get(0))
        .end();
    }).failureHandler(generateFailureHandler());
    String value = "4";
    testRequest(HttpMethod.GET, "/query/int/maximumminimum?param1=" + value, 200, value);
  }

  @Test
  public void testIntMaximumMinimumFailureQueryParam() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(vertxTestSpec.getPath("/query/int/maximumminimum").getGet());
    router.get("/query/int/maximumminimum").handler(validationHandler);
    router.get("/query/int/maximumminimum").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("param1").get(0))
        .end();
    }).failureHandler(generateFailureHandler());
    String value = "11";
    testRequest(HttpMethod.GET, "/query/int/maximumminimum?param1=" + value, 400, "failure:NO_MATCH");
  }

  @Test
  public void testIntMultipleOfFailureQueryParam() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(vertxTestSpec.getPath("/query/int/multipleof").getGet());
    router.get("/query/int/multipleof").handler(validationHandler);
    router.get("/query/int/multipleof").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("param1").get(0))
        .end();
    }).failureHandler(generateFailureHandler());
    String value = "11";
    testRequest(HttpMethod.GET, "/query/int/multipleof?param1=" + value, 400, "failure:NO_MATCH");
  }

  @Test
  public void testQueryStringPatternParamFailure() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(vertxTestSpec.getPath("/query/string/pattern").getGet());
    router.get("/query/string/pattern").handler(validationHandler);
    router.get("/query/string/pattern").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("value").get(0))
        .end();
    }).failureHandler(generateFailureHandler());
    String value = "aabbbb";
    testRequest(HttpMethod.GET, "/query/string/pattern?value=" + value, 400, "failure:NO_MATCH");
  }

  @Test
  public void testQueryStringMaxLengthParamFailure() throws Exception {
    Swagger2RequestValidationHandlerImpl validationHandler = new Swagger2RequestValidationHandlerImpl(vertxTestSpec.getPath("/query/string/maxlength").getGet());
    router.get("/query/string/maxlength").handler(validationHandler);
    router.get("/query/string/maxlength").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("value").get(0))
        .end();
    }).failureHandler(generateFailureHandler());
    String value = "aabbbb";
    testRequest(HttpMethod.GET, "/query/string/maxlength?value=" + value, 400, "failure:NO_MATCH");
  }

}
