package io.vertx.ext.web.validation;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import java.net.URLEncoder;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class HTTPRequestValidationTest extends WebTestValidationBase {

  /*
  ParameterType tests
   */

  @Test
  public void testBoolValidation() {
    testParameterType(ParameterType.BOOL);
  }

  @Test
  public void testEmailValidation() {
    testParameterType(ParameterType.EMAIL);
  }

  @Test
  public void testURIValidation() {
    testParameterType(ParameterType.URI);
  }

  @Test
  public void testIntegerValidation() {
    testParameterType(ParameterType.INT);
  }

  @Test
  public void testFloatValidation() {
    testParameterType(ParameterType.FLOAT);
  }

  @Test
  public void testDoubleValidation() {
    testParameterType(ParameterType.DOUBLE);
  }

  @Test
  public void testDateValidation() {
    testParameterType(ParameterType.DATE);
  }

  @Test
  public void testDateTimeValidation() {
    testParameterType(ParameterType.DATETIME);
  }

  @Test
  public void testTimeValidation() {
    testParameterType(ParameterType.TIME);
  }

  @Test
  public void testBase64Validation() {
    testParameterType(ParameterType.BASE64);
  }

  //TODO write all tests for validation methods

  /*
   For every parameter location there are two tests:
   - success validation
   - failure validation
   All tests are executed with included parameter types
  */

  @Test
  public void testPathParamsWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler =
      HTTPRequestValidationHandler.create()
        .addPathParam("a", ParameterType.GENERIC_STRING)
        .addPathParam("b", ParameterType.BOOL)
        .addPathParam("c", ParameterType.INT);
    router.get("/testPathParams/:a/:b/:c").handler(validationHandler);
    router.get("/testPathParams/:a/:b/:c").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.pathParams().get("a") + routingContext.pathParams().get("b") + routingContext.pathParams().get("c")).end();
    }).failureHandler(generateFailureHandler());
    String value1 = getSuccessSample(ParameterType.BOOL);
    String value2 = getSuccessSample(ParameterType.INT);
    String path = "/testPathParams/hello/" + URLEncoder.encode(value1, "UTF-8") + "/" + URLEncoder.encode(value2, "UTF-8");
    testRequest(HttpMethod.GET, path, 200, "hello" + value1 + value2);
  }

  @Test
  public void testPathParamsFailureWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler =
      HTTPRequestValidationHandler.create()
        .addPathParam("a", ParameterType.GENERIC_STRING)
        .addPathParam("b", ParameterType.BOOL)
        .addPathParam("c", ParameterType.INT);
    router.get("/testPathParams/:a/:b/:c").handler(validationHandler);
    router.get("/testPathParams/:a/:b/:c").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.pathParam("a") + routingContext.pathParam("b") + routingContext.pathParam("c")).end();
    }).failureHandler(generateFailureHandler());
    String path = "/testPathParams/hello/" + URLEncoder.encode(getFailureSample(ParameterType.BOOL), "UTF-8") + "/" + URLEncoder.encode(getFailureSample(ParameterType.INT) + "/", "UTF-8");
    testRequest(HttpMethod.GET, path, 400, "failure:NO_MATCH");
  }

  @Test
  public void testQueryParamsWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create()
      .addQueryParam("param1", ParameterType.BOOL, true).addQueryParam("param2", ParameterType.INT, true);
    router.get("/testQueryParams").handler(validationHandler);
    router.get("/testQueryParams").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("param1").get(0) + routingContext.queryParam("param2").get(0)).end();
    }).failureHandler(generateFailureHandler());
    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams");
    String param1 = getSuccessSample(ParameterType.BOOL);
    String param2 = getSuccessSample(ParameterType.INT);
    encoder.addParam("param1", param1);
    encoder.addParam("param2", param2);
    testRequest(HttpMethod.GET, encoder.toString(), 200, param1 + param2);
  }

  @Test
  public void testQueryParamsFailureWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create()
      .addQueryParam("param1", ParameterType.BOOL, true).addQueryParam("param2", ParameterType.INT, true);
    router.get("/testQueryParams").handler(validationHandler);
    router.get("/testQueryParams").handler(routingContext -> {
      routingContext.response().setStatusMessage(routingContext.queryParam("param1").get(0) + routingContext.queryParam("param2").get(0)).end();
    }).failureHandler(generateFailureHandler());
    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams");
    encoder.addParam("param1", getFailureSample(ParameterType.BOOL));
    encoder.addParam("param2", getFailureSample(ParameterType.INT));
    testRequest(HttpMethod.GET, encoder.toString(), 400, "failure:NO_MATCH");
  }

  @Test
  public void testQueryParamsArrayAndPathParamsWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create()
      .addPathParam("pathParam1", ParameterType.INT)
      .addQueryParamsArray("awesomeArray", ParameterType.EMAIL, true)
      .addQueryParam("anotherParam", ParameterType.DOUBLE, true);
    router.get("/testQueryParams/:pathParam1").handler(validationHandler);
    router.get("/testQueryParams/:pathParam1").handler(routingContext -> {
      routingContext.response().setStatusMessage(
        routingContext.pathParam("pathParam1") +
          routingContext.queryParam("awesomeArray").size() +
          routingContext.queryParam("anotherParam").get(0))
        .end();
    }).failureHandler(generateFailureHandler());

    String pathParam = getSuccessSample(ParameterType.INT);
    String arrayValue1 = getSuccessSample(ParameterType.EMAIL);
    String arrayValue2 = getSuccessSample(ParameterType.EMAIL);
    String anotherParam = getSuccessSample(ParameterType.DOUBLE);

    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams/" + URLEncoder.encode(pathParam, "UTF-8"));
    encoder.addParam("awesomeArray", arrayValue1);
    encoder.addParam("awesomeArray", arrayValue2);
    encoder.addParam("anotherParam", anotherParam);

    testRequest(HttpMethod.GET, encoder.toString(), 200, pathParam + "2" + anotherParam);
  }

  @Test
  public void testQueryParamsArrayAndPathParamsFailureWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create()
      .addPathParam("pathParam1", ParameterType.INT)
      .addQueryParamsArray("awesomeArray", ParameterType.EMAIL, true)
      .addQueryParam("anotherParam", ParameterType.DOUBLE, true);
    router.get("/testQueryParams/:pathParam1").handler(validationHandler);
    router.get("/testQueryParams/:pathParam1").handler(routingContext -> {
      routingContext.response().setStatusMessage(
        routingContext.pathParam("pathParam1") +
          routingContext.queryParam("awesomeArray").size() +
          routingContext.queryParam("anotherParam").get(0))
        .end();
    }).failureHandler(generateFailureHandler());

    String pathParam = getSuccessSample(ParameterType.INT);
    String arrayValue1 = getSuccessSample(ParameterType.EMAIL);
    String arrayValue2 = getFailureSample(ParameterType.EMAIL);
    String anotherParam = getSuccessSample(ParameterType.DOUBLE);

    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams/" + URLEncoder.encode(pathParam, "UTF-8"));
    encoder.addParam("awesomeArray", arrayValue1);
    encoder.addParam("awesomeArray", arrayValue2);
    encoder.addParam("anotherParam", anotherParam);

    testRequest(HttpMethod.GET, encoder.toString(), 400, "failure:NO_MATCH");
  }



}
