package io.vertx.ext.web.openapi.asserts;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.OpenAPIHolder;

public class MyAssertions {

  public static JsonAssert assertThat(JsonObject actual) { return new JsonAssert(actual); }

  public static JsonAssert assertThat(JsonArray actual) { return new JsonAssert(actual); }

  public static OpenAPIHolderAssert assertThat(OpenAPIHolder actual) {
    return new OpenAPIHolderAssert(actual);
  }

  public static JsonAssert assertThatJson(Object actual) { return new JsonAssert(actual); }

}
