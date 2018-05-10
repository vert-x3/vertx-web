package io.vertx.ext.web.api;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.impl.RequestParameterImpl;
import io.vertx.ext.web.api.impl.RequestParametersImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RequestParametersTest {

  @Test
  public void testRequestParameterToJsonNumber() {
    RequestParameter param = new RequestParameterImpl("bla", 1);
    assertEquals(1, param.toJson());
  }

  @Test
  public void testRequestParameterToJsonString() {
    RequestParameter param = new RequestParameterImpl("bla", "string");
    assertEquals("string", param.toJson());
  }

  @Test
  public void testRequestParameterToJsonMap() {
    RequestParameter param1 = new RequestParameterImpl("param1", 1);
    RequestParameter param2 = new RequestParameterImpl("param2", "string");
    RequestParameter param3 = new RequestParameterImpl("param3", null);
    Map<String, RequestParameter> paramsMap = new HashMap<>();
    paramsMap.put(param1.getName(), param1);
    paramsMap.put(param2.getName(), param2);
    paramsMap.put(param3.getName(), param3);

    RequestParameter params = new RequestParameterImpl("params", paramsMap);

    Object result = params.toJson();
    assertTrue(result instanceof JsonObject);
    JsonObject object = (JsonObject) result;

    assertEquals(1, object.getValue("param1"));
    assertEquals("string", object.getValue("param2"));
    assertNull(object.getValue("param3"));
  }

  @Test
  public void testRequestParameterToJsonArray() {
    RequestParameter param1 = new RequestParameterImpl("param1", 1);
    RequestParameter param2 = new RequestParameterImpl("param2", "string");
    RequestParameter param3 = new RequestParameterImpl("param3", null);
    List<RequestParameter> paramsList = new ArrayList<>();
    paramsList.add(param1);
    paramsList.add(param2);
    paramsList.add(param3);

    RequestParameter params = new RequestParameterImpl("params", paramsList);

    Object result = params.toJson();
    assertTrue(result instanceof JsonArray);
    JsonArray object = (JsonArray) result;

    assertEquals(1, object.getValue(0));
    assertEquals("string", object.getValue(1));
    assertNull(object.getValue(2));
  }

  @Test
  public void testToJsonObjectEmpty() {
    RequestParameters params = new RequestParametersImpl();
    JsonObject obj = params.toJson();
    assertEquals(0, obj.getJsonObject("path").size());
    assertEquals(0, obj.getJsonObject("cookie").size());
    assertEquals(0, obj.getJsonObject("query").size());
    assertEquals(0, obj.getJsonObject("form").size());
    assertEquals(0, obj.getJsonObject("header").size());
    assertNull(obj.getValue("body"));
  }

  @Test
  public void testToJsonObject() {
    RequestParameter param1 = new RequestParameterImpl("param1", 1);
    RequestParameter param2 = new RequestParameterImpl("param2", "string");
    RequestParameter param3 = new RequestParameterImpl("param3", null);
    Map<String, RequestParameter> paramsMap = new HashMap<>();
    paramsMap.put(param1.getName(), param1);
    paramsMap.put(param2.getName(), param2);
    paramsMap.put(param3.getName(), param3);
    RequestParameter bodyParam = new RequestParameterImpl("params", paramsMap);

    RequestParametersImpl params = new RequestParametersImpl();
    params.setCookieParameters(paramsMap);
    params.setFormParameters(paramsMap);
    params.setHeaderParameters(paramsMap);
    params.setPathParameters(paramsMap);
    params.setQueryParameters(paramsMap);
    params.setBody(bodyParam);

    JsonObject obj = params.toJson();
    assertEquals(3, obj.getJsonObject("path").size());
    assertEquals(3, obj.getJsonObject("cookie").size());
    assertEquals(3, obj.getJsonObject("query").size());
    assertEquals(3, obj.getJsonObject("form").size());
    assertEquals(3, obj.getJsonObject("header").size());
    assertEquals(bodyParam.toJson(), obj.getValue("body"));
  }

}
