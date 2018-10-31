package io.vertx.ext.web.api.generator;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

public class ApiHandlerUtils {

  public static Object searchInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return  obj.getValue("body");
    if (obj.getJsonObject("path").containsKey(key)) return  obj.getJsonObject("path").getValue(key);
    if (obj.getJsonObject("query").containsKey(key)) return  obj.getJsonObject("query").getValue(key);
    if (obj.getJsonObject("header").containsKey(key)) return  obj.getJsonObject("header").getValue(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return  obj.getJsonObject("cookie").getValue(key);
    if (obj.getJsonObject("form").containsKey(key)) return  obj.getJsonObject("form").getValue(key);
    return null;
  }

  public static Optional<Object> searchOptionalInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchInJson(obj, key));
  }

  public static Integer searchIntegerInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return obj.getInteger("body");
    if (obj.getJsonObject("path").containsKey(key)) return  obj.getJsonObject("path").getInteger(key);
    if (obj.getJsonObject("query").containsKey(key)) return  obj.getJsonObject("query").getInteger(key);
    if (obj.getJsonObject("header").containsKey(key)) return  obj.getJsonObject("header").getInteger(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return  obj.getJsonObject("cookie").getInteger(key);
    if (obj.getJsonObject("form").containsKey(key)) return  obj.getJsonObject("form").getInteger(key);
    return null;
  }

  public static Optional<Integer> searchOptionalIntegerInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchIntegerInJson(obj, key));
  }

  public static Character searchCharInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return (Character)obj.getValue("body");
    if (obj.getJsonObject("path").containsKey(key)) return (Character) obj.getJsonObject("path").getValue(key);
    if (obj.getJsonObject("query").containsKey(key)) return (Character) obj.getJsonObject("query").getValue(key);
    if (obj.getJsonObject("header").containsKey(key)) return (Character) obj.getJsonObject("header").getValue(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return (Character) obj.getJsonObject("cookie").getValue(key);
    if (obj.getJsonObject("form").containsKey(key)) return (Character) obj.getJsonObject("form").getValue(key);
    return null;
  }

  public static Optional<Character> searchOptionalCharacterInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchCharInJson(obj, key));
  }

  public static Long searchLongInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return obj.getLong("body");
    if (obj.getJsonObject("path").containsKey(key)) return  obj.getJsonObject("path").getLong(key);
    if (obj.getJsonObject("query").containsKey(key)) return  obj.getJsonObject("query").getLong(key);
    if (obj.getJsonObject("header").containsKey(key)) return  obj.getJsonObject("header").getLong(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return  obj.getJsonObject("cookie").getLong(key);
    if (obj.getJsonObject("form").containsKey(key)) return  obj.getJsonObject("form").getLong(key);
    return null;
  }

  public static Optional<Long> searchOptionalLongInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchLongInJson(obj, key));
  }

  public static Double searchDoubleInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return obj.getDouble("body");
    if (obj.getJsonObject("path").containsKey(key)) return  obj.getJsonObject("path").getDouble(key);
    if (obj.getJsonObject("query").containsKey(key)) return  obj.getJsonObject("query").getDouble(key);
    if (obj.getJsonObject("header").containsKey(key)) return  obj.getJsonObject("header").getDouble(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return  obj.getJsonObject("cookie").getDouble(key);
    if (obj.getJsonObject("form").containsKey(key)) return  obj.getJsonObject("form").getDouble(key);
    return null;
  }

  public static Optional<Double> searchOptionalDoubleInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchDoubleInJson(obj, key));
  }

  public static String searchStringInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return obj.getString("body");
    if (obj.getJsonObject("path").containsKey(key)) return  obj.getJsonObject("path").getString(key);
    if (obj.getJsonObject("query").containsKey(key)) return  obj.getJsonObject("query").getString(key);
    if (obj.getJsonObject("header").containsKey(key)) return  obj.getJsonObject("header").getString(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return  obj.getJsonObject("cookie").getString(key);
    if (obj.getJsonObject("form").containsKey(key)) return  obj.getJsonObject("form").getString(key);
    return null;
  }

  public static Optional<String> searchOptionalStringInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchStringInJson(obj, key));
  }

  public static JsonArray searchJsonArrayInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return obj.getJsonArray("body");
    if (obj.getJsonObject("path").containsKey(key)) return  obj.getJsonObject("path").getJsonArray(key);
    if (obj.getJsonObject("query").containsKey(key)) return  obj.getJsonObject("query").getJsonArray(key);
    if (obj.getJsonObject("header").containsKey(key)) return  obj.getJsonObject("header").getJsonArray(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return  obj.getJsonObject("cookie").getJsonArray(key);
    if (obj.getJsonObject("form").containsKey(key)) return  obj.getJsonObject("form").getJsonArray(key);
    return null;
  }

  public static Optional<JsonArray> searchOptionalJsonArrayInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchJsonArrayInJson(obj, key));
  }

  public static JsonObject searchJsonObjectInJson(JsonObject obj, String key) {
    if ("body".equals(key)) return obj.getJsonObject("body");
    if (obj.getJsonObject("path").containsKey(key)) return obj.getJsonObject("path").getJsonObject(key);
    if (obj.getJsonObject("query").containsKey(key)) return  obj.getJsonObject("query").getJsonObject(key);
    if (obj.getJsonObject("header").containsKey(key)) return  obj.getJsonObject("header").getJsonObject(key);
    if (obj.getJsonObject("cookie").containsKey(key)) return  obj.getJsonObject("cookie").getJsonObject(key);
    if (obj.getJsonObject("form").containsKey(key)) return  obj.getJsonObject("form").getJsonObject(key);
    return null;
  }

  public static Optional<JsonObject> searchOptionalJsonObjectInJson(JsonObject obj, String key) {
    return Optional.ofNullable(searchJsonObjectInJson(obj, key));
  }

}
