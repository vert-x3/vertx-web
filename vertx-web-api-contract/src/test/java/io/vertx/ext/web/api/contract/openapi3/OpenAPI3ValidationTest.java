package io.vertx.ext.web.api.contract.openapi3;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenAPI3RequestValidationHandlerImpl;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenApi3Utils;
import io.vertx.ext.web.api.validation.ParameterType;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.api.validation.WebTestValidationBase;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.List;

/**
 * This tests are about OpenAPI 3 validation behaviours. The validation handlers are not constructed through OpenAPI3RouterFactory.
 * For router factory behaviours, please give a look at OpenAPI3RouterFactoryTest
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3ValidationTest extends WebTestValidationBase {

  OpenAPI testSpec;

  @Rule
  public ExternalResource resource = new ExternalResource() {
    @Override
    protected void before() throws Throwable {
      testSpec = loadSwagger("src/test/resources/swaggers/validation_test.yaml");
    }

    @Override
    protected void after() {
    }

  };

  private OpenAPI loadSwagger(String filename) {
    return new OpenAPIV3Parser().readLocation(filename, null, OpenApi3Utils.getParseOptions()).getOpenAPI();
  }

  @Test
  public void testLoadSampleOperationObject() throws Exception {
    Operation op = testSpec.getPaths().get("/pets").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    router.get("/pets").handler(validationHandler);
    router.get("/pets").handler(routingContext -> routingContext.response().setStatusMessage("ok").end()).failureHandler(generateFailureHandler(false));
    testRequest(HttpMethod.GET, "/pets", 200, "ok");
  }

  @Test
  public void testPathParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/pets/{petId}").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/pets/:petId", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.pathParameter("petId").getInteger().toString()).end();
    });

    testRequest(HttpMethod.GET, "/pets/3", 200, "3");

  }

  @Test
  public void testPathParameterFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/pets/{petId}").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/pets/:petId", HttpMethod.GET, true, validationHandler, (routingContext) -> routingContext.response().setStatusMessage("ok").end());
    testRequest(HttpMethod.GET, "/pets/three", 400, errorMessage(ValidationException.ErrorType.NO_MATCH));
  }

  @Test
  public void testQueryParameterNotRequired() throws Exception {
    Operation op = testSpec.getPaths().get("/pets").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/pets", HttpMethod.GET, false, validationHandler, (routingContext) -> routingContext.response().setStatusMessage("ok").end());
    testRequest(HttpMethod.GET, "/pets", 200, "ok");
  }

  @Test
  public void testQueryParameterArrayExploded() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/arrayTests/formExploded").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/arrayTests/formExploded", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      List<String> result = new ArrayList<>();
      for (RequestParameter r : params.queryParameter("parameter").getArray())
        result.add(r.getInteger().toString());
      routingContext.response().setStatusMessage(serializeInCSVStringArray(result)).end();
    });
    List<String> values = new ArrayList<>();
    values.add("4");
    values.add("2");
    values.add("26");

    StringBuilder stringBuilder = new StringBuilder();
    for (String s : values) {
      stringBuilder.append("parameter=" + s + "&");
    }
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);

    testRequest(HttpMethod.GET, "/queryTests/arrayTests/formExploded?" + stringBuilder, 200,
      serializeInCSVStringArray(values));
  }

  @Test
  public void testQueryParameterArrayDefaultStyle() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/arrayTests/default").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/arrayTests/default", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      List<String> result = new ArrayList<>();
      for (RequestParameter r : params.queryParameter("parameter").getArray())
        result.add(r.getInteger().toString());
      routingContext.response().setStatusMessage(serializeInCSVStringArray(result)).end();
    });
    List<String> values = new ArrayList<>();
    values.add("4");
    values.add("2");
    values.add("26");

    testRequest(HttpMethod.GET, "/queryTests/arrayTests/default?parameter=" + serializeInCSVStringArray(values), 200,
      serializeInCSVStringArray(values));
  }

  @Test
  public void testQueryParameterArrayDefaultStyleFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/arrayTests/default").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/arrayTests/default", HttpMethod.GET, true, validationHandler, (routingContext) -> routingContext.response().setStatusMessage("ok").end());
    List<String> values = new ArrayList<>();
    values.add("4");
    values.add("1"); // multipleOf: 2
    values.add("26");

    testRequest(HttpMethod.GET, "/queryTests/arrayTests/default?parameter=" + serializeInCSVStringArray(values), 400,
      errorMessage(ValidationException.ErrorType.NO_MATCH));
  }

  @Test
  public void testDefaultStringQueryParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/defaultString").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/defaultString", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("parameter").getString()).end();
    });
    testRequest(HttpMethod.GET, "/queryTests/defaultString", 200, "aString");
  }

  @Test
  public void testAllowEmptyValueQueryParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/defaultString").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/defaultString", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("parameter").getString()).end();
    });
    // Empty value should not be overwritten
    testRequest(HttpMethod.GET, "/queryTests/defaultString?parameter=", 200, "");
  }

  @Test
  public void testDefaultIntQueryParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/defaultInt").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/defaultInt", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      RequestParameter requestParameter = params.queryParameter("parameter");
      assertTrue(requestParameter.isInteger());
      routingContext.response().setStatusMessage(requestParameter.toString()).end();
    });

    testRequest(HttpMethod.GET, "/queryTests/defaultInt", 200, "1");
  }

  @Test
  public void testDefaultFloatQueryParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/defaultFloat").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/defaultFloat", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      RequestParameter requestParameter = params.queryParameter("parameter");
      assertTrue(requestParameter.isFloat());
      routingContext.response().setStatusMessage(requestParameter.toString()).end();
    });

    testRequest(HttpMethod.GET, "/queryTests/defaultFloat", 200, "1.0");
  }

  @Test
  public void testDefaultDoubleQueryParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/defaultDouble").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/defaultDouble", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      RequestParameter requestParameter = params.queryParameter("parameter");
      assertTrue(requestParameter.isDouble());
      routingContext.response().setStatusMessage(requestParameter.toString()).end();
    });

    testRequest(HttpMethod.GET, "/queryTests/defaultDouble", 200, "1.0");
  }

  @Test
  public void testQueryParameterByteFormat() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/byteFormat").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/byteFormat", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("parameter").getString()).end();
    });

    testRequest(HttpMethod.GET, "/queryTests/byteFormat?parameter=Zm9vYmFyCg==", 200, "Zm9vYmFyCg==");
  }

  @Test
  public void testFormArrayParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/formTests/arraytest").getPost();
    if(op.getParameters()==null) op.setParameters(new ArrayList<>());
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/formTests/arraytest", HttpMethod.POST, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      List<String> result = new ArrayList<>();
      for (RequestParameter r : params.formParameter("values").getArray())
        result.add(r.getInteger().toString());
      routingContext.response().setStatusMessage(params.formParameter("id").getString() + serializeInCSVStringArray
        (result)).end();
    });

    String id = "anId";

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    String values = serializeInCSVStringArray(valuesArray);

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("id", id);
    form.add("values", values);

    testRequestWithForm(HttpMethod.POST, "/formTests/arraytest", FormType.FORM_URLENCODED, form, 200, id + values);
  }

  @Test
  public void testFormArrayParameterFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/formTests/arraytest").getPost();
    if(op.getParameters()==null) op.setParameters(new ArrayList<>());
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/formTests/arraytest", HttpMethod.POST, true, validationHandler, (routingContext) -> routingContext.response().setStatusMessage("ok").end());

    String id = "anId";

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    valuesArray.add(getFailureSample(ParameterType.INT));
    String values = serializeInCSVStringArray(valuesArray);

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("id", id);
    form.add("values", values);

    testRequestWithForm(HttpMethod.POST, "/formTests/arraytest", FormType.FORM_URLENCODED, form, 400, errorMessage
      (ValidationException.ErrorType.NO_MATCH));
  }

  @Test
  public void testFormURLEncodedCharParameter() throws Exception {
    Operation op = testSpec.getPaths().get("/formTests/urlencodedchar").getPost();
    if(op.getParameters()==null) op.setParameters(new ArrayList<>());
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/formTests/urlencodedchar", HttpMethod.POST, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.formParameter("name").getString()).end();
    });

    String name = "test+urlencoded+char";

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("name", name);

    testRequestWithForm(HttpMethod.POST, "/formTests/urlencodedchar", FormType.FORM_URLENCODED, form, 200, name);
  }

  @Test
  public void testJsonBody() throws Exception {
    Operation op = testSpec.getPaths().get("/jsonBodyTest/sampleTest").getPost();
    if(op.getParameters()==null) op.setParameters(new ArrayList<>());
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/jsonBodyTest/sampleTest", HttpMethod.POST, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("Content-Type", "application/json")
        .end(params.body().getJsonObject().encode());
    });

    JsonObject object = new JsonObject();
    object.put("id", "anId");

    List<Integer> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger());
    object.put("values", valuesArray);

    testRequestWithJSON(HttpMethod.POST, "/jsonBodyTest/sampleTest", object.toBuffer(), 200, "OK", object.toBuffer());
    testRequestWithBufferResponse(HttpMethod.POST,
      "/jsonBodyTest/sampleTest",
      "application/json; charset=utf-8",
      object.toBuffer(), 200, "OK", object.toBuffer(), "application/json");
    testRequestWithBufferResponse(HttpMethod.POST,
      "/jsonBodyTest/sampleTest",
      "application/superapplication+json",
      object.toBuffer(), 200, "OK", object.toBuffer(), "application/json");
  }

  @Test
  public void testJsonBodyFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/jsonBodyTest/sampleTest").getPost();
    if(op.getParameters()==null) op.setParameters(new ArrayList<>());
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/jsonBodyTest/sampleTest", HttpMethod.POST, true, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("Content-Type", "application/json")
        .end(params.body().getJsonObject().encode());
    });

    JsonObject object = new JsonObject();
    object.put("id", "anId");

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    valuesArray.add(2, getFailureSample(ParameterType.INT));
    object.put("values", valuesArray);

    testRequestWithJSON(HttpMethod.POST, "/jsonBodyTest/sampleTest", object.toBuffer(), 400, errorMessage(ValidationException.ErrorType.JSON_INVALID));
  }

  @Test
  public void testAllOfQueryParam() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/allOfTest").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/allOfTest", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("parameter").getObjectValue("a").getInteger()
        .toString() + params.queryParameter("parameter").getObjectValue("b").getBoolean().toString()).end();
    });

    String a = "5";
    String b = "false";

    String parameter = "parameter=a," + a + ",b," + b;

    testRequest(HttpMethod.GET, "/queryTests/allOfTest?" + parameter, 200, a + b);
  }

  @Test
  public void testAllOfQueryParamWithDefault() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/allOfTest").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/allOfTest", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("parameter").getObjectValue("a").getInteger()
        .toString() + params.queryParameter("parameter").getObjectValue("b").getBoolean().toString()).end();
    });

    String a = "5";
    String b = "";

    String parameter = "parameter=a," + a + ",b," + b;

    testRequest(HttpMethod.GET, "/queryTests/allOfTest?" + parameter, 200, a + "false");
  }

  @Test
  public void testAllOfQueryParamFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/allOfTest").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/allOfTest", HttpMethod.GET, true, validationHandler, (routingContext) -> routingContext.response().setStatusMessage("ok").end());

    String a = "5";
    String b = "aString";

    String parameter = "parameter=a," + a + ",b," + b;

    testRequest(HttpMethod.GET, "/queryTests/allOfTest?" + parameter, 400, errorMessage(ValidationException.ErrorType
      .NO_MATCH));
  }

  @Test
  public void testQueryParameterAnyOf() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/anyOfTest").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/anyOfTest", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.queryParameter("parameter").getBoolean().toString()).end();
    });

    testRequest(HttpMethod.GET, "/queryTests/anyOfTest?parameter=true", 200, "true");
  }

  @Test
  public void testQueryParameterAnyOfFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/anyOfTest").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/anyOfTest", HttpMethod.GET, true, validationHandler, (routingContext) -> routingContext.response().setStatusMessage("ok").end());

    testRequest(HttpMethod.GET, "/queryTests/anyOfTest?parameter=anyString", 400, errorMessage(ValidationException
      .ErrorType.NO_MATCH));
  }

  @Test
  public void testComplexMultipart() throws Exception {
    Operation op = testSpec.getPaths().get("/multipart/complex").getPost();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/multipart/complex", HttpMethod.POST, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      assertEquals(params.formParameter("param1").getString(), "sampleString");
      assertNotNull(params.formParameter("param2").getJsonObject());
      assertEquals(params.formParameter("param2").getJsonObject().getString("name"), "Willy");
      assertEquals(params.formParameter("param4").getArray().size(), 4);
      routingContext.response().setStatusMessage("ok").end();
    });
    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("param1", "sampleString");

    JsonObject pet = new JsonObject();
    pet.put("id", 14612);
    pet.put("name", "Willy");

    form.add("param2", pet.encode());

    form.add("param3", "SELECT * FROM table;");

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.FLOAT).getFloat().toString());
    form.add("param4", serializeInCSVStringArray(valuesArray));

    testRequestWithForm(HttpMethod.POST, "/multipart/complex", FormType.MULTIPART, form, 200, "ok");
  }

  @Test
  public void testEmptyBody() throws Exception {
    Operation op = testSpec.getPaths().get("/multipart/complex").getPost();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/multipart/complex", HttpMethod.POST, false, validationHandler, (routingContext) -> routingContext.response().setStatusMessage("ok").end());

    testRequest(HttpMethod.POST, "/multipart/complex", 200, "ok");
  }

  @Test
  @Ignore
  public void testCircularReferences() throws Exception {
    Operation op = testSpec.getPaths().get("/circularReferences").getPost();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/circularReferences", HttpMethod.POST, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("Content-Type", "application/json")
        .end(params.body().getJsonObject().encode());
    });

    JsonObject obj = new JsonObject("{\n" +
      "    \"a\": {\n" +
      "        \"a\": [\n" +
      "            {\n" +
      "                \"a\": {\n" +
      "                    \"a\": []\n" +
      "                },\n" +
      "                \"b\": \"hi\",\n" +
      "                \"c\": 10\n" +
      "            }\n" +
      "        ]\n" +
      "    },\n" +
      "    \"b\": \"hello\",\n" +
      "    \"c\": 6\n" +
      "}");

    testRequestWithJSON(HttpMethod.POST, "/circularReferences", obj.toBuffer(), 200, "OK", obj.toBuffer());
  }

  @Test
  public void testNullJson() throws Exception {
    Operation op = testSpec.getPaths().get("/pets").getPost();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/pets", HttpMethod.POST, true, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(params.body().getJsonObject().encode());
    });

    // An empty body should be a non parsable json, not an empty object invalid
    testRequestWithJSON(HttpMethod.POST, "/pets", null, 400, errorMessage(ValidationException.ErrorType.JSON_INVALID));

    // An empty json object should be invalid, because some fields are required
    testRequestWithJSON(HttpMethod.POST, "/pets", new JsonObject().toBuffer(),400, errorMessage(ValidationException.ErrorType.JSON_INVALID));
  }

  @Test
  public void testEmptyParametersNotNull() throws Exception {
    Operation op = testSpec.getPaths().get("/pets").getPost();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/pets", HttpMethod.POST, true, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      assertEquals(0, params.cookieParametersNames().size()); //Here it should not throw exception (issue #850)
      assertEquals(0, params.pathParametersNames().size());
      assertEquals(0, params.queryParametersNames().size());
      assertEquals(0, params.headerParametersNames().size());
      assertEquals(0, params.formParametersNames().size());
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end();
    });

    testRequestWithJSON(HttpMethod.POST, "/pets", new JsonObject().put("id", 1).put("name", "Willy").toBuffer(),200, "OK");
  }

  @Test
  public void testAdditionalPropertiesJson() throws Exception {
    Operation op = testSpec.getPaths().get("/additionalProperties").getPost();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/additionalProperties", HttpMethod.POST, true, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(params.body().getJsonObject().encode());
    });

    JsonObject pet = new JsonObject();
    pet.put("id", 14612);
    pet.put("name", "Willy");
    pet.put("lazyness",  "Highest");

    testRequestWithJSON(HttpMethod.POST, "/additionalProperties", pet.toBuffer(), 400, errorMessage(ValidationException.ErrorType.JSON_INVALID));
  }

  @Test
  public void testJsonBodyFailureErrorMessage() throws Exception {
    Operation op = testSpec.getPaths().get("/jsonBodyTest/sampleTest").getPost();
    if(op.getParameters()==null) op.setParameters(new ArrayList<>());
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/jsonBodyTest/sampleTest", HttpMethod.POST, true, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("Content-Type", "application/json")
        .end(params.body().getJsonObject().encode());
    }, routingContext -> {
      ValidationException e = (ValidationException) routingContext.failure();
      routingContext
        .response()
        .setStatusCode(400)
        .setStatusMessage(errorMessage(ValidationException.ErrorType.JSON_INVALID))
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("field", e.parameterName()).toBuffer());
    });

    JsonObject object = new JsonObject();
    object.put("id", "anId");

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    valuesArray.add(0, getFailureSample(ParameterType.INT));
    object.put("values", valuesArray);

    testRequestWithJSON(HttpMethod.POST, "/jsonBodyTest/sampleTest", object.toBuffer(), 400,
      errorMessage(ValidationException.ErrorType.JSON_INVALID),
      new JsonObject().put("field", "body.values[0]").toBuffer());

    testRequestWithJSON(HttpMethod.POST, "/jsonBodyTest/sampleTest", new JsonArray().toBuffer(), 400,
      errorMessage(ValidationException.ErrorType.JSON_INVALID),
      new JsonObject().put("field", "body").toBuffer());
  }

  @Test
  public void testQueryExpandedObjectTestOnlyAdditionalProperties() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/objectTests/onlyAdditionalProperties").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/objectTests/onlyAdditionalProperties",
      HttpMethod.GET,
      false,
      validationHandler,
      (routingContext) -> {
        RequestParameters params = routingContext.get("parsedParameters");
        assertEquals("hello", params.queryParameter("wellKnownParam").getString());
        RequestParameter param = params.queryParameter("params");
        assertFalse(param.getObjectKeys().contains("wellKnownParam"));
        int res = param.getObjectValue("param2").getInteger() + param.getObjectValue("param1").getInteger();
        routingContext.response().setStatusCode(200).setStatusMessage("Result: " + res).end();
      }
    );

    testRequest(HttpMethod.GET, "/queryTests/objectTests/onlyAdditionalProperties?param1=2&param2=4&wellKnownParam=hello", 200, "Result: 6");
  }

  @Test
  public void testQueryExpandedObjectTestOnlyAdditionalPropertiesFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/queryTests/objectTests/onlyAdditionalProperties").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/queryTests/objectTests/onlyAdditionalProperties",
      HttpMethod.GET,
      true,
      validationHandler,
      (routingContext) -> {
        routingContext.response().setStatusCode(200).setStatusMessage("OK").end();
      }
    );

    testRequest(HttpMethod.GET, "/queryTests/objectTests/onlyAdditionalProperties?param1=2&param2=a&wellKnownParam=a", 400, errorMessage(ValidationException.ErrorType.NO_MATCH));
  }

  @Test
  public void testCookieExpandedObjectTestOnlyAdditionalProperties() throws Exception {
    Operation op = testSpec.getPaths().get("/cookieTests/objectTests/onlyAdditionalProperties").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/cookieTests/objectTests/onlyAdditionalProperties",
      HttpMethod.GET,
      false,
      validationHandler,
      (routingContext) -> {
        RequestParameters params = routingContext.get("parsedParameters");
        assertEquals("hello", params.cookieParameter("wellKnownParam").toString());
        RequestParameter param = params.cookieParameter("params");
        assertFalse(param.getObjectKeys().contains("wellKnownParam"));
        int res = param.getObjectValue("param2").getInteger() + param.getObjectValue("param1").getInteger();
        routingContext.response().setStatusCode(200).setStatusMessage(Integer.toString(res)).end();
      }
    );

    QueryStringEncoder params = new QueryStringEncoder("/");
    params.addParam("param1", Integer.toString(5));
    params.addParam("param2", Integer.toString(1));
    params.addParam("wellKnownParam", "hello");

    testRequestWithCookies(HttpMethod.GET, "/cookieTests/objectTests/onlyAdditionalProperties", params.toUri().getRawQuery(), 200, "6");
  }

  @Test
  public void testCookieExpandedObjectTestOnlyAdditionalPropertiesFailure() throws Exception {
    Operation op = testSpec.getPaths().get("/cookieTests/objectTests/onlyAdditionalProperties").getGet();
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(op, op.getParameters(), testSpec);
    loadHandlers("/cookieTests/objectTests/onlyAdditionalProperties",
      HttpMethod.GET,
      true,
      validationHandler,
      (routingContext) -> {
        routingContext.response().setStatusCode(200).setStatusMessage("OK").end();
      }
    );

    QueryStringEncoder params = new QueryStringEncoder("/");
    params.addParam("param1", Integer.toString(5));
    params.addParam("param2", "a");
    params.addParam("wellKnownParam", "hello");

    testRequestWithCookies(HttpMethod.GET, "/cookieTests/objectTests/onlyAdditionalProperties", params.toUri().getRawQuery(), 400, errorMessage(ValidationException.ErrorType.NO_MATCH));
  }

}
