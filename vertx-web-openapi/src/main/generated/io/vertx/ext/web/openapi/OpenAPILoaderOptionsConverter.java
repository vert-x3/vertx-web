package io.vertx.ext.web.openapi;

import io.vertx.core.json.JsonObject;

/**
 * Converter and mapper for {@link io.vertx.ext.web.openapi.OpenAPILoaderOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.openapi.OpenAPILoaderOptions} original class using Vert.x codegen.
 */
public class OpenAPILoaderOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OpenAPILoaderOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "authHeaders":
          if (member.getValue() instanceof JsonObject) {
          }
          break;
        case "authQueryParams":
          if (member.getValue() instanceof JsonObject) {
          }
          break;
      }
    }
  }

  public static void toJson(OpenAPILoaderOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(OpenAPILoaderOptions obj, java.util.Map<String, Object> json) {
    if (obj.getAuthHeaders() != null) {
      JsonObject map = new JsonObject();
      obj.getAuthHeaders().forEach((key, value) -> map.put(key, value));
      json.put("authHeaders", map);
    }
    if (obj.getAuthQueryParams() != null) {
      JsonObject map = new JsonObject();
      obj.getAuthQueryParams().forEach((key, value) -> map.put(key, value));
      json.put("authQueryParams", map);
    }
  }
}
