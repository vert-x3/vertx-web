package io.vertx.ext.web.validation;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;
import io.vertx.ext.web.validation.impl.RequestParametersImpl;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RequestParametersTest {

  @Test
  public void testRequestParameterToJsonNumber() {
    RequestParameter param = new RequestParameterImpl(1);
    assertEquals(1, param.toJson());
  }

  @Test
  public void testRequestParameterToJsonString() {
    RequestParameter param = new RequestParameterImpl("string");
    assertEquals("string", param.toJson());
  }

  @Test
  public void testToJsonObjectEmpty() {
    RequestParameters params = new RequestParametersImpl();
    JsonObject obj = params.toJson();
    assertEquals(0, obj.getJsonObject("path").size());
    assertEquals(0, obj.getJsonObject("cookie").size());
    assertEquals(0, obj.getJsonObject("query").size());
    assertEquals(0, obj.getJsonObject("header").size());
    assertNull(obj.getValue("body"));
  }

}
