package io.vertx.ext.web.designdriven.openapi3;

import com.reprezen.kaizen.oasparser.OpenApiParser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.RequestParameters;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.designdriven.openapi3.impl.OpenAPI3RequestValidationHandlerImpl;
import io.vertx.ext.web.validation.WebTestValidationBase;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@Ignore
public class OpenAPI3ParametersUnitTest extends WebTestValidationBase {

  OpenApi3 spec;
  ApiClient client;

  private OpenApi3 loadSwagger(String filename) {
    return (OpenApi3) new OpenApiParser().parse(new File(filename), false);
  }

  @Rule
  public ExternalResource resource = new ExternalResource() {
    @Override
    protected void before() throws Throwable {
      spec = loadSwagger("src/test/resources/swaggers/openapi.yaml");
    }

    @Override
    protected void after() {
    }
  };

  @Override
  public void setUp() throws Exception {
    super.setUp();
    client = new ApiClient(webClient);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }


  /**
   * Test: path_matrix_noexplode_empty
   * Expected parameters sent:
   * color: ;color
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixNoexplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/noexplode/empty/{color}").getGet(), null);
    router.get("/path/matrix/noexplode/empty/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "";


    client.pathMatrixNoexplodeEmpty(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_matrix_noexplode_string
   * Expected parameters sent:
   * color: ;color=blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixNoexplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/noexplode/string/{color}").getGet(), null);
    router.get("/path/matrix/noexplode/string/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isString());
      assertEquals(color_path.getString(), "blue");
      res.put("color", color_path.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "blue";


    client.pathMatrixNoexplodeString(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_matrix_noexplode_array
   * Expected parameters sent:
   * color: ;color=blue,black,brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/noexplode/array/{color}").getGet(), null);
    router.get("/path/matrix/noexplode/array/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isArray());
      res.put("color", new JsonArray(color_path.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_path;
    color_path = new ArrayList<>();
    color_path.add("blue");
    color_path.add("black");
    color_path.add("brown");


    client.pathMatrixNoexplodeArray(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_matrix_noexplode_object
   * Expected parameters sent:
   * color: ;color=R,100,G,200,B,150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/noexplode/object/{color}").getGet(), null);
    router.get("/path/matrix/noexplode/object/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_path.getObjectKeys())
        map.put(key, color_path.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_path;
    color_path = new HashMap<>();
    color_path.put("R", "100");
    color_path.put("G", "200");
    color_path.put("B", "150");


    client.pathMatrixNoexplodeObject(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_matrix_explode_empty
   * Expected parameters sent:
   * color: ;color
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixExplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/explode/empty/{color}").getGet(), null);
    router.get("/path/matrix/explode/empty/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "";


    client.pathMatrixExplodeEmpty(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_matrix_explode_string
   * Expected parameters sent:
   * color: ;color=blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixExplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/explode/string/{color}").getGet(), null);
    router.get("/path/matrix/explode/string/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isString());
      assertEquals(color_path.getString(), "blue");
      res.put("color", color_path.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "blue";


    client.pathMatrixExplodeString(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_matrix_explode_array
   * Expected parameters sent:
   * color: ;color=blue;color=black;color=brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixExplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/explode/array/{color}").getGet(), null);
    router.get("/path/matrix/explode/array/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isArray());
      res.put("color", new JsonArray(color_path.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_path;
    color_path = new ArrayList<>();
    color_path.add("blue");
    color_path.add("black");
    color_path.add("brown");


    client.pathMatrixExplodeArray(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_matrix_explode_object
   * Expected parameters sent:
   * color: ;R=100;G=200;B=150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testPathMatrixExplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/matrix/explode/object/{color}").getGet(), null);
    router.get("/path/matrix/explode/object/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_path.getObjectKeys())
        map.put(key, color_path.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_path;
    color_path = new HashMap<>();
    color_path.put("R", "100");
    color_path.put("G", "200");
    color_path.put("B", "150");


    client.pathMatrixExplodeObject(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_noexplode_empty
   * Expected parameters sent:
   * color: .
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelNoexplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/noexplode/empty/{color}").getGet(), null);
    router.get("/path/label/noexplode/empty/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "";


    client.pathLabelNoexplodeEmpty(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_noexplode_string
   * Expected parameters sent:
   * color: .blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelNoexplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/noexplode/string/{color}").getGet(), null);
    router.get("/path/label/noexplode/string/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isString());
      assertEquals(color_path.getString(), "blue");
      res.put("color", color_path.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "blue";


    client.pathLabelNoexplodeString(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_noexplode_array
   * Expected parameters sent:
   * color: .blue.black.brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/noexplode/array/{color}").getGet(), null);
    router.get("/path/label/noexplode/array/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isArray());
      res.put("color", new JsonArray(color_path.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_path;
    color_path = new ArrayList<>();
    color_path.add("blue");
    color_path.add("black");
    color_path.add("brown");


    client.pathLabelNoexplodeArray(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_noexplode_object
   * Expected parameters sent:
   * color: .R.100.G.200.B.150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/noexplode/object/{color}").getGet(), null);
    router.get("/path/label/noexplode/object/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_path.getObjectKeys())
        map.put(key, color_path.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_path;
    color_path = new HashMap<>();
    color_path.put("R", "100");
    color_path.put("G", "200");
    color_path.put("B", "150");


    client.pathLabelNoexplodeObject(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_explode_empty
   * Expected parameters sent:
   * color: .
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelExplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/explode/empty/{color}").getGet(), null);
    router.get("/path/label/explode/empty/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "";


    client.pathLabelExplodeEmpty(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_explode_string
   * Expected parameters sent:
   * color: .blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelExplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/explode/string/{color}").getGet(), null);
    router.get("/path/label/explode/string/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isString());
      assertEquals(color_path.getString(), "blue");
      res.put("color", color_path.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "blue";


    client.pathLabelExplodeString(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_explode_array
   * Expected parameters sent:
   * color: .blue.black.brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelExplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/explode/array/{color}").getGet(), null);
    router.get("/path/label/explode/array/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isArray());
      res.put("color", new JsonArray(color_path.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_path;
    color_path = new ArrayList<>();
    color_path.add("blue");
    color_path.add("black");
    color_path.add("brown");


    client.pathLabelExplodeArray(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_label_explode_object
   * Expected parameters sent:
   * color: .R=100.G=200.B=150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testPathLabelExplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/label/explode/object/{color}").getGet(), null);
    router.get("/path/label/explode/object/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_path.getObjectKeys())
        map.put(key, color_path.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_path;
    color_path = new HashMap<>();
    color_path.put("R", "100");
    color_path.put("G", "200");
    color_path.put("B", "150");


    client.pathLabelExplodeObject(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_simple_noexplode_string
   * Expected parameters sent:
   * color: blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testPathSimpleNoexplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/simple/noexplode/string/{color}").getGet(), null);
    router.get("/path/simple/noexplode/string/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isString());
      assertEquals(color_path.getString(), "blue");
      res.put("color", color_path.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "blue";


    client.pathSimpleNoexplodeString(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_simple_noexplode_array
   * Expected parameters sent:
   * color: blue,black,brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testPathSimpleNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/simple/noexplode/array/{color}").getGet(), null);
    router.get("/path/simple/noexplode/array/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isArray());
      res.put("color", new JsonArray(color_path.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_path;
    color_path = new ArrayList<>();
    color_path.add("blue");
    color_path.add("black");
    color_path.add("brown");


    client.pathSimpleNoexplodeArray(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_simple_noexplode_object
   * Expected parameters sent:
   * color: R,100,G,200,B,150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testPathSimpleNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/simple/noexplode/object/{color}").getGet(), null);
    router.get("/path/simple/noexplode/object/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_path.getObjectKeys())
        map.put(key, color_path.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_path;
    color_path = new HashMap<>();
    color_path.put("R", "100");
    color_path.put("G", "200");
    color_path.put("B", "150");


    client.pathSimpleNoexplodeObject(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_simple_explode_string
   * Expected parameters sent:
   * color: blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testPathSimpleExplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/simple/explode/string/{color}").getGet(), null);
    router.get("/path/simple/explode/string/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isString());
      assertEquals(color_path.getString(), "blue");
      res.put("color", color_path.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_path;
    color_path = "blue";


    client.pathSimpleExplodeString(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_simple_explode_array
   * Expected parameters sent:
   * color: blue,black,brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testPathSimpleExplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/simple/explode/array/{color}").getGet(), null);
    router.get("/path/simple/explode/array/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isArray());
      res.put("color", new JsonArray(color_path.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_path;
    color_path = new ArrayList<>();
    color_path.add("blue");
    color_path.add("black");
    color_path.add("brown");


    client.pathSimpleExplodeArray(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: path_simple_explode_object
   * Expected parameters sent:
   * color: R=100,G=200,B=150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testPathSimpleExplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/path/simple/explode/object/{color}").getGet(), null);
    router.get("/path/simple/explode/object/{color}").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_path = params.pathParameter("color");
      assertNotNull(color_path);
      assertTrue(color_path.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_path.getObjectKeys())
        map.put(key, color_path.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_path;
    color_path = new HashMap<>();
    color_path.put("R", "100");
    color_path.put("G", "200");
    color_path.put("B", "150");


    client.pathSimpleExplodeObject(color_path, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_noexplode_empty
   * Expected parameters sent:
   * color: color=
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormNoexplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/noexplode/empty").getGet(), null);
    router.get("/query/form/noexplode/empty").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_query;
    color_query = "";


    client.queryFormNoexplodeEmpty(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_noexplode_string
   * Expected parameters sent:
   * color: color=blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormNoexplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/noexplode/string").getGet(), null);
    router.get("/query/form/noexplode/string").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isString());
      assertEquals(color_query.getString(), "blue");
      res.put("color", color_query.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_query;
    color_query = "blue";


    client.queryFormNoexplodeString(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_noexplode_array
   * Expected parameters sent:
   * color: color=blue,black,brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/noexplode/array").getGet(), null);
    router.get("/query/form/noexplode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isArray());
      res.put("color", new JsonArray(color_query.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_query;
    color_query = new ArrayList<>();
    color_query.add("blue");
    color_query.add("black");
    color_query.add("brown");


    client.queryFormNoexplodeArray(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_noexplode_object
   * Expected parameters sent:
   * color: color=R,100,G,200,B,150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/noexplode/object").getGet(), null);
    router.get("/query/form/noexplode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_query.getObjectKeys())
        map.put(key, color_query.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_query;
    color_query = new HashMap<>();
    color_query.put("R", "100");
    color_query.put("G", "200");
    color_query.put("B", "150");


    client.queryFormNoexplodeObject(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_explode_empty
   * Expected parameters sent:
   * color: color=
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormExplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/explode/empty").getGet(), null);
    router.get("/query/form/explode/empty").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_query;
    color_query = "";


    client.queryFormExplodeEmpty(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_explode_string
   * Expected parameters sent:
   * color: color=blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormExplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/explode/string").getGet(), null);
    router.get("/query/form/explode/string").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isString());
      assertEquals(color_query.getString(), "blue");
      res.put("color", color_query.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_query;
    color_query = "blue";


    client.queryFormExplodeString(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_explode_array
   * Expected parameters sent:
   * color: color=blue&color=black&color=brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormExplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/explode/array").getGet(), null);
    router.get("/query/form/explode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isArray());
      res.put("color", new JsonArray(color_query.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_query;
    color_query = new ArrayList<>();
    color_query.add("blue");
    color_query.add("black");
    color_query.add("brown");


    client.queryFormExplodeArray(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_form_explode_object
   * Expected parameters sent:
   * color: R=100&G=200&B=150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testQueryFormExplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/form/explode/object").getGet(), null);
    router.get("/query/form/explode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_query.getObjectKeys())
        map.put(key, color_query.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_query;
    color_query = new HashMap<>();
    color_query.put("R", "100");
    color_query.put("G", "200");
    color_query.put("B", "150");


    client.queryFormExplodeObject(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_spaceDelimited_noexplode_array
   * Expected parameters sent:
   * color: blue%20black%20brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testQuerySpaceDelimitedNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/spaceDelimited/noexplode/array").getGet(), null);
    router.get("/query/spaceDelimited/noexplode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isArray());
      res.put("color", new JsonArray(color_query.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_query;
    color_query = new ArrayList<>();
    color_query.add("blue");
    color_query.add("black");
    color_query.add("brown");


    client.querySpaceDelimitedNoexplodeArray(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_spaceDelimited_noexplode_object
   * Expected parameters sent:
   * color: R%20100%20G%20200%20B%20150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testQuerySpaceDelimitedNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/spaceDelimited/noexplode/object").getGet(), null);
    router.get("/query/spaceDelimited/noexplode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_query.getObjectKeys())
        map.put(key, color_query.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_query;
    color_query = new HashMap<>();
    color_query.put("R", "100");
    color_query.put("G", "200");
    color_query.put("B", "150");


    client.querySpaceDelimitedNoexplodeObject(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_pipeDelimited_noexplode_array
   * Expected parameters sent:
   * color: blue|black|brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testQueryPipeDelimitedNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/pipeDelimited/noexplode/array").getGet(), null);
    router.get("/query/pipeDelimited/noexplode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isArray());
      res.put("color", new JsonArray(color_query.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_query;
    color_query = new ArrayList<>();
    color_query.add("blue");
    color_query.add("black");
    color_query.add("brown");


    client.queryPipeDelimitedNoexplodeArray(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_pipeDelimited_noexplode_object
   * Expected parameters sent:
   * color: R|100|G|200|B|150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testQueryPipeDelimitedNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/pipeDelimited/noexplode/object").getGet(), null);
    router.get("/query/pipeDelimited/noexplode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_query.getObjectKeys())
        map.put(key, color_query.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_query;
    color_query = new HashMap<>();
    color_query.put("R", "100");
    color_query.put("G", "200");
    color_query.put("B", "150");


    client.queryPipeDelimitedNoexplodeObject(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: query_deepObject_explode_object
   * Expected parameters sent:
   * color: color[R]=100&color[G]=200&color[B]=150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testQueryDeepObjectExplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/query/deepObject/explode/object").getGet(), null);
    router.get("/query/deepObject/explode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_query = params.queryParameter("color");
      assertNotNull(color_query);
      assertTrue(color_query.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_query.getObjectKeys())
        map.put(key, color_query.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_query;
    color_query = new HashMap<>();
    color_query.put("R", "100");
    color_query.put("G", "200");
    color_query.put("B", "150");


    client.queryDeepObjectExplodeObject(color_query, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_noexplode_empty
   * Expected parameters sent:
   * color: color=
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormNoexplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/noexplode/empty").getGet(), null);
    router.get("/cookie/form/noexplode/empty").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_cookie;
    color_cookie = "";


    client.cookieFormNoexplodeEmpty(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_noexplode_string
   * Expected parameters sent:
   * color: color=blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormNoexplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/noexplode/string").getGet(), null);
    router.get("/cookie/form/noexplode/string").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isString());
      assertEquals(color_cookie.getString(), "blue");
      res.put("color", color_cookie.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_cookie;
    color_cookie = "blue";


    client.cookieFormNoexplodeString(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_noexplode_array
   * Expected parameters sent:
   * color: color=blue,black,brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/noexplode/array").getGet(), null);
    router.get("/cookie/form/noexplode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isArray());
      res.put("color", new JsonArray(color_cookie.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_cookie;
    color_cookie = new ArrayList<>();
    color_cookie.add("blue");
    color_cookie.add("black");
    color_cookie.add("brown");


    client.cookieFormNoexplodeArray(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_noexplode_object
   * Expected parameters sent:
   * color: color=R,100,G,200,B,150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/noexplode/object").getGet(), null);
    router.get("/cookie/form/noexplode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_cookie.getObjectKeys())
        map.put(key, color_cookie.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_cookie;
    color_cookie = new HashMap<>();
    color_cookie.put("R", "100");
    color_cookie.put("G", "200");
    color_cookie.put("B", "150");


    client.cookieFormNoexplodeObject(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_explode_empty
   * Expected parameters sent:
   * color: color=
   * Expected response: {"color":null}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormExplodeEmpty() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/explode/empty").getGet(), null);
    router.get("/cookie/form/explode/empty").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isEmpty());
      res.putNull("color");


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_cookie;
    color_cookie = "";


    client.cookieFormExplodeEmpty(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":null}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":null}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_explode_string
   * Expected parameters sent:
   * color: color=blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormExplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/explode/string").getGet(), null);
    router.get("/cookie/form/explode/string").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isString());
      assertEquals(color_cookie.getString(), "blue");
      res.put("color", color_cookie.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_cookie;
    color_cookie = "blue";


    client.cookieFormExplodeString(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_explode_array
   * Expected parameters sent:
   * color: color=blue&color=black&color=brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormExplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/explode/array").getGet(), null);
    router.get("/cookie/form/explode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isArray());
      res.put("color", new JsonArray(color_cookie.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_cookie;
    color_cookie = new ArrayList<>();
    color_cookie.add("blue");
    color_cookie.add("black");
    color_cookie.add("brown");


    client.cookieFormExplodeArray(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: cookie_form_explode_object
   * Expected parameters sent:
   * color: R=100&G=200&B=150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testCookieFormExplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/cookie/form/explode/object").getGet(), null);
    router.get("/cookie/form/explode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_cookie = params.cookieParameter("color");
      assertNotNull(color_cookie);
      assertTrue(color_cookie.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_cookie.getObjectKeys())
        map.put(key, color_cookie.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_cookie;
    color_cookie = new HashMap<>();
    color_cookie.put("R", "100");
    color_cookie.put("G", "200");
    color_cookie.put("B", "150");


    client.cookieFormExplodeObject(color_cookie, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: header_simple_noexplode_string
   * Expected parameters sent:
   * color: blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testHeaderSimpleNoexplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/header/simple/noexplode/string").getGet(), null);
    router.get("/header/simple/noexplode/string").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_header = params.headerParameter("color");
      assertNotNull(color_header);
      assertTrue(color_header.isString());
      assertEquals(color_header.getString(), "blue");
      res.put("color", color_header.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_header;
    color_header = "blue";


    client.headerSimpleNoexplodeString(color_header, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: header_simple_noexplode_array
   * Expected parameters sent:
   * color: blue,black,brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testHeaderSimpleNoexplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/header/simple/noexplode/array").getGet(), null);
    router.get("/header/simple/noexplode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_header = params.headerParameter("color");
      assertNotNull(color_header);
      assertTrue(color_header.isArray());
      res.put("color", new JsonArray(color_header.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_header;
    color_header = new ArrayList<>();
    color_header.add("blue");
    color_header.add("black");
    color_header.add("brown");


    client.headerSimpleNoexplodeArray(color_header, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: header_simple_noexplode_object
   * Expected parameters sent:
   * color: R,100,G,200,B,150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testHeaderSimpleNoexplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/header/simple/noexplode/object").getGet(), null);
    router.get("/header/simple/noexplode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_header = params.headerParameter("color");
      assertNotNull(color_header);
      assertTrue(color_header.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_header.getObjectKeys())
        map.put(key, color_header.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_header;
    color_header = new HashMap<>();
    color_header.put("R", "100");
    color_header.put("G", "200");
    color_header.put("B", "150");


    client.headerSimpleNoexplodeObject(color_header, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: header_simple_explode_string
   * Expected parameters sent:
   * color: blue
   * Expected response: {"color":"blue"}
   *
   * @throws Exception
   */
  @Test
  public void testHeaderSimpleExplodeString() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/header/simple/explode/string").getGet(), null);
    router.get("/header/simple/explode/string").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_header = params.headerParameter("color");
      assertNotNull(color_header);
      assertTrue(color_header.isString());
      assertEquals(color_header.getString(), "blue");
      res.put("color", color_header.getString());


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    String color_header;
    color_header = "blue";


    client.headerSimpleExplodeString(color_header, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":\"blue\"}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":\"blue\"}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: header_simple_explode_array
   * Expected parameters sent:
   * color: blue,black,brown
   * Expected response: {"color":["blue","black","brown"]}
   *
   * @throws Exception
   */
  @Test
  public void testHeaderSimpleExplodeArray() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/header/simple/explode/array").getGet(), null);
    router.get("/header/simple/explode/array").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_header = params.headerParameter("color");
      assertNotNull(color_header);
      assertTrue(color_header.isArray());
      res.put("color", new JsonArray(color_header.getArray().stream().map(param -> param.getString()).collect(Collectors.toList())));


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    List<Object> color_header;
    color_header = new ArrayList<>();
    color_header.add("blue");
    color_header.add("black");
    color_header.add("brown");


    client.headerSimpleExplodeArray(color_header, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":[\"blue\",\"black\",\"brown\"]}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

  /**
   * Test: header_simple_explode_object
   * Expected parameters sent:
   * color: R=100,G=200,B=150
   * Expected response: {"color":{"R":"100","G":"200","B":"150"}}
   *
   * @throws Exception
   */
  @Test
  public void testHeaderSimpleExplodeObject() throws Exception {
    OpenAPI3RequestValidationHandler validationHandler = new OpenAPI3RequestValidationHandlerImpl(spec.getPath("/header/simple/explode/object").getGet(), null);
    router.get("/header/simple/explode/object").handler(validationHandler).handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      JsonObject res = new JsonObject();

      RequestParameter color_header = params.headerParameter("color");
      assertNotNull(color_header);
      assertTrue(color_header.isObject());
      Map<String, String> map = new HashMap<>();
      for (String key : color_header.getObjectKeys())
        map.put(key, color_header.getObjectValue(key).getString());
      res.put("color", map);


      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(res.encode());
    }).failureHandler(generateFailureHandler(false));

    CountDownLatch latch = new CountDownLatch(1);

    Map<String, Object> color_header;
    color_header = new HashMap<>();
    color_header.put("R", "100");
    color_header.put("G", "200");
    color_header.put("B", "150");


    client.headerSimpleExplodeObject(color_header, (AsyncResult<HttpResponse> ar) -> {
      if (ar.succeeded()) {
        assertEquals(200, ar.result().statusCode());
        assertTrue("Expected: " + new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").encode() + " Actual: " + ar.result().bodyAsJsonObject().encode(), new JsonObject("{\"color\":{\"R\":\"100\",\"G\":\"200\",\"B\":\"150\"}}").equals(ar.result().bodyAsJsonObject()));
      } else {
        assertTrue(ar.cause().getMessage(), false);
      }
      latch.countDown();
    });
    awaitLatch(latch);

  }

}
