package io.vertx.ext.web.validation;

import com.reprezen.kaizen.oasparser.OpenApiParser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.RequestParameters;
import io.vertx.ext.web.validation.impl.OpenAPI3RequestValidationHandlerImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import sun.reflect.annotation.ExceptionProxy;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class OpenAPI3ValidationTest extends WebTestValidationBase {

  OpenApi3 petStore;
  OpenApi3 testSpec;

  @Rule
  public ExternalResource resource = new ExternalResource() {
    @Override
    protected void before() throws Throwable {
      petStore = loadSwagger("src/test/resources/swaggers/petstore.yaml");
      testSpec = loadSwagger("src/test/resources/swaggers/testSpec.yaml");
    }

    ;

    @Override
    protected void after() {
    }

    ;
  };

  private OpenApi3 loadSwagger(String filename) {
    return (OpenApi3) new OpenApiParser().parse(new File(filename), true);
  }

  @Test
  public void testLoadSampleOperationObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(petStore.getPath("/pets").getGet(), null);
    router.get("/pets").handler(validationHandler);
    router.get("/pets").handler(routingContext -> {
      routingContext.response().setStatusMessage("ok")
        .end();
    }).failureHandler(generateFailureHandler(false));
    testRequest(HttpMethod.GET, "/pets", 200, "ok");
  }

  @Test
  public void testPathParameter() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(petStore.getPath("/pets/{petId}").getGet(), null);
    loadHandlers("/pets/:petId", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.getPathParameter("petId").getString()).end();
    });

    testRequest(HttpMethod.GET, "/pets/aPetId", 200, "aPetId");

  }

  @Test
  public void testPathParameterFailure() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/pets/{petId}").getGet(), null);
    loadHandlers("/pets/:petId", HttpMethod.GET, true, validationHandler, (routingContext) -> {
      routingContext.response().setStatusMessage("ok").end();
    });
    testRequest(HttpMethod.GET, "/pets/3", 400, errorMessage(ValidationException.ErrorType.NO_MATCH));
  }

  @Test
  public void testQueryParameterNotRequired() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(petStore.getPath("/pets").getGet(), null);
    loadHandlers("/pets", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      routingContext.response().setStatusMessage("ok").end();
    });
    testRequest(HttpMethod.GET, "/pets", 200, "ok");
  }

  @Test
  public void testQueryParameterArrayExploded() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/queryTests/arrayTests/formExploded").getGet(), null);
    loadHandlers("/queryTests/arrayTests/formExploded", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      List<String> result = new ArrayList<>();
      for (RequestParameter r : params.getQueryParameter("parameter").getArray())
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

    testRequest(HttpMethod.GET, "/queryTests/arrayTests/formExploded?" + stringBuilder, 200, serializeInCSVStringArray(values));
  }

  @Test
  public void testQueryParameterArrayDefaultStyle() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/queryTests/arrayTests/default").getGet(), null);
    loadHandlers("/queryTests/arrayTests/default", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      List<String> result = new ArrayList<>();
      for (RequestParameter r : params.getQueryParameter("parameter").getArray())
        result.add(r.getInteger().toString());
      routingContext.response().setStatusMessage(serializeInCSVStringArray(result)).end();
    });
    List<String> values = new ArrayList<>();
    values.add("4");
    values.add("2");
    values.add("26");

    testRequest(HttpMethod.GET, "/queryTests/arrayTests/default?parameter=" + serializeInCSVStringArray(values), 200, serializeInCSVStringArray(values));
  }

  @Test
  public void testQueryParameterArrayDefaultStyleFailure() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/queryTests/arrayTests/default").getGet(), null);
    loadHandlers("/queryTests/arrayTests/default", HttpMethod.GET, true, validationHandler, (routingContext) -> {
      routingContext.response().setStatusMessage("ok").end();
    });
    List<String> values = new ArrayList<>();
    values.add("4");
    values.add("1"); // multipleOf: 2
    values.add("26");

    testRequest(HttpMethod.GET, "/queryTests/arrayTests/default?parameter=" + serializeInCSVStringArray(values), 400, errorMessage(ValidationException.ErrorType.NO_MATCH));
  }

  @Test
  public void testDefaultQueryParameter() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/queryTests/default").getGet(), null);
    loadHandlers("/queryTests/default", HttpMethod.GET, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.getQueryParameter("parameter").getString()).end();
    });

    testRequest(HttpMethod.GET, "/queryTests/default?parameter=", 200, "aString");
    testRequest(HttpMethod.GET, "/queryTests/default", 200, "aString");
  }

  @Test
  public void testFormArrayParameter() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/formTests/arraytest").getPost(), null);
    loadHandlers("/formTests/arraytest", HttpMethod.POST, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      List<String> result = new ArrayList<>();
      for (RequestParameter r : params.getFormParameter("values").getArray())
        result.add(r.getInteger().toString());
      routingContext.response().setStatusMessage(params.getFormParameter("id").getString() + serializeInCSVStringArray(result)).end();
    });

    String id = "anId";

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    String values = serializeInCSVStringArray(valuesArray);

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("id", id);
    form.add("values", URLEncoder.encode(values, "UTF-8"));

    testRequestWithForm(HttpMethod.POST, "/formTests/arraytest", FormType.FORM_URLENCODED, form, 200, id + values);
  }

  @Test
  public void testFormArrayParameterFailure() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/formTests/arraytest").getPost(), null);
    loadHandlers("/formTests/arraytest", HttpMethod.POST, true, validationHandler, (routingContext) -> {
      routingContext.response().setStatusMessage("ok").end();
    });

    String id = "anId";

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    valuesArray.add(getFailureSample(ParameterType.INT));
    String values = serializeInCSVStringArray(valuesArray);

    MultiMap form = MultiMap.caseInsensitiveMultiMap();
    form.add("id", id);
    form.add("values", URLEncoder.encode(values, "UTF-8"));

    testRequestWithForm(HttpMethod.POST, "/formTests/arraytest", FormType.FORM_URLENCODED, form, 400, errorMessage(ValidationException.ErrorType.NO_MATCH));
  }

  @Test
  public void testJsonBody() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/jsonBodyTest/sampleTest").getPost(), null);
    loadHandlers("/jsonBodyTest/sampleTest", HttpMethod.POST, false, validationHandler, (routingContext) -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(params.getBody().getJsonObject().encode()).end();
    });

    JsonObject object = new JsonObject();
    object.put("id", "anId");

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    object.put("values", valuesArray);

    testRequestWithJSON(HttpMethod.POST, "/jsonBodyTest/sampleTest", object, 200, object.encode());
  }

  @Test
  public void testJsonBodyFailure() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(testSpec.getPath("/jsonBodyTest/sampleTest").getPost(), null);
    loadHandlers("/jsonBodyTest/sampleTest", HttpMethod.POST, true, validationHandler, (routingContext) -> {
      routingContext.response().setStatusMessage("ok").end();
      ;
    });

    JsonObject object = new JsonObject();
    object.put("id", "anId");

    List<String> valuesArray = new ArrayList<>();
    for (int i = 0; i < 4; i++)
      valuesArray.add(getSuccessSample(ParameterType.INT).getInteger().toString());
    valuesArray.add(2, getFailureSample(ParameterType.INT));
    object.put("values", valuesArray);

    testRequestWithJSON(HttpMethod.POST, "/jsonBodyTest/sampleTest", object, 400, errorMessage(ValidationException.ErrorType.JSON_INVALID));
  }

}
