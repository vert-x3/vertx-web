package io.vertx.ext.web.api.validation;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.handler.BodyHandler;
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
    testPrimitiveParameterType(ParameterType.BOOL);
  }

  @Test
  public void testEmailValidation() {
    testPrimitiveParameterType(ParameterType.EMAIL);
  }

  @Test
  public void testURIValidation() {
    testPrimitiveParameterType(ParameterType.URI);
  }

  @Test
  public void testIntegerValidation() {
    testPrimitiveParameterType(ParameterType.INT);
  }

  @Test
  public void testFloatValidation() {
    testPrimitiveParameterType(ParameterType.FLOAT);
  }

  @Test
  public void testDoubleValidation() {
    testPrimitiveParameterType(ParameterType.DOUBLE);
  }

  @Test
  public void testDateValidation() {
    testPrimitiveParameterType(ParameterType.DATE);
  }

  @Test
  public void testDateTimeValidation() {
    testPrimitiveParameterType(ParameterType.DATETIME);
  }

  @Test
  public void testTimeValidation() {
    testPrimitiveParameterType(ParameterType.TIME);
  }

  @Test
  public void testBase64Validation() {
    testPrimitiveParameterType(ParameterType.BASE64);
  }

  @Test
  public void testUUIDValidation() {
    testPrimitiveParameterType(ParameterType.UUID);
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
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addPathParam("a",
      ParameterType.GENERIC_STRING).addPathParam("b", ParameterType.BOOL).addPathParam("c", ParameterType.INT);
    router.get("/testPathParams/:a/:b/:c").handler(validationHandler);
    router.get("/testPathParams/:a/:b/:c").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.pathParameter("a").getString() + params.pathParameter("b")
        .getBoolean() + params.pathParameter("c").getInteger()).end();
    }).failureHandler(generateFailureHandler(false));
    String value1 = getSuccessSample(ParameterType.BOOL).getBoolean().toString();
    String value2 = getSuccessSample(ParameterType.INT).getInteger().toString();
    String path = "/testPathParams/hello/" + URLEncoder.encode(value1, "UTF-8") + "/" + URLEncoder.encode(value2,
      "UTF-8");
    testRequest(HttpMethod.GET, path, 200, "hello" + value1 + value2);
  }

  @Test
  public void testPathParamsFailureWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addPathParam("a",
      ParameterType.GENERIC_STRING).addPathParam("b", ParameterType.BOOL).addPathParam("c", ParameterType.INT);
    router.get("/testPathParams/:a/:b/:c").handler(validationHandler);
    router.get("/testPathParams/:a/:b/:c").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.pathParameter("a").getString() + params.pathParameter("b")
        .getBoolean() + params.pathParameter("c").getInteger()).end();
    }).failureHandler(generateFailureHandler(true));
    String path = "/testPathParams/hello/" + URLEncoder.encode(getFailureSample(ParameterType.BOOL), "UTF-8") + "/" +
      URLEncoder.encode(getFailureSample(ParameterType.INT) + "/", "UTF-8");
    testRequest(HttpMethod.GET, path, 400, "failure:NO_MATCH");
  }

  @Test
  public void testQueryParamsWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addQueryParam("param1",
      ParameterType.BOOL, true).addQueryParam("param2", ParameterType.INT, true);
    router.get("/testQueryParams").handler(validationHandler);
    router.get("/testQueryParams").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("param1").getBoolean().toString() + params
        .queryParameter("param2").getInteger().toString()).end();
    }).failureHandler(generateFailureHandler(false));
    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams");
    String param1 = getSuccessSample(ParameterType.BOOL).getBoolean().toString();
    String param2 = getSuccessSample(ParameterType.INT).getInteger().toString();
    encoder.addParam("param1", param1);
    encoder.addParam("param2", param2);
    testRequest(HttpMethod.GET, encoder.toString(), 200, param1 + param2);
  }

  @Test
  public void testQueryParamsFailureWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addQueryParam("param1",
      ParameterType.BOOL, true).addQueryParam("param2", ParameterType.INT, true);
    router.get("/testQueryParams").handler(validationHandler);
    router.get("/testQueryParams").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("param1").getBoolean().toString() + params
        .queryParameter("param2").getInteger().toString());
    }).failureHandler(generateFailureHandler(true));
    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams");
    encoder.addParam("param1", getFailureSample(ParameterType.BOOL));
    encoder.addParam("param2", getFailureSample(ParameterType.INT));
    testRequest(HttpMethod.GET, encoder.toString(), 400, "failure:NO_MATCH");
  }

  @Test
  public void testQueryParamsArrayAndPathParamsWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addPathParam("pathParam1",
      ParameterType.INT).addQueryParamsArray("awesomeArray", ParameterType.EMAIL, true).addQueryParam("anotherParam",
      ParameterType.DOUBLE, true);
    router.get("/testQueryParams/:pathParam1").handler(validationHandler);
    router.get("/testQueryParams/:pathParam1").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.pathParameter("pathParam1").getInteger().toString() + params
        .queryParameter("awesomeArray").getArray().size() + params.queryParameter("anotherParam").getDouble()
        .toString()).end();
    }).failureHandler(generateFailureHandler(false));

    String pathParam = getSuccessSample(ParameterType.INT).getInteger().toString();
    String arrayValue1 = getSuccessSample(ParameterType.EMAIL).getString();
    String arrayValue2 = getSuccessSample(ParameterType.EMAIL).getString();
    String anotherParam = getSuccessSample(ParameterType.DOUBLE).getDouble().toString();

    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams/" + URLEncoder.encode(pathParam, "UTF-8"));
    encoder.addParam("awesomeArray", arrayValue1);
    encoder.addParam("awesomeArray", arrayValue2);
    encoder.addParam("anotherParam", anotherParam);

    testRequest(HttpMethod.GET, encoder.toString(), 200, pathParam + "2" + anotherParam);
  }

  @Test
  public void testQueryParamsArrayAndPathParamsFailureWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addPathParam("pathParam1",
      ParameterType.INT).addQueryParamsArray("awesomeArray", ParameterType.EMAIL, true).addQueryParam("anotherParam",
      ParameterType.DOUBLE, true);
    router.get("/testQueryParams/:pathParam1").handler(validationHandler);
    router.get("/testQueryParams/:pathParam1").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.pathParameter("pathParam1").getInteger().toString() + params
        .queryParameter("awesomeArray").getArray().size() + params.queryParameter("anotherParam").getDouble()
        .toString()).end();
    }).failureHandler(generateFailureHandler(true));

    String pathParam = getSuccessSample(ParameterType.INT).getInteger().toString();
    String arrayValue1 = getFailureSample(ParameterType.EMAIL);
    String arrayValue2 = getSuccessSample(ParameterType.EMAIL).getString();
    String anotherParam = getSuccessSample(ParameterType.DOUBLE).getDouble().toString();

    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams/" + URLEncoder.encode(pathParam, "UTF-8"));
    encoder.addParam("awesomeArray", arrayValue1);
    encoder.addParam("awesomeArray", arrayValue2);
    encoder.addParam("anotherParam", anotherParam);

    testRequest(HttpMethod.GET, encoder.toString(), 400, "failure:NO_MATCH");
  }

  @Test
  public void testFormURLEncodedParamWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addFormParam("parameter",
      ParameterType.INT, true);
    router.route().handler(BodyHandler.create());
    router.post("/testFormParam").handler(validationHandler);
    router.post("/testFormParam").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.formParameter("parameter").getInteger().toString()).end();
    }).failureHandler(generateFailureHandler(false));

    String formParam = getSuccessSample(ParameterType.INT).getInteger().toString();

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("parameter", formParam);

    testRequestWithForm(HttpMethod.POST, "/testFormParam", FormType.FORM_URLENCODED, form, 200, formParam);
  }

  @Test
  public void testFormMultipartParamWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addFormParam("parameter",
      ParameterType.INT, true);
    router.route().handler(BodyHandler.create());
    router.post("/testFormParam").handler(validationHandler);
    router.post("/testFormParam").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.formParameter("parameter").getInteger().toString()).end();
    }).failureHandler(generateFailureHandler(false));

    String formParam = getSuccessSample(ParameterType.INT).getInteger().toString();

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("parameter", formParam);

    testRequestWithForm(HttpMethod.POST, "/testFormParam", FormType.MULTIPART, form, 200, formParam);
  }

  @Test
  public void testFormURLEncodedOverrideWithIncludedTypes() throws Exception {
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addFormParam("parameter",
      ParameterType.INT, true).addQueryParam("parameter", ParameterType.INT, true);
    router.route().handler(BodyHandler.create());
    router.post("/testFormParam").handler(validationHandler);
    router.post("/testFormParam").handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.formParameter("parameter").getInteger().toString()).end();
    }).failureHandler(generateFailureHandler(false));

    String formParam = getSuccessSample(ParameterType.INT).getInteger().toString();
    String queryParam = getSuccessSample(ParameterType.INT).getInteger().toString();

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("parameter", formParam);

    testRequestWithForm(HttpMethod.POST, "/testFormParam?parameter=" + queryParam, FormType.FORM_URLENCODED, form,
      200, formParam);
  }

  @Test
  public void testValidationHandlerChaining() throws Exception {
    HTTPRequestValidationHandler validationHandler1 = HTTPRequestValidationHandler
      .create()
      .addQueryParam("param1", ParameterType.INT, true);
    HTTPRequestValidationHandler validationHandler2 = HTTPRequestValidationHandler
      .create()
      .addQueryParam("param2", ParameterType.BOOL, true);
    router.route().handler(BodyHandler.create());
    router.get("/testHandlersChaining")
      .handler(validationHandler1)
      .handler(validationHandler2)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        assertNotNull(params.queryParameter("param1"));
        assertNotNull(params.queryParameter("param2"));
        routingContext
          .response()
          .setStatusMessage(
            params.queryParameter("param1").getInteger().toString() +
              params.queryParameter("param2").getBoolean()
          ).end();
    }).failureHandler(generateFailureHandler(false));

    String param1 = getSuccessSample(ParameterType.INT).getInteger().toString();
    String param2 = getSuccessSample(ParameterType.BOOL).getBoolean().toString();

    testRequest(HttpMethod.GET, "/testHandlersChaining?param1=10&param2=true", 200, "10true");
  }
}
