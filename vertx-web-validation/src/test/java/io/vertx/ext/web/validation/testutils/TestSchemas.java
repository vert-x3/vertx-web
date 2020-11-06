package io.vertx.ext.web.validation.testutils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.ArraySchemaBuilder;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.json.schema.common.dsl.StringSchemaBuilder;

import java.util.regex.Pattern;

import static io.vertx.json.schema.draft7.dsl.Keywords.maxLength;
import static io.vertx.json.schema.draft7.dsl.Schemas.*;

public class TestSchemas {

  public static ObjectSchemaBuilder SAMPLE_OBJECT_SCHEMA_BUILDER =
    objectSchema()
      .property("someNumbers", arraySchema().items(numberSchema()))
      .property("oneNumber", numberSchema())
      .patternProperty(Pattern.compile("someIntegers"), arraySchema().items(intSchema()))
      .patternProperty(Pattern.compile("oneInteger"), intSchema())
      .additionalProperties(booleanSchema());

  public static JsonObject VALID_OBJECT =
    new JsonObject()
      .put("someNumbers", new JsonArray().add(1.1).add(2.2))
      .put("oneNumber", 3.3)
      .put("someIntegers", new JsonArray().add(1).add(2))
      .put("oneInteger", 3)
      .put("aBoolean", true);

  public static JsonObject INVALID_OBJECT =
    new JsonObject()
      .put("someNumbers", new JsonArray().add(1.1).add(2.2))
      .put("oneNumber", 3.3)
      .put("someIntegers", new JsonArray().add(1).add(2))
      .put("oneInteger", 3)
      .put("aBoolean", "bla");

  public static ArraySchemaBuilder SAMPLE_ARRAY_SCHEMA_BUILDER =
    arraySchema()
      .items(stringSchema());

  public static JsonArray VALID_ARRAY =
    new JsonArray()
      .add("")
      .add("bla");

  public static JsonArray INVALID_ARRAY =
    new JsonArray()
      .add("")
      .add("bla")
      .add(1);

  public static StringSchemaBuilder SAMPLE_STRING_SCHEMA_BUILDER =
    stringSchema()
      .with(maxLength(5));

  public static String VALID_STRING = "aaa";

  public static String INVALID_STRING = "aaaaaa";

}
