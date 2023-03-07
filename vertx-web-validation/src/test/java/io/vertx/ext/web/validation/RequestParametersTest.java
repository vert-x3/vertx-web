package io.vertx.ext.web.validation;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;
import io.vertx.ext.web.validation.impl.RequestParametersImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RequestParametersTest {

  @Test
  public void testRequestParameterToJsonNumber() {
    RequestParameter param = new RequestParameterImpl(1);
    Assertions.assertEquals(1, param.get());
  }

  @Test
  public void testRequestParameterToJsonString() {
    RequestParameter param = new RequestParameterImpl("string");
    Assertions.assertEquals("string", param.get());
  }

  @Test
  public void testToJsonObjectEmpty() {
    RequestParameters params = new RequestParametersImpl();
    JsonObject obj = params.toJson();
    Assertions.assertEquals(0, obj.getJsonObject("path").size());
    Assertions.assertEquals(0, obj.getJsonObject("cookie").size());
    Assertions.assertEquals(0, obj.getJsonObject("query").size());
    Assertions.assertEquals(0, obj.getJsonObject("header").size());
    Assertions.assertNull(obj.getValue("body"));
  }

  @Test
  public void testToJsonObjectWithNull() {
    Map<String, RequestParameter> map = new HashMap<>();
    map.put("aaa", new RequestParameterImpl(1));
    map.put("bbb", null);

    RequestParametersImpl params = new RequestParametersImpl();
    params.setPathParameters(
      map
    );

    JsonObject obj = params.toJson();
    Assertions.assertEquals(2, obj.getJsonObject("path").size());
    Assertions.assertEquals(1, obj.getJsonObject("path").getValue("aaa"));
    Assertions.assertNull(obj.getJsonObject("path").getValue("bbb"));
  }

}
