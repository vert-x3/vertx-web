package io.vertx.ext.web.handler.graphql;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.graphql.ApolloWSOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.graphql.ApolloWSOptions} original class using Vert.x codegen.
 */
public class ApolloWSOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ApolloWSOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "keepAlive":
          if (member.getValue() instanceof Number) {
            obj.setKeepAlive(((Number)member.getValue()).longValue());
          }
          break;
        case "origin":
          if (member.getValue() instanceof String) {
            obj.setOrigin((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(ApolloWSOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(ApolloWSOptions obj, java.util.Map<String, Object> json) {
    json.put("keepAlive", obj.getKeepAlive());
    if (obj.getOrigin() != null) {
      json.put("origin", obj.getOrigin());
    }
  }
}
