package io.vertx.ext.web.openapi;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.openapi.OpenAPILoaderOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.openapi.OpenAPILoaderOptions} original class using Vert.x codegen.
 */
public class OpenAPILoaderOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OpenAPILoaderOptions obj) {
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

   static void toJson(OpenAPILoaderOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(OpenAPILoaderOptions obj, java.util.Map<String, Object> json) {
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
