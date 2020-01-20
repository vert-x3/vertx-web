package io.vertx.ext.web.openapi.asserts;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.OpenAPIHolder;

public class MyAssertions {

  public static JsonAssert assertThat(JsonObject actual) { return new JsonAssert(actual); }

  public static JsonAssert assertThat(JsonArray actual) { return new JsonAssert(actual); }

  public static OpenAPILoaderAssert assertThat(OpenAPIHolder actual) { return new OpenAPILoaderAssert(actual); }

  public static JsonAssert assertThatJson(Object actual) { return new JsonAssert(actual); }

}
